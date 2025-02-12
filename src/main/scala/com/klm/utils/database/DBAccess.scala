package com.klm.utils.database

import akka.actor.ActorSystem
import com.klm.persistence.Schema
import slick.jdbc.PostgresProfile.api._

case class DBAccess(actorSystem: ActorSystem) extends Schema {

  val db: Database = Database.forConfig("database")

  /** Only for integration tests */
  //  private val tables = List(Persons)

  //  private val existing: Future[Vector[MTable]] = db.run(MTable.getTables)
  //  private val f = existing.flatMap(v => {
  //    val names = v.map(mt => mt.name.name)
  //    val createIfNotExist = tables.filter(table =>
  //      !names.contains(table.baseTableRow.tableName)).map(_.schema.create)
  //    db.run(DBIO.sequence(createIfNotExist))
  //  })
  //  Await.result(f, Duration.Inf)

}
