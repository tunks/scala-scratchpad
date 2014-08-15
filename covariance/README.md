# Exercise: covariance in Scala

Starting with the implementation from [the previous exercise](https://github.com/earldouglas/scala-scratchpad/tree/master/option#exercise-option-monad-in-scala):

```scala
sealed trait Option[A] { ... }
case class Some[A](a: A) extends Option[A]
case class None[A]() extends Option[A]
```

Make `Option` covariant in its type `A` so that only a single `None` instance is needed:

```scala
case object None extends Option[???]
```

```scala
def quotient(divisor: Int)(dividend: Int): Int =
  dividend / divisor

def remainder(divisor: Int)(dividend: Int): Option[Int] =
  (dividend % divisor) match {
    case 0 => None
    case r => Some(r)
  }

val x3 =
  for {
    x <- Some(42)
    q  = quotient(2)(x)
    r <- remainder(4)(x)
  } yield (x,q,r)

println(x3) // Some((42,21,2))
```
