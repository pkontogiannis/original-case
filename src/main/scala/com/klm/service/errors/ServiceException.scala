package com.klm.service.errors

sealed trait ServiceException extends Exception

trait DatabaseException extends ServiceException

object ServiceException {

  case class NotEnoughResources(message: String) extends DatabaseException

  case class RecordNotFound(message: String) extends DatabaseException

  case class RecordsNotFound(message: String) extends DatabaseException

}
