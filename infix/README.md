# Infix functions with Scala type classes

*January 5, 2014*

Out of the box, Scala supports infix functions via a syntactic nuance; a function with a symbolic name ending in a colon may be written to the left of the instance on which it is defined.

Consider the class `MyNum`:

```scala
case class MyNum(x: Int) {
  def +:(y: Int): Int = x + y
}
```

Because the addition function `+:` ends in a colon, we can use it as both a prefix and an infix function:

```scala
val x = MyNum(21).+:(21) // 42
val y = 21 +: MyNum(21)  // 42
```

This is handy, but somewhat syntactically limited.  Fortunately, we can do better.

Consider the trait `Semigroup`:

```scala
trait Semigroup[A] {
  def append(a1: A, a2: A): A
}
```

Any type `A` can have a semigroup if we can implement `append` for it:

```scala
val listSemigroup =
  new Semigroup[List[Int]] {
    def append(a1: List[Int], a2: List[Int]): List[Int] = a1 ++ a2
  }
import listSemigroup._

val list = append(List(1,2,3), List(4,5,6)) // List(1,2,3,4,5,6)
```

So far, we have a prefix-notation function `append` that takes two lists, `a1` and `a2`, and concatenates them.  Let's augment `Semigroup`, adding a type class that lifts `a1` into a new type that has its own `append` function:

```scala
trait Semigroup[A] {
  def append(a1: A, a2: A): A
  class InfixSemigroup(a1: A) {
    def ⋅(a2: A): A = Semigroup.this.append(a1, a2)
  }
  implicit def infix(a1: A) = new InfixSemigroup(a1)
}
```

We can use our same `listSemigroup` implementation, which now has an implicit `infix` function to convert `a1` into an `InfixSemigroup` instance, which has its own `append` function called `⋅`:

```scala
val listSemigroup =
  new Semigroup[List[Int]] {
    def append(a1: List[Int], a2: List[Int]): List[Int] = a1 ++ a2
  }
import listSemigroup._

val list = List(1,2,3) ⋅ List(4,5,6) // List(1,2,3,4,5,6)
```
