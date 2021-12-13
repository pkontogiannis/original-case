package com.klm.service

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.server.{ Directives, Route }
import com.klm.service.airport.AirportRoutes
import com.klm.service.auth.AuthRoutes
import com.klm.service.errors.{ ErrorMapper, HttpError, ServiceError, _ }
import com.klm.service.health.HealthRoutes
import com.klm.service.user.UserRoutes
import com.klm.utils.server.Version
import com.klm.utils.swagger.Swagger
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Decoder.Result
import io.circe.generic.auto._
import io.circe.{ Decoder, Encoder, HCursor, Json }

import java.sql.Timestamp
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Routes extends Version with Directives with FailFastCirceSupport with LazyLogging {

  def completeEither[E <: ServiceError, R: ToEntityMarshaller](statusCode: StatusCode, either: => Either[E, R])(
      implicit mapper: ErrorMapper[E, HttpError]
  ): Route =
    either match {
      case Right(value) =>
        complete(statusCode, value)
      case Left(value) =>
        logger.info(value.message)
        complete(
          value.statusCode,
          ErrorResponse(code = value.code, message = value.message)
        )
    }

  implicit val httpErrorMapper: ErrorMapper[ServiceError, HttpError] =
    Routes.buildErrorMapper(ServiceError.httpErrorMapper)

  implicit class ErrorOps[E <: ServiceError, A](result: Future[Either[E, A]]) {
    def toRestError[G <: HttpError](implicit errorMapper: ErrorMapper[E, G]): Future[Either[G, A]] = result.map {
      case Left(error) => Left(errorMapper(error))
      case Right(value) => Right(value)
    }
  }

  implicit val TimestampFormat: Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp] with Decoder[Timestamp] {
    override def apply(a: Timestamp): Json = Encoder.encodeLong.apply(a.getTime)

    override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
  }

}

object Routes extends Directives {

  def extractClaims(claims: Map[String, Any]): (UUID, String) = {
    val connectedPersonId   = UUID.fromString(claims("userId").toString)
    val connectedPersonRole = claims("role").toString
    (connectedPersonId, connectedPersonRole)
  }

  def buildRoutes(dependencies: Dependencies): Route =
    new UserRoutes(dependencies.userService).userRoutes ~
    new HealthRoutes(dependencies.healthService).healthRoutes ~
    new AuthRoutes(dependencies.authService).authRoutes ~
    new AirportRoutes(dependencies.airportService, dependencies.cacheClient).airportRoutes ~
    Swagger.routes

  def buildErrorMapper(serviceErrorMapper: PartialFunction[ServiceError, HttpError]): ErrorMapper[ServiceError, HttpError] =
    (e: ServiceError) =>
      serviceErrorMapper
        .applyOrElse(
          e,
          (se: ServiceError) => InternalErrorHttp(s"Unexpected error ${se}")
        )

}
