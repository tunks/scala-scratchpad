package functionaldebugging

object Arithmetic {

  val add: Int => Int => Int =
    x => y => x + y

  val mult: Int => Int => Int =
    x => y => x * y

  val div: Int => Int => Option[Double] =
    x => y => if (y == 0) None else Some(x.toDouble / y)

  val parse: String => Option[Int] =
    x => try { Some(x.toInt) } catch { case t: Throwable => None }

}

trait SideEffects {

  import Arithmetic._

  lazy val result =
    for {
      x1 <- parse("42")
       _  = println("x1: " + x1)
      x2  = mult(x1)(2)
       _  = println("x2: " + x2)
      x3  = add(x2)(42)
       _  = println("x3: " + x3)
      x4 <- div(x3)(3)
       _  = println("x4: " + x4)
    } yield x4
  // [stdout] x1: 42
  // [stdout] x2: 84
  // [stdout] x3: 126
  // [stdout] x4: 42.0
  // Some(42.0)

}

trait Semigroup[F[_],A] {
  def *(a: A): F[A]
}

object Semigroup {
  implicit def semigroup[A](x: Seq[A]): Semigroup[Seq,A] =
    new Semigroup[Seq,A] {
      def *(a: A): Seq[A] = x :+ a
    }
}

trait Functor[F[_],A] {
  def map[B](f: A => B): F[B]
}

trait Monad[F[_],A] extends Functor[F,A] {
  def flatMap[B](f: A => F[B]): F[B]
}

object Identity {

  case class ID[A](a: A)

  implicit def id[A](x: ID[A]): Monad[ID,A] =
    new Monad[ID,A] {
      def map[B](f: A => B): ID[B] = ID(f(x.a))
      def flatMap[B](f: A => ID[B]): ID[B] = f(x.a)
    }

}

object Option {

  implicit def option[A](x: Option[A]): Monad[Option,A] =
    new Monad[Option,A] {
      def map[B](f: A => B): Option[B] = x.map(f)
      def flatMap[B](f: A => Option[B]): Option[B] = x.flatMap(f)
    }

}

object State {

  import Identity._

  case class StateT[F[_],S,A](run: S => F[(A,S)])
  type State[S,A] = StateT[ID,S,A]

  private type StateTM[F[_],S,A] = Monad[({type λ[α] = StateT[F,S,α]})#λ,A]

  implicit def stateT[F[_],S,A](x: StateT[F,S,A])
      (implicit lift: F[(A,S)] => Monad[F,(A,S)]): StateTM[F,S,A] =
    new StateTM[F,S,A] {
      def run(s: S): F[(A,S)] = x.run(s)
      def map[B](f: A => B): StateT[F,S,B] =
        new StateT[F,S,B]({ g => x.run(g) map { x => (f(x._1), x._2) } })
      def flatMap[B](f: A => StateT[F,S,B]): StateT[F,S,B] =
        new StateT[F,S,B]({ g => x.run(g) flatMap { x => f(x._1).run(x._2) } })
    }

}

object Log {

  import State._

  type LogT[F[_],A] = StateT[F,Semigroup[Seq,String],A]

  def logT[F[_],A](x: F[A])(implicit lift: F[A] => Monad[F,A]): LogT[F,A] =
    new LogT(log => x.map(a => (a, log)))

  def log(x: String): LogT[Option,Unit] =
    new LogT(log => Some(((), log * x)))

  def run[F[_],A](log: LogT[F,A]) = log.run(Nil)
}

trait NoSideEffects {

  import Arithmetic._
  import Option._
  import State._
  import Log._
  import Semigroup._

  lazy val resultL =
    for {
      x1 <- logT(parse("42"))
      _  <- log("x1: " + x1)
      x2  = mult(x1)(2)
      _  <- log("x2: " + x2)
      x3  = add(x2)(42)
      _  <- log("x3: " + x3)
      x4 <- logT(div(x3)(3))
      _  <- log("x4: " + x4)
    } yield x4

  lazy val result = run(resultL)
  // Some((42.0,List(x1: 42, x2: 84, x3: 126, x4: 42.0)))

}

object Main extends App with NoSideEffects {
  println(result)
}
