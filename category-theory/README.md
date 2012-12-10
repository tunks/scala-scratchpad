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
  override def map[C](f: B => C): (A => C) = { a => f(g(a)) }
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
  override def ap[C](f: A => B => C): (A => C) = { a => f(a)(g(a)) } 
}
```

Example:

```scala
implicit def fn1Applicative[A, B](g: A => B) = new Fn1Applicative(g)
val fn1ApplicativeDemo: Int => Int =
  { x: Int => x + 1 } ap { x: Int => y: Int => x * (y + 3) }
val x = fn1ApplicativeDemo(5) // x = 5 * ((5 + 1) + 3) = 45
```

## Monad

A monad builds upon an applicative by adding a way to "tilt" an inter-category function of type `A => F[B]` and apply it as a function of type `F[A] => F[B]`

```scala
trait Monad[A, F[_]] extends Applicative[A, F] {
  def flatMap[B](f: A => F[B]): F[B]
}
```

### The unary function monad (aka the Reader monad)

The unary function monad is used to pass a single external value into a bunch of functions which depend on it.  This is also known as the Reader monad, and is a way to implement [dependency injection](https://github.com/Versal/jellyfish).

```scala
class Fn1Monad[A, B](g: A => B) extends Fn1Applicative[A, B](g)
                                   with Monad[B, ({type λ[α] = A => α})#λ] {
  override def flatMap[C](f: B => (A => C)): (A => C) = { a => f(g(a))(a) }
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

We can cheat a little bit and implementation a log that does something other than simply collecting log messages in memory.

```scala
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

val (x, log) = f3(new StdoutLog) // x = 2, log has printed two `info` messages to stdout
```
