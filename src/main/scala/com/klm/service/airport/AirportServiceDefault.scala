package com.klm.service.airport

import cats.data.EitherT
import com.klm.service.airport.TravelAPIModel.{ Airport, FareWithInfo }
import com.klm.service.airport.amadeus.TravelAPI
import com.klm.service.domain.TravelModel.Currency.Currency
import com.klm.service.domain.TravelModel.Language.Language
import com.klm.service.domain.TravelModel.SortByField.SortByField
import com.klm.service.domain.TravelModel.{ Language, SortByField }
import com.klm.service.errors.ServiceError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

class AirportServiceDefault(val travelAPI: TravelAPI) extends AirportService {

  def getAirports(
      size: Int = 100,
      page: Int = 1,
      lang: Language,
      term: Option[String] = None,
      sort: SortByField
  )(implicit contextRequestId: String): Future[Either[ServiceError, List[Airport]]] =
    (for {
      listOfAirports <- EitherT(travelAPI.getAirports(size, page, lang, term))
      sortedAirports = {
        if (sort == SortByField.none) { listOfAirports }
        else
          listOfAirports.sortBy(airport =>
            sort match {
              case SortByField.name => airport.name
              case SortByField.code => airport.code
            }
          )
      }
    } yield sortedAirports).value

  def getAirport(airportCode: String, lang: Language = Language.EN)(
      implicit contextRequestId: String
  ): Future[Either[ServiceError, Airport]] =
    travelAPI.getAirport(airportCode, lang)

  def getFareOffer(
      originCode: String,
      destinationCode: String,
      currency: Currency
  )(implicit contextRequestId: String): Future[Either[ServiceError, FareWithInfo]] = {
    val result = for {
      originAirport <- EitherT(getAirport(originCode))
      destinationAirport <- EitherT(getAirport(destinationCode))
      fare <- EitherT(travelAPI.getFareOffer(originCode, destinationCode, currency))
      combinedResults = FareWithInfo(
        originAirport,
        destinationAirport,
        fare.amount,
        fare.currency
      )
    } yield combinedResults
    result.value
  }
}
