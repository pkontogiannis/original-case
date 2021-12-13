package com.klm.utils.jwt

import com.klm.service.errors.ServiceError.AuthenticationError
import pdi.jwt.JwtClaim

import java.util.UUID
import scala.util.{ Failure, Success, Try }

case class Claims(subject: UUID, issuedAt: Long, expires: Long, issuer: String)

object Claims {
  def apply(jwtClaim: JwtClaim): Either[AuthenticationError, Claims] =
    Try(new Claims(UUID.fromString(jwtClaim.subject.get), jwtClaim.issuedAt.get, jwtClaim.expiration.get, jwtClaim.issuer.get)) match {
      case Failure(_) => Left(AuthenticationError())
      case Success(value) => Right(value)
    }
}
