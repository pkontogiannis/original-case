package com.klm.service.auth

import com.klm.service.domain.UserModel.{Token, UserCreate, UserDto, UserLogin, UserLoginDto}
import com.klm.service.errors.DatabaseError
import com.klm.service.errors.ServiceError.AuthenticationError
import com.typesafe.scalalogging.LazyLogging

import java.util.UUID
import scala.concurrent.Future

trait AuthService extends LazyLogging {

  def loginUser(userLogin: UserLogin): Future[Either[AuthenticationError, UserLoginDto]]

  def registerUser(userRegister: UserCreate): Future[Either[DatabaseError, UserDto]]

  def getAccessToken(userId: UUID, role: String): Future[Either[AuthenticationError, Token]]

  def getRefreshToken(userId: UUID, role: String): Future[Either[AuthenticationError, Token]]

}
