package effects

sealed trait Program[A] {
  final def map[B](f: A => B): Program[B] = flatMap(f andThen Pure.apply)
  final def flatMap[B](f: A => Program[B]): Program[B] = FlatMap(this, f)
  final def andThen[B](p: => Program[B]): Program[B] = FlatMap(this, {_:A => p})
}
case class FlatMap[A,B](p: Program[A], f: A => Program[B]) extends Program[B]

trait Programs extends Effects {

   @annotation.tailrec
   final def runProgram[A](p: Program[A]): A =
     p match {
       case FlatMap(p2, f2) =>
         runProgram(
           p2 match {
             case FlatMap(p3, f3) => p3 flatMap (p4 => f3(p4) flatMap f2)
             case p4:Effect[_]    => f2(runEffect(p4))
           }
         )
       case a4:Effect[A] => runEffect(a4)
    }

}

sealed trait Problem
case class NotFound(detail: String) extends Problem

sealed trait Stance
case object Orthodox extends Stance
case object Southpaw extends Stance

case class NakMuay(name: String, stance: Stance)

sealed trait Effect[A] extends Program[A]
case class Log(x: Any) extends Effect[Unit]
case class Pure[A](a: A) extends Effect[A]
case class Save(nakMuay: NakMuay) extends Effect[Unit]
case object Enumerate extends Effect[Iterable[NakMuay]]
case class GetByName(name: String) extends Effect[Either[NotFound,NakMuay]]
case class GetByStance(stance: Stance) extends Effect[Iterable[NakMuay]]

trait Effects {
  def runEffect[A](a: Effect[A]): A
}
