package com.klm.service.user

import com.klm.service.domain.UserModel
import com.klm.service.domain.UserModel.{UpdateUser, UserCreate, UserDto}
import com.klm.service.errors.ServiceError.{GenericDatabaseError, MethodNotAllowed}
import com.klm.service.errors.{DatabaseError, ServiceError}
import com.klm.service.user.persistence.UserPersistence

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserServiceDefault(val userPersistence: UserPersistence) extends UserService {

  def getUsers: Future[Either[DatabaseError, List[UserDto]]] =
    userPersistence.getUsers.map {
      case Right(value) =>
        logger.info(
          s"[${this.getClass.getSimpleName}] successfully retrieve a list of users with uuid: ${value.map(us => us.userId).mkString(", ")}"
        )
        Right(value.map(user => UserModel.userToUserDto(user)))
      case Left(_) => Left(GenericDatabaseError)
    }

  def getUser(userId: UUID): Future[Either[DatabaseError, UserDto]] =
    userPersistence.getUser(userId).map {
      case Right(value) =>
        logger.info(s"[${this.getClass.getSimpleName}] successfully retrieve a user with uuid: ${value.userId}")
        Right(UserModel.userToUserDto(value))
      case Left(error) => Left(error)
    }

  def createUser(userCreate: UserCreate): Future[Either[DatabaseError, UserDto]] =
    userPersistence.createUser(userCreate).map {
      case Right(value) =>
        logger.info(s"[${this.getClass.getSimpleName}] successfully created a user with uuid: ${value.userId}")
        Right(UserModel.userToUserDto(value))
      case Left(error) =>
        Left(error)
    }

  def updateUser(userId: UUID, updateUser: UpdateUser): Future[Either[DatabaseError, UserDto]] =
    userPersistence.updateUser(userId, updateUser).map {
      case Right(value) =>
        logger.info(s"[${this.getClass.getSimpleName}] successfully update a user with uuid: ${value.userId}")
        Right(UserModel.userToUserDto(value))
      case Left(error) => Left(error)
    }

  def updateUserPartially(userId: UUID, updateUser: UpdateUser): Future[Either[DatabaseError, UserDto]] =
    userPersistence.updateUserPartially(userId, updateUser).map {
      case Right(value) =>
        logger.info(s"[${this.getClass.getSimpleName}] successfully partially update a user with uuid: ${value.userId}")
        Right(UserModel.userToUserDto(value))
      case Left(error) => Left(error)
    }

  def deleteUser(userId: UUID, connectedUserId: UUID): Future[Either[ServiceError, Boolean]] =
    if (userId == connectedUserId)
      Future.successful(Left(MethodNotAllowed("User cannot delete himself")))
    else
      userPersistence.deleteUser(userId).map {
        case Right(value) =>
          logger.info(s"[${this.getClass.getSimpleName}] successfully delete a user with uuid: $userId")
          Right(value)
        case Left(error) => Left(error)
      }

  def deleteAllUsers(): Future[Either[DatabaseError, Boolean]] =
    userPersistence.deleteAllUsers().map {
      case Right(value) =>
        logger.info(s"[${this.getClass.getSimpleName}] successfully delete all users")
        Right(value)
      case Left(error) => Left(error)
    }

}
