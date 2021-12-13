package com.klm.persistence

import com.klm.persistence.tables.UserTableDef
import slick.lifted.TableQuery

trait Schema
    extends SlickJdbcProfile
    with UserTableDef {

  val Users: TableQuery[UserTable]                     = TableQuery[UserTable]
}
