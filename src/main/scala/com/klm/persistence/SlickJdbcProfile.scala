package com.klm.persistence

import akka.actor.ActorSystem
import com.klm.utils.database.DBAccess
import slick.jdbc.JdbcProfile


trait SlickJdbcProfile extends UUIDColumns{
  lazy val dbAccess: DBAccess   = DBAccess(actorSystem)
  lazy val profile: JdbcProfile = slick.jdbc.PostgresProfile
  val actorSystem: ActorSystem
}
