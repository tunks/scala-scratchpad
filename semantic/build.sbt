resolvers += "bigdata release" at "http://www.systap.com/maven/releases"

scalaVersion := "2.11.5"

libraryDependencies +=  "com.bigdata" % "bigdata" % "1.4.0"

// get banana-rdf and run `sbt publish-local`
libraryDependencies +=  "org.w3" %% "banana" % "0.8.0-SNAPSHOT" exclude("org.w3", "examples_2.11")
