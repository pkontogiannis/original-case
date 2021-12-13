package com.klm.utils.server

import com.klm.utils.config.Configuration

trait Config {

  val configuration: Configuration = Configuration.default
  val dbConfig: DatabaseConfig     = configuration.databaseConfig
  val redisConfig: RedisConfig     = configuration.redisConfig

}
