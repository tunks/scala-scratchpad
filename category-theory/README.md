# Type Classes, Category Theory and Scala: A Reference

## Functor

```scala
trait Functor[A, F[_]] {
  def map[B](f: A => B): F[B]
}
```

### Function1 functor

```scala
class Fn1Functor[A, B](g: A => B) extends Functor[B, ({type λ[α] = A => α})#λ] {
  override def map[C](f: B => C): (A => C) = { a => f(g(a)) }
}
```

```scala
implicit def fn1Functor[A, B](g: A => B) = new Fn1Functor(g)

val fn1FunctorDemo: Int => Int =
  { x: Int => x + 1 } map { x: Int => x * 2 } map { x: Int => x - 3 }

println("fn1FunctorDemo(5) = " + fn1FunctorDemo(5)) // ((5 + 1) * 2) - 3 = 9
```
