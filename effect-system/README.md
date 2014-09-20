# A simple effect system for Scala

*September 20, 2014*

An `Effect` represents an action to be carried out.  It is a kind of instruction.

```scala
case class NotFound(detail: String) 

sealed trait Effect[A] extends Program[A]
case class Log(x: Any) extends Effect[Unit]
case class Pure[A](a: A) extends Effect[A]
case class Save(x: (String,String)) extends Effect[Unit]
case object Enumerate extends Effect[Iterable[(String,String)]]
case class GetByKey(k: String) extends Effect[Either[NotFound,(String,String)]]
case class GetByValue(v: String) extends Effect[Iterable[(String,String)]]
```

A `Program` is a composition of effects (or other programs).

```scala
sealed trait Program[A] {
  final def map[B](f: A => B): Program[B] = flatMap(f andThen Pure.apply)
  final def flatMap[B](f: A => Program[B]): Program[B] = FlatMap(this, f)
  final def andThen[B](p: => Program[B]): Program[B] = FlatMap(this, {_:A => p})
}
case class FlatMap[A,B](p: Program[A], f: A => Program[B]) extends Program[B]
```

Given a (domain specific) way to run an effect:

```scala
trait EffectRunner {
  def runEffect[A](a: Effect[A]): A
}
```

Programs can be run with a common, tail-recursive interpreter:

```scala
trait ProgramRunner extends EffectRunner {
  @annotation.tailrec
  final def runProgram[A](p: Program[A]): A =
    p match {
      case FlatMap(p2, f2) =>
        p2 match {
          case FlatMap(p3, f3) => runProgram(p3 flatMap (a3 => f3(a3) flatMap f2))
          case e:Effect[_]     => runProgram(f2(runEffect(e)))
        }
      case a4:Effect[A] => runEffect(a4)
    }
}
```

Programs are defined with no knowledge of how they will be interpreted.

```scala
val program1: Program[Map[String,Int]] =
  for {
    nmo <- GetByKey("Saenchai")
    _   <- nmo match {
             case Right(_) => Pure(())
             case Left(NotFound(x)) =>
               Log(s"not found: $x") andThen
               Log("creating new record for Senchai") andThen
               Save(("Saenchai", "southpaw"))
           }
    _   <- Save(("Yodwicha", "orthodox"))
    _   <- Save(("Petboonchu", "orthodox"))
    os  <- GetByValue("orthodox")
    oc   = os.size
    sps <- GetByValue("southpaw")
    spc  = sps.size
  } yield Map("orthodox" -> oc, "southpaw" -> spc)
```

The means of interpreting effects depends on the domain, environment, 
configuration, etc.

```scala
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

  private def save(x: (String,String)): Unit = db = db + x

  private def enumerate(): Iterable[(String,String)] = db

  private def getByKey(k: String): Either[NotFound,(String,String)] =
    db get k map { v => Right((k,v)) } getOrElse Left(NotFound(k))

  private def getByValue(v: String): Iterable[(String,String)] =
    for {
      kv <- db
      (_, value) = kv
      if value == v
    } yield kv
}
```

Finally, the effects interpreter is exercised at the top-level of the 
application.

```scala
object Main extends App with SimpleImpl with ProgramRunner {
  println(runProgram(programs.program1))
}
```
