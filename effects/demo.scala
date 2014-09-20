package demo

import effects._

trait SimpleImpl extends EffectRunner {

   override def runEffect[A](a: Effect[A]): A =
     a match {
       case Log(x)        => println(s"[info] $x")
       case Pure(a)       => a
       case Save(n)       => save(n)
       case Enumerate     => enumerate()
       case GetByKey(k)   => getByKey(k)
       case GetByValue(v) => getByValue(v)
     }

  private var db: Map[String,String] = Map.empty

  private def save(x: Pair): Unit = db = db + x

  private def enumerate(): Iterable[Pair] = db

  private def getByKey(k: String): Either[NotFound,Pair] =
    db get k map { v => Right((k,v)) } getOrElse Left(NotFound(k))

  private def getByValue(v: String): Iterable[Pair] =
    for {
      kv <- db
      (_, value) = kv
      if value == v
    } yield kv

}

object Main extends App with SimpleImpl with ProgramRunner {
  println(runProgram(programs.program1))
}
