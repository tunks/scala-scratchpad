# Type Classes, Category Theory, and Scala

_8 December 2012_

This is a light reference for Scala implementations of some commonly used aspects of category theory, and will grow over time as I encounter useful ways to apply it.

## Functor

A functor lifts a function of type `A => B` to a function of type `F[A] => F[B]`

```scala
trait Functor[A, F[_]] {
  def map[B](f: A => B): F[B]
}
```

### The unary function functor

The unary function functor is used to compose a bunch of unary functions together, like Scala's [`andThen`](http://www.scala-lang.org/api/current/scala/Function1.html) function.

```scala
class Fn1Functor[A, B](g: A => B) extends Functor[B, ({type λ[α] = A => α})#λ] {
  override def map[C](f: B => C): (A => C) = a => f(g(a))
}
```

Example: the function `fn1FunctorEx` below has the form `x => ((x + 1) * 2) - 3`

```scala
implicit def fn1Functor[A, B](g: A => B) = new Fn1Functor(g)

val fn1FunctorEx: Int => Int =
  { x: Int => x + 1 } map { x: Int => x * 2 } map { x: Int => x - 3 }

val x = fn1FunctorEx(5) // x = ((5 + 1) * 2) - 3 = 9
```

## Applicative

An applicative builds upon a functor with a way to apply an already-lifted function of type `F[A => B]` as a function of type `F[A] => F[B]`

```scala
trait Applicative[A, F[_]] extends Functor[A, F] {
  def ap[B](f: F[A => B]): F[B]
}
```

### The unary function applicative functor

```scala
class Fn1Applicative[A, B](g: A => B) extends Fn1Functor[A, B](g)
                                         with Applicative[B, ({type λ[α] = A => α})#λ] {
  override def ap[C](f: A => B => C): (A => C) = a => f(a)(g(a))
}
```

Example:

```scala
implicit def fn1Applicative[A, B](g: A => B) = new Fn1Applicative(g)
val fn1ApplicativeDemo: Int => Int =
  { x: Int => x + 1 } ap { x: Int => y: Int => x * (y + 3) }
val x = fn1ApplicativeDemo(5) // x = 5 * ((5 + 1) + 3) = 45
```

### The optional value applicative functor

The optional value applicative functor can be used to pass potentially unknown arguments to a function.

```scala
class OptionApplicative[A](a: A) extends Applicative[A, Option] {
  override def map[B](f: A => B): Option[B] = Option(f(a))
  override def ap[B](f: Option[A => B]): Option[B] = f.map(_(a))
}
```

Example: the function `add3` takes three integers that might be null, which Scala interprets as zero, and sums them

```scala
implicit def optionApplicative[A](a: A) = new OptionApplicative(a)
val add3: Int => Int => Int => Int = x => y => z => x + y + z

val optionApplicativeDemo1 = 1 ap (2 ap (3 map add3)) // Some(6)

val nope: Int = null.asInstanceOf[Int]
val optionApplicativeDemo2 = 1 ap (nope ap (3 map add3)) // Some(4)
```

### The either applicative functor

The either applicative functor can be used to pass potentially invalid arguments to a function.  This is useful when arguments must first be parsed.

```scala
trait Semigroup[A] {
  def append(a: A): A
}

class ListSemigroup[A](as: List[A]) extends Semigroup[List[A]] {
  override def append(as2: List[A]) = as ++ as2
}

class EitherApplicative[B, A](x: Either[B, A])(implicit bs: B => Semigroup[B])
  extends Applicative[A, ({type λ[α] = Either[B, α]})#λ] {
  override def map[C](f: A => C): Either[B, C] = x match {
    case Right(a) => Right(f(a))
    case Left(b)  => Left(b)
  }
  override def ap[C](f: Either[B, A => C]): Either[B, C] = x match {
    case Right(a) => f match {
      case Right(ac) => Right(ac(a))
      case Left(b)   => Left(b)
    }
    case Left(b) => f match {
      case Right(_) => Left(b)
      case Left(b2) => Left(bs(b) append b2)
    }
  }
}
```

Example: the function `add4` takes four integers that have been parsed from strings, and sums them

```scala
object EitherApplicative {
  val lifter = new Applicative.Lifter[({type λ[α] = Either[List[Any], α]})#λ]
  implicit def functee[A, B](g: A => B) = new lifter.Functee(g)
  implicit def applicatee[A, B](f: Either[List[Any], A => B]) = new lifter.Applicatee(f)
}

object Applicative {
  class Lifter[F[_]] {
    class Functee[A, B](g: A => B) {
      def <%>(f: Functor[A, F]) = f map g
    }
    class Applicatee[A, B](f: F[A => B]) {
      def <*>(a: Applicative[A, F]) = a ap f
    }
  }
}
```

