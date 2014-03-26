scalaVersion := "2.10.3"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.6"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

ScoverageSbtPlugin.instrumentSettings

CoverallsPlugin.coverallsSettings
