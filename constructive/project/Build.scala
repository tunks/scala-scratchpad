import sbt._
import Keys._

object BuildSettings {
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.earldouglas",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.0-M2"
  )
}

object MackroseBuild extends Build {
  import BuildSettings._

  lazy val _constructiveExample: Project = Project(
    "constructive-example",
    file("constructive-example"),
    settings = buildSettings
  ) dependsOn(constructive)

  lazy val constructive: Project = Project(
    "constructive",
    file("constructive"),
    settings = buildSettings ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _)
    )
  )

}

