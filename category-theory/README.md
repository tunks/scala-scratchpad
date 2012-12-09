# Type Classes, Category Theory and Scala

## Functor

```scala
trait Functor[A, F[_]] {
  def map[B](f: A => B): F[B]
}
```

### `Function1` functor

```scala
class Fn1Functor[A, B](g: A => B) extends Functor[B, ({type λ[α] = A => α})#λ] {
  override def map[C](f: B => C): (A => C) = { a => f(g(a)) }
}
```

#### Example

```scala
implicit def fn1Functor[A, B](g: A => B) = new Fn1Functor(g)

val fn1FunctorEx: Int => Int =
  { x: Int => x + 1 } map { x: Int => x * 2 } map { x: Int => x - 3 }

println("fn1FunctorEx(5) = " + fn1FunctorEx(5)) // ((5 + 1) * 2) - 3 = 9
```

## Monad

```scala
trait Monad[A, F[_]] extends Functor[A, F] {
  def flatMap[B](f: A => F[B]): F[B]
}
```

### `Function1` monad (aka Reader monad)

```scala
class Fn1Monad[A, B](g: A => B) extends Fn1Functor[A, B](g)
                                   with Monad[B, ({type λ[α] = A => α})#λ] {
  override def flatMap[C](f: B => (A => C)): (A => C) = { a => f(g(a))(a) }
}
```

#### Examples

```scala
implicit def fn1Monad[A, B](g: A => B) = new Fn1Monad(g)

val fn1MonadEx1: Int => Int =
  { x: Int => x + 1 } flatMap { x: Int => y: Int => x * y }

println("fn1MonadEx1(5) = " + fn1MonadEx1(5)) // (5 + 1) * 5 = 30
```

```scala
implicit def fn1Monad[A, B](g: A => B) = new Fn1Monad(g)

val fn1MonadEx2: Int => Int =
  for {
    a <- { x: Int => x + 1 }
    b <- { x: Int => y: Int => x * y } apply a
  } yield b

println("fn1MonadEx2(5) = " + fn1MonadEx2(5)) // (5 + 1 * 5) = 30
