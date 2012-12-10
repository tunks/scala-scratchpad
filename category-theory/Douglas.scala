// Semigroup

trait Semigroup[A, F[_]] {
  def append(a: A): F[A]
}

class Tuple2ListSemigroup[A, B](l: List[(A, B)]) extends Semigroup[(A, B), List] {
  override def append(ab: (A, B)) = ab :: l
}

object Semigroup {
  implicit def tuple2ListSemigroup[A, B](l: List[(A, B)]) = new Tuple2ListSemigroup(l)
}

// Monoid

trait Monoid[A, F[_]] extends Semigroup[A, F] {
  def empty: A
}

// Functor

trait Functor[A, F[_]] {
  def map[B](f: A => B): F[B]
}

object Functor {
  implicit def fn1Functor[A, B](g: A => B) = new Fn1Functor(g)
}

class Fn1Functor[A, B](g: A => B) extends Functor[B, ({type λ[α] = A => α})#λ] {
  override def map[C](f: B => C): (A => C) = { a => f(g(a)) }
}

trait Fn1FunctorDemo {
  import Functor.fn1Functor
  val fn1FunctorDemo: Int => Int =
    { x: Int => x + 1 } map { x: Int => x * 2 } map { x: Int => x - 3 }
  println("fn1FunctorDemo(5) = " + fn1FunctorDemo(5)) // ((5 + 1) * 2) - 3 = 9
}

// Applicative Functor

trait Applicative[A, F[_]] extends Functor[A, F] {
  def ap[B](f: F[A => B]): F[B]
}

class Fn1Applicative[A, B](g: A => B) extends Fn1Functor[A, B](g)
                                         with Applicative[B, ({type λ[α] = A => α})#λ] {
  override def ap[C](f: A => B => C): (A => C) = { a => f(a)(g(a)) } 
}

object Applicative {
  implicit def fn1Applicative[A, B](g: A => B) = new Fn1Applicative(g)
}

trait Fn1ApplicativeDemo {
  import Applicative.fn1Applicative
  val fn1ApplicativeDemo: Int => Int =
    { x: Int => x + 1 } ap { x: Int => y: Int => x * (y + 3) }
  println("fn1ApplicativeDemo(5) = " + fn1ApplicativeDemo(5))
}

// Monad

trait Monad[A, F[_]] extends Applicative[A, F] {
  def flatMap[B](f: A => F[B]): F[B]
}

class Fn1Monad[A, B](g: A => B) extends Fn1Applicative[A, B](g)
                                   with Monad[B, ({type λ[α] = A => α})#λ] {
  override def flatMap[C](f: B => (A => C)): (A => C) = { a => f(g(a))(a) }
}

class StateMonad[S, A](g: S => (A, S)) extends Monad[A, ({type λ[α] = S => (α, S)})#λ] {
  override def map[B](f: A => B): (S => (B, S)) =
    { state =>
      val (a, state1) = g(state)
      (f(a), state1)
    }
  def ap[B](f: S => (A => B, S)): (S => (B, S)) =
    { state =>
      val (a, state1) = g(state)
      val (atob, state2) = f(state1)
      (atob(a), state2)
    }
  override def flatMap[B](f: A => (S => (B, S))): (S => (B, S)) =
    { state =>
      val (a, state1) = g(state)
      f(a)(state1)
    }
}

object Monad {
  type Reader[A, B] = Fn1Monad[A, B]
  implicit def fn1Monad[A, B](g: A => B) = new Fn1Monad(g)
  implicit def stateMonad[S, A](g: S => (A, S)) = new StateMonad(g)
}

trait Fn1MonadDemo1 {
  import Monad.fn1Monad
  val fn1MonadDemo1: Int => Int =
    { x: Int => x + 1 } flatMap { x: Int => y: Int => x * (y + 3) }
  println("fn1MonadDemo1(5) = " + fn1MonadDemo1(5)) // (5 + 1) * (5 + 3) = 48
}

trait Fn1MonadDemo2 {
  import Monad.fn1Monad
  val fn1MonadDemo2: Int => Int =
    for {
      a <- { x: Int => x + 1 }
      b <- { x: Int => y: Int => x * (y + 3) } apply a
    } yield b
  println("fn1MonadDemo2(5) = " + fn1MonadDemo2(5)) // (5 + 1) * (5 + 3) = 48
}

trait StateMonadDemo1 {
  import Monad.stateMonad

  val f1: List[String] => (Int, List[String]) = log => (1, "f1" :: log)
  val f2: Int => List[String] => (Int, List[String]) = x => log => (x + 1, "f2" :: log)

  val f3: List[String] => (Int, List[String]) = for {
    a <- f1
    b <- f2(a)
  } yield b

  val (x, log) = f3(Nil)
  println("StateMonadDemo1: (x, log) = (" + x + ", " + log + ")")
}

trait StateMonadDemo2 {
  import Monad.stateMonad

  trait Log { def append(level: String, message: String): Log }
  class StdoutLog extends Log {
    def append(level: String, message: String) = {
      println("[" + level + "] " + message)
      this
    }
  }

  val f1: Log => (Int, Log) = log => (1, log.append("info", "f1"))
  val f2: Int => Log => (Int, Log) = x => log => (x + 1, log.append("info", "f2"))

  val f3: Log => (Int, Log) = for {
    a <- f1
    b <- f2(a)
  } yield b

  val (x, log) = f3(new StdoutLog)
  println("StateMonadDemo1: x = " + x)
}

// Demo

object Demo extends App
               with Fn1FunctorDemo
               with Fn1ApplicativeDemo
               with Fn1MonadDemo1
               with Fn1MonadDemo2
               with StateMonadDemo2
