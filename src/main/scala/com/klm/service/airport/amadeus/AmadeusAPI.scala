package com.klm.service.airport.amadeus

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import akka.pattern.{ CircuitBreaker, CircuitBreakerOpenException }
import cats.data.EitherT
import com.klm.service.airport.TravelAPIModel.{ Airport, Fare }
import com.klm.service.airport.cache.CacheClient
import com.klm.service.domain.TravelModel.Currency.Currency
import com.klm.service.domain.TravelModel.Language.Language
import com.klm.service.domain.TravelModel.{ Currency, Language }
import com.klm.service.errors.ServiceError
import com.klm.service.errors.ServiceError._
import com.typesafe.scalalogging.LazyLogging
import io.circe.Json
import io.circe.parser.{ decode, parse }
import io.circe.schema.Schema
import sttp.client3.{ basicRequest, HttpURLConnectionBackend, UriContext }
import sttp.model.StatusCode

import java.util.Base64
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.Source
import scala.language.implicitConversions

class AmadeusAPI(val cacheClient: CacheClient)(implicit system: ActorSystem) extends TravelAPI with LazyLogging {

  val airport_json_schema: Schema =
    Schema.load(parse(Source.fromResource("json_schemas/airport_json_schema.json").mkString).getOrElse(Json.Null))
  val airports_json_schema: Schema =
    Schema.load(parse(Source.fromResource("json_schemas/airports_json_schema.json").mkString).getOrElse(Json.Null))
  val fare_json_schema: Schema =
    Schema.load(parse(Source.fromResource("json_schemas/fare_json_schema.json").mkString).getOrElse(Json.Null))

  def getAirports(
      size: Int            = 1000,
      page: Int            = 1,
      lang: Language       = Language.EN,
      term: Option[String] = None
  )(implicit contextRequestId: String): Future[Either[ServiceError, List[Airport]]] = {
    logger.info(s"getAirports")

    val params = mutable.Map(
      "size" -> size.toString,
      "page" -> page.toString,
      "lang" -> lang.toString
    )

    if (term.nonEmpty)
      params.addOne("term" -> term.get)

    val uri = Uri
      .from(
        scheme = "http",
        host   = "localhost",
        port   = 8080,
        path   = "/airports"
      )
      .withQuery(Query(params.toMap))

    logger.info(
      s"RequestID: $contextRequestId - fetch airports with param size: $size, page: $page, lang: $lang, and term: $term"
    )

    val result = for {
      unmarshalledJson <- EitherT(loadFromCacheOrCall(uri.toString()))
      validatedJson <- EitherT(Future.successful(validateJsonWithSchema(unmarshalledJson, airports_json_schema)))
      airports <- EitherT(
        Future.successful(decodeToAirportList(validatedJson))
      )
    } yield airports

    result.value
  }

  def getAirport(code: String, lang: Language = Language.EN)(
      implicit contextRequestId: String
  ): Future[Either[ServiceError, Airport]] = {
    logger.info(s"getAirport for code: $code")

    val params = mutable.Map(
      "lang" -> lang.toString
    )

    val uri = Uri
      .from(
        scheme = "http",
        host   = "localhost",
        port   = 8080,
        path   = s"/airports/$code"
      )
      .withQuery(Query(params.toMap))

    val result: EitherT[Future, ServiceError, Airport] = for {
      unmarshalledJson <- EitherT(loadFromCacheOrCall(uri.toString()))
      validatedJson <- EitherT(Future.successful(validateJsonWithSchema(unmarshalledJson, airport_json_schema)))
      airport <- EitherT(
        Future.successful(decodeToAirport(validatedJson))
      )
    } yield airport

    result.value
  }

  def getFareOffer(
      originCode: String,
      destinationCode: String,
      currency: Currency = Currency.EUR
  )(implicit contextRequestId: String): Future[Either[ServiceError, Fare]] = {
    logger.info(s"getFareOffer for origin: $originCode and destination: $destinationCode")

    val params = mutable.Map(
      "currency" -> currency.toString
    )
    val uri = Uri
      .from(
        scheme = "http",
        host   = "localhost",
        port   = 8080,
        path   = s"/fares/$originCode/$destinationCode"
      )
      .withQuery(Query(params.toMap))
    val result: EitherT[Future, ServiceError, Fare] = for {
      unmarshalledJson <- EitherT(callEndpoint(uri.toString()))
      validatedJson <- EitherT(Future.successful(validateJsonWithSchema(unmarshalledJson, fare_json_schema)))
      fare <- EitherT(
        Future.successful(decodeToFare(validatedJson))
      )
    } yield fare
    result.value
  }

