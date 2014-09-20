package effects

sealed trait Program[A] {
  final def map[B](f: A => B): Program[B] = flatMap(f andThen Pure.apply)
  final def flatMap[B](f: A => Program[B]): Program[B] = FlatMap(this, f)
  final def andThen[B](p: => Program[B]): Program[B] = FlatMap(this, {_:A => p})
}
case class FlatMap[A,B](p: Program[A], f: A => Program[B]) extends Program[B]

trait EffectRunner {
  def runEffect[A](a: Effect[A]): A
}

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

case class NotFound(detail: String) 

sealed trait Effect[A] extends Program[A]
case class Log(x: Any) extends Effect[Unit]
case class Pure[A](a: A) extends Effect[A]
case class Save(x: (String,String)) extends Effect[Unit]
case object Enumerate extends Effect[Iterable[(String,String)]]
case class GetByKey(k: String) extends Effect[Either[NotFound,(String,String)]]
case class GetByValue(v: String) extends Effect[Iterable[(String,String)]]
