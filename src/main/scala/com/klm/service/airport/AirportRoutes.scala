package com.klm.service.airport

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import com.klm.service.airport.cache.CacheClient
import com.klm.service.domain.TravelModel.{ Currency, Language, SortByField }
import com.klm.service.{ ExtraRoutes, Routes, SecuredRoutes }
import io.circe.generic.auto._

import scala.language.implicitConversions
import scala.util.{ Failure, Success }

class AirportRoutes(val airportService: AirportService, val cacheClient: CacheClient)
    extends Routes
    with SecuredRoutes
    with ExtraRoutes {

  val airportRoutes: Route = routes

  def time[R](block: => R): R = {
    val t0     = System.nanoTime()
    val result = block // call-by-name
    val t1     = System.nanoTime()
    val cur    = (t1 - t0) / 1000
    result
  }

  def routes: Route =
    time {
      withRequestId { requestId =>
        implicit val contextRequestId: String = requestId
        pathPrefix("api" / version)(
          travelManagement
        )
      }
    }

  def travelManagement(implicit contextRequestId: String): Route =
    pathPrefix("travel") {
      airportManagement ~
      fareManagement
    }

  def airportManagement(implicit contextRequestId: String): Route =
    pathPrefix("airports") {
      airportActions ~ getAirports
    }

  def getAirports(implicit contextRequestId: String): Route =
    get {
      parameters(
        Symbol("size").as[Int].?(default    = 100),
        Symbol("page").as[Int].?(default    = 1),
        Symbol("lang").as[String].?(default = Language.EN.toString),
        Symbol("term").as[String].?,
        Symbol("sort").as[String].?(default = SortByField.none.toString)
      ) { (size, page, lang, term, sort) =>
        onComplete(
          airportService.getAirports(
            size,
            page,
            Language.withNameWithDefault(lang),
            term,
            SortByField.withNameWithDefault(sort)
          )
        ) {
          case Success(future) => completeEither(StatusCodes.OK, future)
          case Failure(ex) =>
            logger.info(ex.getMessage)
            complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
        }
      }
    }

  def airportActions(implicit contextRequestId: String): Route =
    pathPrefix(Segment)(airportCode => getAirport(airportCode))

  def getAirport(airportCode: String)(implicit contextRequestId: String): Route =
    get {
      parameters(
        Symbol("lang").as[String].?(default = Language.EN.toString)
      ) { lang =>
        onComplete(airportService.getAirport(airportCode, Language.withNameWithDefault(lang))) {
          case Success(future) => completeEither(StatusCodes.OK, future)
          case Failure(ex) =>
            complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
        }
      }
    }

  def fareManagement(implicit contextRequestId: String): Route =
    pathPrefix("fare" / Segment / Segment) { (originCode, destinationCode) =>
      get {
        parameters(
          Symbol("currency").as[String].?(default = Currency.EUR.toString)
        ) { currency =>
          onComplete(
            airportService.getFareOffer(
              originCode,
              destinationCode,
              Currency.withNameWithDefault(currency)
            )
          ) {
            case Success(future) => completeEither(StatusCodes.OK, future)
            case Failure(ex) =>
              complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
          }
        }
      }
    }
}
