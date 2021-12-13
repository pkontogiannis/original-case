package com.klm.service.errors

import com.klm.service.errors.ServiceError.AuthenticationError

trait ErrorMapper[-FromError <: ServiceError, +ToError <: HttpError] extends (FromError => ToError)

object ErrorMapper {

  implicit val toHttpError: ErrorMapper[AuthenticationError, UnauthorizedErrorHttp] = { _: ServiceError.AuthenticationError =>
    UnauthorizedErrorHttp()
  }
}
