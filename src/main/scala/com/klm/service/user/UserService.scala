package com.klm.service.user

import com.klm.service.domain.UserModel.{ UpdateUser, UserCreate, UserDto }
import com.klm.service.errors.{ DatabaseError, ServiceError }
import com.typesafe.scalalogging.LazyLogging

import java.util.UUID
import scala.concurrent.Future

trait UserService extends LazyLogging {

  def getUsers: Future[Either[DatabaseError, List[UserDto]]]

  def getUser(userId: UUID): Future[Either[DatabaseError, UserDto]]

  def createUser(userCreate: UserCreate): Future[Either[DatabaseError, UserDto]]

  def updateUser(userId: UUID, updateUser: UpdateUser): Future[Either[DatabaseError, UserDto]]

  def updateUserPartially(userId: UUID, updateUser: UpdateUser): Future[Either[DatabaseError, UserDto]]

  def deleteUser(userId: UUID, connectedUserId: UUID): Future[Either[ServiceError, Boolean]]

  def deleteAllUsers(): Future[Either[DatabaseError, Boolean]]

}
