name := "web-effect-system"

scalaVersion := "2.10.4"

jetty()

libraryDependencies ++= Seq(
    "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
  , "com.github.spullara.mustache.java" % "compiler" % "0.8.9"
)
