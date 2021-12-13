package com.klm.service.domain

object TravelModel {

  object Currency extends Enumeration {
    type Currency = Value
    val EUR, USD, Unknown = Value

    def withNameWithDefault(name: String): Value =
      values.find(_.toString.toLowerCase() == name.toLowerCase()).getOrElse(Unknown)
  }

  object Language extends Enumeration {
    type Language = Value
    val EN, NL, Unknown = Value

    def withNameWithDefault(name: String): Value =
      values.find(_.toString.toLowerCase() == name.toLowerCase()).getOrElse(Unknown)
  }

  object SortByField extends Enumeration {
    type SortByField = Value
    val name, code, none = Value

    def withNameWithDefault(name: String): Value =
      values.find(_.toString.toLowerCase() == name.toLowerCase()).getOrElse(none)
  }
}
