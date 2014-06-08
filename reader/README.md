# Exercise: dependency injection in Scala

Given a function `run` and a class `Reader` that wraps it:

```scala
case class Reader[E,A](run: E => A) {

  def map[B](g: A => B): Reader[E,B] = ???

  def flatMap[B](g: A => Reader[E,B]): Reader[E,B] = ???

}
```

Implement `map` and `flatMap` so that we can use `Reader` in a `for`-comprehension:

```scala
def get(k: String): Reader[Map[String,Int],Int] =
  Reader(m => m(k))

val resultR: Reader[Map[String,Int],Tuple4[Int,Int,Int,Int]] =
  for {
    foo <- get("foo")
    bar  = foo * 2
    baz <- get("baz")
    raz  = foo + bar
  } yield (foo,bar,baz,raz)

println(resultR.run(Map("foo" -> 14, "baz" -> 2))) // (14,28,2,42)
```
