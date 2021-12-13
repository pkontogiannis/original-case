package com.klm.persistence

import slick.jdbc.{GetResult, PositionedParameters, PositionedResult, SetParameter}

import java.sql.JDBCType
import java.util.UUID

trait UUIDColumns {

  def mkGetResult[T](next: (PositionedResult => T)): GetResult[T] =
    (rs: PositionedResult) => next(rs)

  implicit class PgUuidPositionedResult(r: PositionedResult) {
    def nextUUID: UUID = r.nextObject().asInstanceOf[UUID]

    def nextUUIDOption: Option[UUID] =
      r.nextObjectOption().map(_.asInstanceOf[UUID])
  }

  implicit val getResultUuid: GetResult[UUID] = mkGetResult(_.nextUUID)
  implicit val getResultUuidOption: GetResult[Option[UUID]] =
    mkGetResult(_.nextUUIDOption)
  implicit object SetUuid extends SetParameter[UUID] {
    override def apply(u: UUID, pp: PositionedParameters): Unit =
      pp.setObject(u, JDBCType.OTHER.getVendorTypeNumber)
  }

  implicit object SetUuidOption extends SetParameter[Option[UUID]] {
    override def apply(u: Option[UUID], pp: PositionedParameters): Unit =
      pp.setObjectOption(u, JDBCType.OTHER.getVendorTypeNumber)
  }

}
