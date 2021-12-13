package com.klm.service.errors

import com.klm.service.errors.ServiceError.{ GenericDatabaseError, RecordNotFound }

sealed trait ServiceError

trait DatabaseError extends ServiceError

object DatabaseError {
  implicit val ToHttpErrorMapper: ErrorMapper[DatabaseError, HttpError] = {
    case GenericDatabaseError =>
      InternalErrorHttp("Unexpected error")
    case RecordNotFound() =>
      DefaultNotFoundErrorHttp
  }
}

object ServiceError {

  val httpErrorMapper: PartialFunction[ServiceError, HttpError] = {
    case RecordAlreadyExists => new RecordAlreadyExists()
    case RecordNotFound() => DefaultNotFoundErrorHttp
    case MethodNotAllowed(message) => MethodNotAllowedErrorHttp(message)
    case AuthenticationError() => UnauthorizedErrorHttp()
    case GenericDatabaseError => InternalErrorHttp("Unexpected error")
    case ServiceUnavailable(message) => ServiceUnavailableHttp(message)
    case InsertModeIsNotDefined(mode) =>
      BadRequestErrorHttp(s"Unable to insert tasks with mode, $mode")
  }

  case class CacheServiceError(message: String) extends ServiceError
  case class InvalidJSONError(message: String) extends ServiceError
  case class DecodeJSONError(message: String) extends ServiceError
  case class ClientServiceError(message: String) extends ServiceError
  case class ServiceUnavailable(message: String) extends ServiceError
  case class AuthenticationError() extends ServiceError
  case class InsertModeIsNotDefined(view: String) extends ServiceError
  case class MethodNotAllowed(message: String) extends ServiceError
  case object GenericDatabaseError extends DatabaseError
  case class RecordNotFound() extends DatabaseError
  case object RecordAlreadyExists extends DatabaseError

}
