package com.klm.service.auth

import com.klm.service.domain.UserModel
import com.klm.service.domain.UserModel.{Token, UserCreate, UserDto, UserLogin, UserLoginDto}
import com.klm.service.errors.DatabaseError
import com.klm.service.errors.ServiceError.AuthenticationError
import com.klm.service.user.persistence.UserPersistence
import com.klm.utils.jwt.JWTUtils

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthServiceDefault(val userPersistence: UserPersistence) extends AuthService {

  def loginUser(userLogin: UserLogin): Future[Either[AuthenticationError, UserLoginDto]] =
    userPersistence.loginUser(userLogin.email, userLogin.password).map {
      case Right(user) =>
        val refreshToken = JWTUtils.getRefreshToken(user.userId, user.role)
        val accessToken  = JWTUtils.getAccessToken(user.userId, user.role)
        logger.info(s"[${this.getClass.getSimpleName}] successfully login user with uuid: ${user.userId}")
        Right(
          UserLoginDto(
            user.email,
            accessToken,
            refreshToken,
            user.role,
            "Bearer"
          )
        )
      case Left(_) =>
        logger.info(s"[${this.getClass.getSimpleName}] failed try login user with email: ${userLogin.email}")
        Left(AuthenticationError())
    }

  def registerUser(userRegister: UserCreate): Future[Either[DatabaseError, UserDto]] =
    userPersistence.createUser(userRegister).map {
      case Right(value) =>
        logger.info(s"[${this.getClass.getSimpleName}] successfully created a user with uuid: ${value.userId}")
        Right(UserModel.userToUserDto(value))
      case Left(error) =>
        Left(error)
    }

  def getAccessToken(userId: UUID, role: String): Future[Either[AuthenticationError, Token]] =
    Future(
      Right(
        JWTUtils.getAccessToken(userId, role)
      )
    )

  def getRefreshToken(userId: UUID, role: String): Future[Either[AuthenticationError, Token]] =
    userPersistence.getUser(userId).map {
      case Left(_) => Left(AuthenticationError())
      case Right(_) =>
        Right(
          JWTUtils.getRefreshToken(userId, role)
        )
    }

}
