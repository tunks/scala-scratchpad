package statet

object `package` {

  implicit def optMonad[A](oa: Opt[A]): Monad[Opt,A] = new OptMonad[A](oa)

  type State[S,+A] = S => (A,S)
  implicit def stateMonad[S,A](sa: State[S,A]): Monad[({type λ[α] = State[S,α]})#λ,A] =
    new StateMonad[S,A](sa)

  type StateT[S,A,F[_]] = S => F[(A,S)]
  implicit def stateTMonad[S,A,F[_]](sa: StateT[S,A,F])
      (implicit m: F[(A,S)] => Monad[F,(A,S)]): Monad[({type λ[α] = StateT[S,α,F]})#λ,A] =
    new StateTMonad[S,A,F](sa)
}

trait Monad[F[_],A] {
  def map[B](f: A => B): F[B]
  def flatMap[B](f: A => F[B]): F[B]
}

sealed trait Opt[+A]
case class Jus[A](a: A) extends Opt[A]
case object Non extends Opt[Nothing]

object OptMonad {
  def unit[A](a: A): Opt[A] = Jus(a)
  def map[A,B](f: A => B)(oa: Opt[A]): Opt[B] =
    oa match {
      case Jus(a) => Jus(f(a))
      case Non    => Non
    }
  def flatMap[A,B](oa: Opt[A])(f: A => Opt[B]): Opt[B] =
    oa match {
      case Jus(a) => f(a)
      case Non    => Non
    }
}

class OptMonad[A](oa: Opt[A]) extends Monad[Opt,A] {
  def map[B](f: A => B): Opt[B] = OptMonad.map(f)(oa)
  def flatMap[B](f: A => Opt[B]): Opt[B] = OptMonad.flatMap(oa)(f)
}

object StateMonad {
  def unit[S,A](a: A): State[S,A] = s => (a,s)
  def map[S,A,B](f: A => B)(sa: State[S,A]): State[S,B] =
    { s =>
      val (a,s2) = sa(s)
      val b = f(a)
      (b, s2)
    }
  def flatMap[S,A,B](sa: State[S,A])(f: A => State[S,B]): State[S,B] =
    { s =>
      val (a,s2) = sa(s)
      val bs = f(a)
      bs(s2)
    }
}

class StateMonad[S,A](sa: State[S,A])
    extends Monad[({type λ[α] = State[S,α]})#λ,A] {
  def map[B](f: A => B): State[S,B] = StateMonad.map(f)(sa)
  def flatMap[B](f: A => State[S,B]): State[S,B] = StateMonad.flatMap(sa)(f)
}

class StateTMonad[S,A,F[_]](sa: StateT[S,A,F])(implicit m: F[(A,S)] => Monad[F,(A,S)])
    extends Monad[({type λ[α] = StateT[S,α,F]})#λ,A] {
  def map[B](f: A => B): StateT[S,B,F] =
    { s =>
      val fas: F[(A,S)] = sa(s)
      val fab: F[(B,S)] =
        m(fas).map {
          case (a,s2) =>
            val b = f(a)
            (b,s2)
        }
      fab
    }
  def flatMap[B](f: A => StateT[S,B,F]): StateT[S,B,F] =
    { s =>
      val fas: F[(A,S)] = sa(s)
      val fab: F[(B,S)] =
        m(fas).flatMap {
          case (a,s2) =>
            val bs = f(a)
            bs(s2)
        }
      fab
    }
}

object Main extends App {

  val a: Opt[Int] =
    for {
      x <- Jus(1)
      y  = x + 1
    } yield y

  println("a: " + a)

  val b: State[Int,Int] = {

    def get[S]: State[S,S] = s => (s,s)
    def set[S](s: S): State[S,Unit] = _ => ((),s)

    for {
      x <- get[Int]
      y  = x + 1
      _ <- set(5)
    } yield y
  }

  println("b(0): " + b(0))

  val c: StateT[Int,Int,Opt] = {

    def get[S]: StateT[S,S,Opt] = s => Jus((s,s))
    def set[S](s: S): StateT[S,Unit,Opt] = _ => Jus(((),s))

    def lift[S,A](oa: Opt[A]): StateT[S,A,Opt] = s => oa.map(a => (a,s))

    def remainder(a: Int, b: Int): Opt[Int] =
      a % b match {
        case 0 => Non
        case r => Jus(r)
      }

    for {
      x <- get[Int]
      y <- lift[Int,Int](remainder(x, 2))
      _ <- set(5)
    } yield y
  }

  println("c(0): " + c(0))
  println("c(1): " + c(1))

}
