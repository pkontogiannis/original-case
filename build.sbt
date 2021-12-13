name := "klm-cs"

version := "0.1"

scalaVersion := "2.13.2"

lazy val akkaHttpVersion       = "10.2.1"
lazy val akkaVersion           = "2.6.16"
lazy val scalaTestVersion      = "3.2.2"
lazy val argonautVersion       = "6.2.5"
lazy val slickVersion          = "3.3.3"
lazy val flywayVersion         = "8.2.0"
lazy val jwtVersion            = "5.0.0"
lazy val circeVersion          = "0.13.0"
lazy val circeJsonSchema       = "0.2.0"
lazy val circeExtra            = "1.35.2"
lazy val h2Version             = "1.4.200"
lazy val catsVersion           = "2.7.0"
lazy val scalaCheck            = "1.15.4"
lazy val postgresVersion       = "42.3.1"
lazy val logbackClassicVersion = "1.2.7"
lazy val scalaLoggingVersion   = "3.9.4"
lazy val logbackEncoderVersion = "7.0.1"

lazy val akkaHttpSwaggerVersion  = "2.6.0"
lazy val akkaScalaSwaggerVersion = "2.5.2"
lazy val swaggerVersion          = "2.1.11"
lazy val jaxRSVersion            = "2.1.1"
lazy val akkaHttpCorsVersion     = "1.1.2"
lazy val swaggerUiVersion        = "1.4.0"
lazy val slickPGVersion          = "0.19.4"
lazy val sttpVersion             = "3.3.18"
lazy val redisClientVersion      = "3.20"
lazy val kamonVersion            = "2.4.2"

scalacOptions += "-deprecation"

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka"      %% "akka-http"           % akkaHttpVersion,
    "com.typesafe.akka"      %% "akka-stream"         % akkaVersion,
    "com.typesafe.akka"      %% "akka-http-testkit"   % akkaHttpVersion % "it,test",
    "com.typesafe.akka"      %% "akka-testkit"        % akkaVersion % "it,test",
    "com.typesafe.akka"      %% "akka-stream-testkit" % akkaVersion % "it,test",
    "org.scala-lang.modules" %% "scala-async"         % "0.10.0",
    "org.scala-lang"         % "scala-reflect"        % scalaVersion.value % Provided,
    // Scala Test
    "org.scalatest"  %% "scalatest"  % scalaTestVersion % "it,test",
    "org.scalacheck" %% "scalacheck" % scalaCheck,
    // JSON Serialization Library
    "io.circe"          %% "circe-core"        % circeVersion,
    "io.circe"          %% "circe-generic"     % circeVersion,
    "io.circe"          %% "circe-parser"      % circeVersion,
    "io.circe"          %% "circe-json-schema" % circeJsonSchema,
    "io.circe"          %% "circe-literal"     % circeVersion,
    "de.heikoseeberger" %% "akka-http-circe"   % circeExtra,
    // Migration of SQL Databases
    "org.flywaydb" % "flyway-core" % flywayVersion,
    // ORM
    "com.typesafe.slick" %% "slick"          % slickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
    "org.postgresql"     % "postgresql"      % postgresVersion,
    // Logging dependencies
    "com.typesafe.scala-logging" %% "scala-logging"           % scalaLoggingVersion,
    "ch.qos.logback"             % "logback-classic"          % logbackClassicVersion,
    "ch.qos.logback"             % "logback-access"           % logbackClassicVersion,
    "net.logstash.logback"       % "logstash-logback-encoder" % logbackEncoderVersion,
    // Monitoring
    "io.kamon" %% "kamon-prometheus" % kamonVersion,
    "io.kamon" %% "kamon-bundle"     % kamonVersion,
    "io.kamon" %% "kamon-akka-http"  % kamonVersion,
    "io.kamon" %% "kamon-jdbc"       % kamonVersion,
    //Wiremock
    "com.github.tomakehurst" % "wiremock-jre8" % "2.32.0" % Test,
    //JWT Dependencies
    "com.pauldijou" %% "jwt-core"  % jwtVersion,
    "com.pauldijou" %% "jwt-circe" % jwtVersion,
    "org.typelevel" %% "cats-core" % catsVersion,
    // 3rd party calls
    "com.softwaremill.sttp.client3" %% "core"              % sttpVersion,
    "com.softwaremill.sttp.client3" %% "akka-http-backend" % sttpVersion,
    "com.softwaremill.sttp.client3" %% "circe"             % sttpVersion,
    // Swagger dependencies
    "ch.megard"                    %% "akka-http-cors"       % akkaHttpCorsVersion,
    "javax.ws.rs"                  % "javax.ws.rs-api"       % jaxRSVersion,
    "com.github.swagger-akka-http" %% "swagger-akka-http"    % akkaHttpSwaggerVersion,
    "com.github.swagger-akka-http" %% "swagger-scala-module" % akkaScalaSwaggerVersion,
    "io.swagger.core.v3"           % "swagger-core"          % swaggerVersion,
    "io.swagger.core.v3"           % "swagger-annotations"   % swaggerVersion,
    "io.swagger.core.v3"           % "swagger-models"        % swaggerVersion,
    "io.swagger.core.v3"           % "swagger-jaxrs2"        % swaggerVersion,
    "co.pragmati"                  %% "swagger-ui-akka-http" % swaggerUiVersion,
    "com.github.tminglei"          %% "slick-pg"             % slickPGVersion,
    "net.debasishg"                %% "redisclient"          % redisClientVersion
  )
}

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    resolvers += Resolver.bintrayRepo("unisay", "maven"),
    resolvers += "jitpack".at("https://jitpack.io"),
    Defaults.itSettings,
    parallelExecution in ThisBuild := false
  )

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

//mainClass in (Compile, run) := Some("com.klm.Main")
mainClass := Some("com.klm.Main")

// *****************************************************************************
// Aliases
// *****************************************************************************

// SBT aliases to run multiple commands in a single call
//   Optionally add it:scalastyle if the project has integration tests
addCommandAlias(
  "styleCheck",
  "; scalafmtCheck ; scalastyle ; it:scalastyle"
)

// Run tests with coverage, optionally add 'it:test' if the project has
// integration tests
addCommandAlias(
  "testCoverage",
  "; coverage ; it:test ; coverageReport"
)

// Alias to run all SBT commands that are connected with quality assurance
addCommandAlias(
  "qa",
  "; styleCheck ; dependencyUpdates ; testCoverage"
)