```scala
implicit def listSemigroup[A](as: List[A]) = new ListSemigroup(as)
implicit def eitherApplicative[A, B](x: Either[B, A])(implicit bs: B => Semigroup[B]) =
  new EitherApplicative(x)
import EitherApplicative._

val add4: Int => Int => Int => Int => Int = w => x => y => z => w + x + y + z

def parse(x: String): Either[List[String], Int] = try {
  Right(x.toInt)
} catch {
  case _ => Left(List("'" + x + "' is not an integer"))
}

val rightApplicativeDemo = parse("1") ap (parse("2") ap (parse("3") ap (parse("4") map add4)))
  // rightApplicativeDemo = Right(10)

val rightApplicativeDemo2 = add4 <%> parse("1") <*> parse("2") <*> parse("3") <*> parse("4")
  // rightApplicativeDemo2 = Right(10)

val leftApplicativeDemo1 = parse("1") ap (parse("nooo") ap (parse("3") ap (parse("fourve") map add4)))
  // leftApplicativeDemo1 = Left(List('nooo' is not an integer, 'fourve' is not an integer))

val leftApplicativeDemo2 = add4 <%> parse("1") <*> parse("nooo") <*> parse("3") <*> parse("fourve")
  // leftApplicativeDemo2 = Left(List('fourve' is not an integer, 'nooo' is not an integer))
```

## Monad

A monad builds upon an applicative by adding a way to "tilt" an inter-category function of type `A => F[B]` and apply it as a function of type `F[A] => F[B]`

```scala
trait Monad[A, F[_]] extends Applicative[A, F] {
  def flatMap[B](f: A => F[B]): F[B]
}
```

### The unary function monad (aka the Reader monad)

The unary function monad is used to pass a single external value into a bunch of functions that depend on it.  This is also known as the Reader monad, and is a way to implement [dependency injection](https://github.com/Versal/jellyfish).

```scala
class Fn1Monad[A, B](g: A => B) extends Fn1Applicative[A, B](g)
                                   with Monad[B, ({type λ[α] = A => α})#λ] {
  override def flatMap[C](f: B => (A => C)): (A => C) = a => f(g(a))(a)
}
```

Example: the function `fn1MonadEx1` below has the form `x => (x + 1) * x`

```scala
implicit def fn1Monad[A, B](g: A => B) = new Fn1Monad(g)

val fn1MonadEx1: Int => Int =
  { x: Int => x + 1 } flatMap { x: Int => y: Int => x * (y + 3) }

val x = fn1MonadEx1(5) // x = (5 + 1) * (5 + 3) = 48
```

Example: like `fn1MonadEx1` above, the function `fn1MonadEx2` below has the form `x => (x + 1) * x`

```scala
implicit def fn1Monad[A, B](g: A => B) = new Fn1Monad(g)

val fn1MonadEx2: Int => Int =
  for {
    a <- { x: Int => x + 1 }
    b <- { x: Int => y: Int => x * y } apply a
  } yield b

val x = fn1MonadEx2(5) // x = (5 + 1) * (5 + 3) = 48
```

### The state monad

The state monad is used to pass a modifiable context from one function invocation to the next.

```scala
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
```

Example: logging to an in-memory list of messages

```scala
implicit def stateMonad[S, A](g: S => (A, S)) = new StateMonad(g)

val f1: List[String] => (Int, List[String]) = log => (1, "f1" :: log)
val f2: Int => List[String] => (Int, List[String]) = x => log => (x + 1, "f2" :: log)

val f3: List[String] => (Int, List[String]) = for {
  a <- f1
  b <- f2(a)
} yield b

val (x, log) = f3(Nil) // (x, log) = (2, List("f2", "f1"))
```

Example: logging to a mutable log

We can cheat a little bit and implement a log that does something other than simply collecting log messages in memory.

```scala
trait Log { def append(level: String, message: String): Log }
class StdoutLog extends Log {
  def append(level: String, message: String) = {
    println("[" + level + "] " + message)
    this
  }
}

implicit def stateMonad[S, A](g: S => (A, S)) = new StateMonad(g)

val f1: Log => (Int, Log) = log => (1, log.append("info", "f1"))
val f2: Int => Log => (Int, Log) = x => log => (x + 1, log.append("info", "f2"))

val f3: Log => (Int, Log) = for {
  a <- f1
  b <- f2(a)
} yield b

val (x, log) = f3(new StdoutLog) // x = 2, log has printed two `info` messages to stdout
```
