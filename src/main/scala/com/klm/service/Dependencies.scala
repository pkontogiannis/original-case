package com.klm.service

import akka.actor.ActorSystem
import com.klm.Main.redisConfig
import com.klm.service.airport.amadeus.AmadeusAPI
import com.klm.service.airport.cache.{ CacheClient, RedisCacheClient }
import com.klm.service.airport.{ AirportService, AirportServiceDefault }
import com.klm.service.auth.{ AuthService, AuthServiceDefault }
import com.klm.service.health.{ HealthService, HealthServiceDefault }
import com.klm.service.user.persistence.UserPersistenceSQL
import com.klm.service.user.{ UserService, UserServiceDefault }
import com.klm.utils.database.DBAccess
import com.redis.RedisClient

case class Dependencies(
    dbAccess: DBAccess,
    healthService: HealthService,
    userService: UserService,
    authService: AuthService,
    airportService: AirportService,
    cacheClient: CacheClient
)

object Dependencies {

  def fromConfig(implicit system: ActorSystem): Dependencies = {

    val dbAccess = DBAccess(system)

    val userPersistence = new UserPersistenceSQL(dbAccess)
    val healthService   = new HealthServiceDefault(dbAccess)
    val userService     = new UserServiceDefault(userPersistence)
    val authService     = new AuthServiceDefault(userPersistence)
    val redisClient     = new RedisClient(redisConfig.host, redisConfig.port)
    val cacheClient     = new RedisCacheClient(redisClient)
    val travelAPI       = new AmadeusAPI(cacheClient)
    val airportService  = new AirportServiceDefault(travelAPI)

    Dependencies(
      dbAccess       = dbAccess,
      healthService  = healthService,
      userService    = userService,
      authService    = authService,
      airportService = airportService,
      cacheClient    = cacheClient
    )
  }
}
