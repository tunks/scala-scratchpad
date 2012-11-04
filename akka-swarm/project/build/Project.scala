import sbt._


class AkkaSwarmProject(info: ProjectInfo) extends DefaultProject(info) with AutoCompilerPlugins with AkkaProject {

  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

  val akkaRemote = akkaModule("remote")

  val scalatest = "org.scalatest" % "scalatest" % "1.3"

  val swarm = "swarm-dpl" % "swarm_2.8.1" % "1.0-SNAPSHOT"

  val cont = compilerPlugin("org.scala-lang.plugins" % "continuations" % "2.8.1")
  override def compileOptions = 
    super.compileOptions ++ compileOptions("-P:continuations:enable")
}
