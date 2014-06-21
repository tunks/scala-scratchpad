scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
    "org.aspectj" % "aspectjweaver" % "1.7.2"
  , "org.aspectj" % "aspectjrt"     % "1.7.2"
  , "log4j" % "log4j" % "1.2.17"
)

javaOptions += "-javaagent:" + System.getProperty("user.home") + "/.ivy2/cache/org.aspectj/aspectjweaver/jars/aspectjweaver-1.7.2.jar"

fork := true
