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
