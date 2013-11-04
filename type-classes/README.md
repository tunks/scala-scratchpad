# Type classes in Scala

*November 3, 2013*

Imagine we define a way for something to be "showable":

```scala
trait Showable {
  def show: String
}
```

Any class that extends `Showable` must implement the `show` method.

Given a way to "show" a `Showable`:
 
```scala
object Stdout {
  def printLine(x: Showable): Unit = println(x.show)
}
```
 
...how do we show arbitrary data?

```scala
Stdout.printLine(42) // fails to compile, because 42 is an Int, and Int does not extend Showable
```
 
We need to make `42` a `Showable` so that we can use `Stdout#printLine` to show it:

```scala
implicit class IntShowable(x: Int) extends Showable {
  def show: String = x.toString
}
```

Now we can show `42`:

```scala
Stdout.printLine(42) // prints "42" to stdout
```
 
This works because Scala sees us trying to pass `42` (an `Int`) as a `Showable`, so it looks for an implicit conversion that can transform an `Int` into a `Showable`. It finds one in `IntShowable`, which provides a constructor with the type `Int => Showable`.
