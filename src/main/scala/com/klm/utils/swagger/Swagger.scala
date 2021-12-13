package com.klm.utils.swagger

import akka.http.scaladsl.server.Route
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import com.klm.utils.server.Config
import io.swagger.v3.oas.models.security.SecurityScheme

object Swagger extends SwaggerHttpService with Config {

  override val apiClasses: Set[Class[_]] = Set()
  override val host = s"${configuration.serverConfig.host}:${configuration.serverConfig.port}"
  override val apiDocsPath = "api-docs" //where you want the swagger-json endpoint exposed
  override val info: Info = Info(title = "Skeleton API", version = "v01") //provides license and other description details
  override val schemes = List("http")
  override val securitySchemes = Map(
    "bearerAuth" -> new SecurityScheme()
      .in(SecurityScheme.In.HEADER)
      .`type`(SecurityScheme.Type.HTTP)
      .scheme("bearer")
      .bearerFormat("JWT")
  )

  override val unwantedDefinitions =
    Seq(
      "Function1",
      "Function1RequestContextFutureRouteResult"
    )

  override def routes: Route = super.routes ~ get {
    pathPrefix("") {
      pathEndOrSingleSlash {
        getFromResource("swagger-ui/index.html")
      }
    } ~
      getFromResourceDirectory("swagger-ui")
  }

}
