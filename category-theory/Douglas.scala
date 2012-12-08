// Semigroup

trait Semigroup[A] {
  def append(a: A): A
}

class IntSemigroup(x: Int) extends Semigroup[Int] {
  override def append(a: Int) = x + a
}

object IntSemigroup {
  implicit def apply(x: Int) = new IntSemigroup(x)
}

// Monoid

trait Monoid[A] extends Semigroup[A] {
  def empty: A
}

// Functor

trait Functor[A, F[_]] {
  def map[B](f: A => B): F[B]
}

class Fn1Functor[A, B](g: A => B) extends Functor[B, ({type λ[α] = A => α})#λ] {
  override def map[C](f: B => C): (A => C) = { a => f(g(a)) }
}

object Fn1Functor {
  implicit def apply[A, B](g: A => B) = new Fn1Functor(g)
}

// Monad

trait Monad[A, F[_]] extends Functor[A, F] {
  def flatMap[B](f: A => F[B]): F[B]
}

class Fn1Monad[A, B](g: A => B) extends Fn1Functor[A, B](g) with Monad[B, ({type λ[α] = A => α})#λ] {
  override def flatMap[C](f: B => (A => C)): (A => C) = { a => f(g(a))(a) }
}

object Fn1Monad {
  type Reader[A, B] = Fn1Monad[A, B]
  implicit def apply[A, B](g: A => B) = new Fn1Monad(g)
}

// Demo

object Demo extends App {

  fn1FunctorDemo()
  fn1MonadDemo()

  def fn1FunctorDemo() {
    import Fn1Functor._

    val add1: Int => Int = { x => x + 1 }
    val times2: Int => Int = { x => x * 2 }

    val map1: Int => Int = add1.map(times2)
    println("map1(5) = " + map1(5))

  }

  def fn1MonadDemo() {
    import Fn1Monad._

    val add1: Int => Int = { x => x + 1 }
    val times: Int => Int => Int = { x => y => x * y }

    val flatMap1: Int => Int = add1.flatMap(times)
    println("flatMap1(5) = " + flatMap1(5))

    val flatMap2: Int => Int =
      for {
        a1 <- add1
         t <- times(a1)
      } yield t
    println("flatMap2(5) = " + flatMap2(5))

  }
}

