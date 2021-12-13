package com.klm.service.health

import com.klm.service.health.HealthModel.HealthStatus
import com.typesafe.scalalogging.LazyLogging

trait HealthService extends LazyLogging {

  def ready: HealthStatus

}
