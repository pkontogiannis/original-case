package com.klm.service.airport

import com.klm.service.domain.TravelModel.Currency
import com.klm.service.domain.TravelModel.Currency.Currency
import io.circe.{ Decoder, Encoder, HCursor, Json }

import scala.language.implicitConversions

object TravelAPIModel {

  case class Airport(code: String, name: String, description: String)
  object Airport {
    implicit val airportDecoder: Decoder[Airport] = (c: HCursor) =>
      for {
        code <- c.downField("code").as[String]
        name <- c.downField("name").as[String]
        description <- c.downField("description").as[String]
      } yield {
        new Airport(code, name, description)
      }
    implicit val airportsDecoder: Decoder[List[Airport]] = (c: HCursor) =>
      for {
        airports <- c.downField("_embedded").downField("locations").as[Seq[Airport]]
      } yield {
        airports.toList
      }
  }

  case class Fare(originCode: String, destinationCode: String, amount: Double, currency: Currency)
  object Fare {
    implicit val fareDecoder: Decoder[Fare] = (c: HCursor) =>
      for {
        originCode <- c.downField("origin").as[String]
        destinationCode <- c.downField("destination").as[String]
        currency <- c.downField("currency").as[String]
        amount <- c.downField("amount").as[Double]
      } yield Fare(
        originCode,
        destinationCode,
        amount,
        Currency.withNameWithDefault(currency)
      )

    implicit val fareEncoder: Encoder[Fare] = (fare: Fare) =>
      Json.obj(
        ("originCode", Json.fromString(fare.originCode)),
        ("destinationCode", Json.fromString(fare.destinationCode)),
        ("amount", Json.fromDouble(fare.amount).get),
        ("currency", Json.fromString(fare.currency.toString))
      )

  }

  case class FareWithInfo(originCode: Airport, destinationCode: Airport, amount: Double, currency: Currency)
  object FareWithInfo {
    implicit val fareWithInfoEncoder: Encoder[FareWithInfo] = (fareWithInfo: FareWithInfo) =>
      Json.obj(
        (
          "originAirport",
          Json.obj(
            ("code", Json.fromString(fareWithInfo.originCode.code)),
            ("name", Json.fromString(fareWithInfo.originCode.name)),
            ("description", Json.fromString(fareWithInfo.originCode.description))
          )
        ),
        (
          "destinationAirport",
          Json.obj(
            ("code", Json.fromString(fareWithInfo.destinationCode.code)),
            ("name", Json.fromString(fareWithInfo.destinationCode.name)),
            ("description", Json.fromString(fareWithInfo.destinationCode.description))
          )
        ),
        ("amount", Json.fromDouble(fareWithInfo.amount).get),
        ("currency", Json.fromString(fareWithInfo.currency.toString))
      )
  }
}
