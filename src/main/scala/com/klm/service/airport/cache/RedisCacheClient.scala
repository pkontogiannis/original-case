package com.klm.service.airport.cache

import akka.actor.ActorSystem
import akka.pattern.{ CircuitBreaker, CircuitBreakerOpenException }
import com.klm.service.errors.ServiceError
import com.klm.service.errors.ServiceError.CacheServiceError
import com.redis.RedisClient
import com.typesafe.scalalogging.LazyLogging

import scala.async.Async.async
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class RedisCacheClient(val client: RedisClient)(implicit system: ActorSystem) extends CacheClient with LazyLogging {

  val cacheBreaker: CircuitBreaker =
    new CircuitBreaker(system.scheduler, maxFailures = 3, callTimeout = 10.seconds, resetTimeout = 0.5.minute)
      .onOpen(notifyMeOnOpen())

  def loadFromCache(key: String)(implicit contextRequestId: String): Future[Either[CacheServiceError, Option[String]]] = {
    logThreadAsyncTask(s"loadFromCache")
    logger.info(s"RequestID: $contextRequestId - Results are loaded from cache for the following $key.")
    val withBreaker: Future[Option[String]] =
      cacheBreaker.withCircuitBreaker(Future(client.get(key)))
    val result: Future[Either[CacheServiceError, Option[String]]] =
      withBreaker.flatMap(res => Future.successful(Right(res))).recoverWith {
        case _: CircuitBreakerOpenException =>
          Future.successful(Left(CacheServiceError("Redis is unavailable")))
        case e: Exception =>
          print(e.getMessage)
          Future.successful(Right(None))
      }
    result
  }

  def addInCache(key: String, payload: String)(implicit contextRequestId: String): Future[Either[ServiceError, Boolean]] = {
    logThreadAsyncTask(s"addInCache")

    logger.info(s"RequestID: $contextRequestId - Add the following $key in the cache.")

    async {
      logThreadAsyncTask(s"addInCache($key)")
      val withBreaker: Future[Boolean] =
        cacheBreaker.withCircuitBreaker(Future(client.set(key = key, value = payload, expire = 1.days)))
      withBreaker
        .flatMap(res => Future.successful(Right(res)))
        .recoverWith {
          case _: CircuitBreakerOpenException =>
            Future.successful(Left(CacheServiceError("Redis is unavailable")))
        }
    }.flatten
  }

  def notifyMeOnOpen(): Unit =
    logger.warn("My CircuitBreaker is now open, and will not close for one minute")

  def logThreadAsyncTask(taskName: String): Unit = {
    val threadId = Thread.currentThread.getId
    logger.info(s"Task $taskName executed by async-task-$threadId")
  }

  def updateMinMaxDuration(duration: Long): Unit =
    async {
      val minKey                           = "http://klm.amadeus.requests.min"
      val maxKey                           = "http://klm.amadeus.requests.max"
      val oldMinimumDuration: Option[Long] = client.get(minKey).map(_.toLong)
      val oldMaximumDuration: Option[Long] = client.get(maxKey).map(_.toLong)

      oldMinimumDuration match {
        case Some(value) =>
          if (value < duration) {
            client.set(key = minKey, value = duration, expire = 1.days)
          }
        case None =>
          client.set(key = minKey, value = duration, expire = 1.days)
      }
      oldMaximumDuration match {
        case Some(value) =>
          if (value < duration) {
            client.set(key = maxKey, value = duration, expire = 1.days)
          }
        case None =>
          client.set(key = maxKey, value = duration, expire = 1.days)
      }
    }

  def loadMetricFromCache(key: String): Future[Either[CacheServiceError, Option[Long]]] = {
    val withBreaker: Future[Option[String]] =
      cacheBreaker.withCircuitBreaker(Future(client.get(key)))

    val result: Future[Either[CacheServiceError, Option[Long]]] =
      withBreaker.flatMap(res => Future.successful(Right(res.map(_.toLong)))).recoverWith {
        case _: CircuitBreakerOpenException =>
          Future.successful(Left(CacheServiceError("Redis is unavailable")))
      }
    result
  }

  def addMetricInCache(key: String, payload: Long): Future[Either[ServiceError, Boolean]] =
    async {
      val withBreaker: Future[Boolean] =
        cacheBreaker.withCircuitBreaker(Future(client.set(key = key, value = payload, expire = 1.days)))
      withBreaker
        .flatMap(res => Future.successful(Right(res)))
        .recoverWith {
          case _: CircuitBreakerOpenException =>
            Future.successful(Left(CacheServiceError("Redis is unavailable")))
        }
    }.flatten

}
