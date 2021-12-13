package com.klm.utils.config

import com.klm.utils.server.{ DatabaseConfig, RedisConfig, ServerConfig }
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

case class Configuration(
    initializationTimeoutConfiguration: FiniteDuration,
    serverConfig: ServerConfig,
    databaseConfig: DatabaseConfig,
    redisConfig: RedisConfig
)

object Configuration extends ConfigurationFunctions {

  case class ClientConfig(name: String, host: String, port: Int)

}

trait ConfigurationFunctions {

  def default: Configuration = {
    val config = ConfigFactory.load()

    new Configuration(
      FiniteDuration.apply(
        config.getDuration("server.initialization-timeout").toMillis,
        MILLISECONDS
      ),
      ServerConfig(config),
      DatabaseConfig(config),
      RedisConfig(config)
    )
  }
}
