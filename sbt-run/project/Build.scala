import sbt._
import Keys._

object SbtRunBuild extends Build {

  lazy val root = Project(id = "sbt-run", base = file(".")) settings (
      organization := "com.earldouglas"
    , name         := "sbt-run"
    , version      := "0.1-SNAPSHOT"
    , scalaVersion := "2.9.2"
    , TaskKey[Unit]("hello-world")  <<= ( fullClasspath in Compile, runner, scalaInstance) map run("com.earldouglas.HelloWorld", "run")
  )

  def run(className: String, methodName: String): (Classpath, ScalaRun, ScalaInstance) => Unit =
    (cp, runner, instance) => {
      val loader        = classpath.ClasspathUtilities.toLoader(cp map { _.data }, instance.loader)
      val classToRun    = Class.forName(className, true, loader)
      val methodToRun   = classToRun.getMethod(methodName)
      val currentThread = Thread.currentThread
      val oldLoader     = Thread.currentThread.getContextClassLoader()
      currentThread.setContextClassLoader(loader)
      try     { methodToRun.invoke(null) }
      finally { currentThread.setContextClassLoader(oldLoader) }
    }

}
