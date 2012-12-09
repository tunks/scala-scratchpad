// Semigroup

trait Semigroup[A] {
  def append(a: A): A
}

class IntSemigroup(x: Int) extends Semigroup[Int] {
  override def append(a: Int) = x + a
}

object Semigroup {
  implicit def intSemigroup(x: Int) = new IntSemigroup(x)
}

// Monoid

trait Monoid[A] extends Semigroup[A] {
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

trait Monad[A, F[_]] extends Functor[A, F] {
  def flatMap[B](f: A => F[B]): F[B]
}

class Fn1Monad[A, B](g: A => B) extends Fn1Functor[A, B](g)
                                   with Monad[B, ({type λ[α] = A => α})#λ] {
  override def flatMap[C](f: B => (A => C)): (A => C) = { a => f(g(a))(a) }
}

object Monad {
  type Reader[A, B] = Fn1Monad[A, B]
  implicit def fn1Monad[A, B](g: A => B) = new Fn1Monad(g)
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

// Demo

object Demo extends App
               with Fn1FunctorDemo
               with Fn1ApplicativeDemo
               with Fn1MonadDemo1
               with Fn1MonadDemo2
