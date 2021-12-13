package com.klm.service.user.persistence

import com.klm.service.domain.UserModel.{UpdateUser, User, UserCreate}
import com.klm.service.errors.DatabaseError
import com.typesafe.scalalogging.LazyLogging

import java.util.UUID
import scala.concurrent.Future

trait UserPersistence extends LazyLogging {

  def getUsers: Future[Either[DatabaseError, List[User]]]

  def getUser(userId: UUID): Future[Either[DatabaseError, User]]

  def createUser(data: UserCreate): Future[Either[DatabaseError, User]]

  def updateUser(userId: UUID, updateUser: UpdateUser): Future[Either[DatabaseError, User]]

  def updateUserPartially(userId: UUID, updateUser: UpdateUser): Future[Either[DatabaseError, User]]

  def deleteUser(userId: UUID): Future[Either[DatabaseError, Boolean]]

  def loginUser(email: String, password: String): Future[Either[DatabaseError, User]]

  def deleteAllUsers(): Future[Either[DatabaseError, Boolean]]

}
