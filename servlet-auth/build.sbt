name := "servlet-auth"

scalaVersion := "2.10.2"

seq(webSettings :_*)

libraryDependencies ++= Seq(
    "javax.mail" % "mail" % "1.4.6"
  , "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % "container"       
  , "org.eclipse.jetty" % "jetty-plus" % "9.1.0.v20131115" % "container"         
  , "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"                 
)
