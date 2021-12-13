package com.klm.persistence.tables

import com.klm.persistence.SlickJdbcProfile
import com.klm.service.domain.UserModel.User
import slick.lifted.ProvenShape

import java.util.UUID

trait UserTableDef {
  self: SlickJdbcProfile =>

  import profile.api._

  class UserTable(tag: Tag) extends Table[User](tag, Some("klm"), "user") {

    def * : ProvenShape[User] =
      (
        id.?,
        userId,
        email,
        password,
        firstName,
        lastName,
        role
      ) <> ((User.apply _).tupled, User.unapply)

    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def userId: Rep[UUID] = column[UUID]("user_uuid", O.Unique, O.Default(UUID.randomUUID()))

    def email: Rep[String] = column[String]("email", O.Unique)

    def password: Rep[String] = column[String]("password")

    def firstName: Rep[String] = column[String]("first_name")

    def lastName: Rep[String] = column[String]("last_name")

    def role: Rep[String] = column[String]("role")
  }

}
