package com.klm.service

import akka.http.scaladsl.server.Directive
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import java.util.UUID

trait ExtraRoutes extends FailFastCirceSupport {
  def withRequestId: Directive[Tuple1[String]] = Directive[Tuple1[String]] { inner => ctx =>
//    Kamon.counter("klm.amadeus.requests.total").withoutTags().increment()
    val uniqueId = UUID.randomUUID().toString
    inner(Tuple1(uniqueId))(ctx)
  }

}
