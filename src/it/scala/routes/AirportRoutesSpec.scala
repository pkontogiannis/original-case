package routes
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.klm.service.airport.AirportRoutes
import com.klm.service.airport.TravelAPIModel.Airport
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import routes.helpers.ServiceSuite

class AirportRoutesSpec extends ServiceSuite {

  trait Fixture {
    val airportRoutes: Route = new AirportRoutes(dependencies.airportService, dependencies.cacheClient).airportRoutes
  }

  "Airport routes" should {

    "successfully retrieve an airport" in new Fixture {
      wiremock.stubFor(
        WireMock
          .get("http://localhost:8080/airports/JFK")
          .willReturn(
            aResponse()
              .withBody(
                """{"code":"JFK","name":"John F. Kennedy International","description":"New York - John F. Kennedy International (JFK), USA","coordinates":{"latitude":40.63861,"longitude":-73.77694},"parent":{"code":"NYC","name":"New York","description":"New York (NYC)","coordinates":{"latitude":40.69694,"longitude":-73.94},"parent":{"code":"US","name":"USA","description":"USA (US)","coordinates":{"latitude":36.0,"longitude":-100.0},"parent":{"code":"US","name":"USA","description":"USA (US)","coordinates":{"latitude":36.0,"longitude":-100.0}}}}}"""
              )
              .withHeader("Content-Type", "application/json")
              .withStatus(200)
          )
      )
      wiremock.start()

      val expectedAirport: Airport = Airport(
        name        = "John F. Kennedy International",
        code        = "JFK",
        description = "New York - John F. Kennedy International (JFK), USA"
      )

      Get(s"/api/v01/travel/airports/${expectedAirport.code}") ~> airportRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.OK)
        val resultAirport: Airport = responseAs[Airport]
        assert(
          resultAirport === expectedAirport
        )
      }
    }

    "successfully return 404 for an invalid airport" in new Fixture {
      Get(s"/api/v01/travel/airports/DummyValue") ~> airportRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.NotFound)
      }
    }

    "successfully retrieve list of airport" in new Fixture {

      wiremock.stubFor(
        WireMock
          .get("http://localhost:8080/airports?size=1&page=2&lang=nl")
          .willReturn(
            aResponse()
              .withBody(
                """{"_embedded":{"locations":[{"code":"YOW","name":"Ottawa International","description":"Ottawa - Ottawa International (YOW), Canada","coordinates":{"latitude":45.32083,"longitude":-75.67278},"parent":{"code":"YOW","name":"Ottawa","description":"Ottawa (YOW)","coordinates":{"latitude":45.33222,"longitude":-75.68194},"parent":{"code":"CA","name":"Canada","description":"Canada (CA)","coordinates":{"latitude":55.0,"longitude":-104.0},"parent":{"code":"CA","name":"Canada","description":"Canada (CA)","coordinates":{"latitude":55.0,"longitude":-104.0}}}}}]},"page":{"size":1,"totalElements":1048,"totalPages":1048,"number":2}}"""
              )
              .withHeader("Content-Type", "application/json")
              .withStatus(200)
          )
      )
      wiremock.start()

      val expectedAirport: Airport = Airport(
        name        = "John F. Kennedy International",
        code        = "JFK",
        description = "New York - John F. Kennedy International (JFK), USA"
      )

      Get(s"/api/v01/travel/airports?size=5&page=2&lang=nl") ~> airportRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.OK)
      }
    }

  }
}
