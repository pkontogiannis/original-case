package com.klm.service.airport.amadeus

import com.klm.service.airport.TravelAPIModel.{ Airport, Fare }
import com.klm.service.domain.TravelModel.Currency.Currency
import com.klm.service.domain.TravelModel.Language.Language
import com.klm.service.domain.TravelModel.{ Currency, Language }
import com.klm.service.errors.ServiceError

import scala.concurrent.Future

trait TravelAPI {

  def getAirports(
      size: Int            = 1000,
      page: Int            = 1,
      lang: Language       = Language.EN,
      term: Option[String] = None
  )(implicit contextRequestId: String): Future[Either[ServiceError, List[Airport]]]

  def getAirport(code: String, lang: Language = Language.EN)(
      implicit contextRequestId: String
  ): Future[Either[ServiceError, Airport]]

  def getFareOffer(
      originCode: String,
      destinationCode: String,
      currency: Currency = Currency.EUR
  )(implicit contextRequestId: String): Future[Either[ServiceError, Fare]]
}
