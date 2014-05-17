# Exercise: trampolines in Scala

Given the functions:

```scala
def odd(x: Int): Boolean =
  if (x == 0) false
  else even(x - 1)

def even(x: Int): Boolean =
  if (x == 0) true
  else odd(x - 1)
```

We can determine the evenness or the oddness of integers:

```scala
println(even(100)) // true
println(even(101)) // false

println(odd(100)) // false
println(odd(101)) // true
```

But due to the mutual recursion of `even` and `odd`, we're limited by our call stack size to evaluating very small numbers:

```scala
println(even(100000)) // stack overflow
```

Convert `even` and `odd` to return `Trampoline[Boolean]` instead of `Boolean`, and write a tail-recursive interpreter that allows large numbers to be evaluated:

```scala
sealed trait Trampoline[+A]
case class Done[+A](a: A) extends Trampoline[A]
case class More[+A](f: () => Trampoline[A]) extends Trampoline[A]

object Trampoline {

  def run[A](t: Trampoline[A]): A = ???

}
```

