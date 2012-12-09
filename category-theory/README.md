# Type Classes, Category Theory and Scala

_8 December 2012_

This is a light reference for Scala implementations of some commonly used aspects of category theory.

This document will grow over time, as I learn more about the theory and discover new ways to apply it.

## Functor

A functor lifts a function of type `A => B` to a function of type `F[A] => F[B]`

```scala
trait Functor[A, F[_]] {
  def map[B](f: A => B): F[B]
}
```

### The unary function functor

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

val x = fn1FunctorEx(5)) // x = ((5 + 1) * 2) - 3 = 9
```

## Monad

A monad builds upon a functor by adding a function to "tilt" an inter-category function of type `A => F[B]` to an intra-category function of type `F[A] => F[B]`

```scala
trait Monad[A, F[_]] extends Functor[A, F] {
  def flatMap[B](f: A => F[B]): F[B]
}
```

### The unary function monad (aka the Reader monad)

```scala
class Fn1Monad[A, B](g: A => B) extends Fn1Functor[A, B](g)
                                   with Monad[B, ({type λ[α] = A => α})#λ] {
  override def flatMap[C](f: B => (A => C)): (A => C) = { a => f(g(a))(a) }
}
```

Example: the function `fn1MonadEx1` below has the form `x => (x + 1) * x`

```scala
implicit def fn1Monad[A, B](g: A => B) = new Fn1Monad(g)

val fn1MonadEx1: Int => Int =
  { x: Int => x + 1 } flatMap { x: Int => y: Int => x * y }

val x = fn1MonadEx1(5)) // x = (5 + 1) * 5 = 30
```

Example: like `fn1MonadEx1` above, the function `fn1MonadEx2` below has the form `x => (x + 1) * x`

```scala
implicit def fn1Monad[A, B](g: A => B) = new Fn1Monad(g)

val fn1MonadEx2: Int => Int =
  for {
    a <- { x: Int => x + 1 }
    b <- { x: Int => y: Int => x * y } apply a
  } yield b

val x = fn1MonadEx2(5)) // x = (5 + 1 * 5) = 30
```
