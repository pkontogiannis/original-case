package com.klm.service.airport

import com.klm.service.airport.TravelAPIModel.{ Airport, FareWithInfo }
import com.klm.service.domain.TravelModel.Currency.Currency
import com.klm.service.domain.TravelModel.Language.Language
import com.klm.service.domain.TravelModel.SortByField.SortByField
import com.klm.service.errors.ServiceError

import scala.concurrent.Future

trait AirportService {

  def getAirports(
      size: Int = 100,
      page: Int = 1,
      lang: Language,
      term: Option[String] = None,
      sort: SortByField
  )(implicit contextRequestId: String): Future[Either[ServiceError, List[Airport]]]

  def getAirport(airportCode: String, lang: Language)(implicit contextRequestId: String): Future[Either[ServiceError, Airport]]

  def getFareOffer(
      originCode: String,
      destinationCode: String,
      currency: Currency
  )(implicit contextRequestId: String): Future[Either[ServiceError, FareWithInfo]]

}
