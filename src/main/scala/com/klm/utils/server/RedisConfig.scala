package com.klm.utils.server
import com.typesafe.config.{ Config => TSConfig }

case class RedisConfig(host: String, port: Int)

object RedisConfig {

  def apply(config: TSConfig): RedisConfig =
    RedisConfig(
      config.getString("redis.host"),
      config.getInt("redis.port")
    )
}
