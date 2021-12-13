lazy val sbtNativePackagerVersion = "1.9.7"
lazy val sbtRevolverVersion       = "0.9.1"
lazy val sbtScalafmtVersion       = "2.4.5"
lazy val sbtUpdatesVersion        = "0.5.3"
lazy val sbtScoverageVersion      = "1.9.2"
lazy val scalaStyleVersion        = "1.0.0"

addSbtPlugin("com.github.sbt"   % "sbt-native-packager"    % sbtNativePackagerVersion)
addSbtPlugin("io.spray"         % "sbt-revolver"           % sbtRevolverVersion)
addSbtPlugin("org.scalameta"    % "sbt-scalafmt"           % sbtScalafmtVersion)
addSbtPlugin("com.timushev.sbt" % "sbt-updates"            % sbtUpdatesVersion)
addSbtPlugin("org.scoverage"    % "sbt-scoverage"          % sbtScoverageVersion)
addSbtPlugin("org.scalastyle"   %% "scalastyle-sbt-plugin" % scalaStyleVersion)
