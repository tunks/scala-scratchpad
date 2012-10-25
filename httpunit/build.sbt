name := "httpunit"

organization := "com.earldouglas"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.2"

EclipseKeys.withSource := true

classpathTypes ~= (_ + "orbit")
 
 libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "1.8" % "test"
  , "org.eclipse.jetty" % "jetty-webapp" % "8.1.5.v20120716" % "test"
  , "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "test" artifacts (Artifact("javax.servlet", "jar", "jar"))
)


