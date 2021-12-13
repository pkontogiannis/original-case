package com.klm.service.airport.cache

import com.klm.service.errors.ServiceError
import com.klm.service.errors.ServiceError.CacheServiceError

import scala.concurrent.Future

trait CacheClient {
  def loadFromCache(key: String)(implicit contextRequestId: String): Future[Either[CacheServiceError, Option[String]]]
  def addInCache(key: String, payload: String)(implicit contextRequestId: String): Future[Either[ServiceError, Boolean]]
  def loadMetricFromCache(key: String): Future[Either[CacheServiceError, Option[Long]]]
  def addMetricInCache(key: String, payload: Long): Future[Either[ServiceError, Boolean]]
  def updateMinMaxDuration(duration: Long): Unit
}
