# Exercise: option monad in Scala

Given a trait `Option` and two implementations:

```scala
sealed trait Option[A] {

  def map[B](f: A => B): Option[B] = ???

  def flatMap[B](f: A => Option[B]): Option[B] = ???

  override def toString(): String =
    this match {
      case Some(a) => "Some(" + a + ")"
      case None()  => "None()"
    }

}

case class Some[A](a: A) extends Option[A]
case class None[A]() extends Option[A]
```

Implement `map` and `flatMap` so that we can use `Option` in a `for`-comprehension:

```scala
def quotient(divisor: Int)(dividend: Int): Int =
  dividend / divisor

def remainder(divisor: Int)(dividend: Int): Option[Int] =
  (dividend % divisor) match {
    case 0 => None()
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