  def decodeToAirportList(validatedJson: Json)(implicit contextRequestId: String): Either[ServiceError, List[Airport]] = {
    logger.info(s"decodeToAirportList")
    decode[List[Airport]](validatedJson.toString()) match {
      case Left(_) =>
        Left(DecodeJSONError(""))
      case Right(value) =>
        Right(value)
    }
  }

  def decodeToAirport(validatedJson: Json)(implicit contextRequestId: String): Either[ServiceError, Airport] = {
    logger.info(s"decodeToAirport")
    decode[Airport](validatedJson.toString()) match {
      case Left(e) =>
        Left(DecodeJSONError(""))
      case Right(value) => Right(value)
    }
  }

  def decodeToFare(validatedJson: Json)(implicit contextRequestId: String): Either[ServiceError, Fare] = {
    logger.info(s"decodeToFare")
    decode[Fare](validatedJson.toString()) match {
      case Left(_) => Left(DecodeJSONError(""))
      case Right(value) => Right(value)
    }
  }

  private def validateJsonWithSchema(payload: String, json_schema: Schema)(
      implicit contextRequestId: String
  ): Either[ServiceError, Json] = {
    logger.info(s"validateJsonWithSchema")
    parse(payload) match {
      case Left(_) => Left(InvalidJSONError(""))
      case Right(validJson) =>
        json_schema
          .validate(validJson)
          .fold(
            _ => Left(InvalidJSONError("")),
            _ => Right(validJson)
          )
    }
  }

  private def loadFromCacheOrCall(uri: String)(implicit contextRequestId: String): Future[Either[ServiceError, String]] = {
    logger.info(s"loadFromCacheOrCall")
    cacheClient
      .loadFromCache(uri)
      .map {
        case Left(_) =>
          callEndpoint(uri)
        case Right(value) =>
          if (value.isEmpty) {
            logger.info(s"RequestID: $contextRequestId - results must return from endpoint call.")
            val res = for {
              resEndpoint <- EitherT(callEndpoint(uri))
              _ <- EitherT(cacheClient.addInCache(uri, resEndpoint))
            } yield resEndpoint
            res.value
          } else {
            logger.info(s"RequestID: $contextRequestId - results were already in cache.")
            Future.successful(Right(value.get))
          }
      }
      .flatten
  }

  private def callEndpoint(uri: String)(implicit contextRequestId: String): Future[Either[ServiceError, String]] = {
    logger.info(s"callEndpoint")
    logger.info(s"RequestID: $contextRequestId - Endpoint is called for the following $uri.")

    val travelAPIBreaker: CircuitBreaker =
      new CircuitBreaker(system.scheduler, maxFailures = 3, callTimeout = 10.seconds, resetTimeout = 0.5.minute)
        .onOpen(notifyMeOnOpen())

    val request = basicRequest
      .get(uri"$uri")
      .header(
        "Authorization",
        "Basic " + Base64.getUrlEncoder.encodeToString("user:secret123".getBytes)
      )

    val withBreaker =
      travelAPIBreaker.withCircuitBreaker(Future(request.send(HttpURLConnectionBackend())))

    withBreaker
      .flatMap(res =>
        res.code match {
          case StatusCode.NotFound => Future.successful(Left(RecordNotFound()))
          case StatusCode.Ok =>
            res.body.fold(
              _ => {
                logger.info(s"RequestID: $contextRequestId - Travel API is unavailable.")
                Future.successful(Left(ClientServiceError("Travel API is unavailable.")))
              },
              value => {
                logger.info(s"RequestID: $contextRequestId - Travel API successfully gave response.")
                Future.successful(Right(value))
              }
            )
          case _ =>
            logger.info(s"RequestID: $contextRequestId - Travel API is unavailable.")
            Future.successful(Left(ServiceUnavailable("Travel API is unavailable.")))
        }
      )
      .recoverWith {
        case _: CircuitBreakerOpenException => {
          logger.info(s"RequestID: $contextRequestId - Travel API is unavailable.")
          Future.successful(Left(ClientServiceError("Travel API is unavailable.")))
        }
      }
  }

  def notifyMeOnOpen(): Unit =
    logger.warn("My CircuitBreaker is now open, and will not close for one minute")
}
