# Arrows and Scala Implicit Conversions

_11 Mar 2012_

[Arrows](http://en.wikipedia.org/wiki/Arrows_in_functional_programming) are a neat way to chain functions together in [interesting ways](http://www.haskell.org/arrows/).

In Scala, an arrow interface might look like this:

```scala
trait Arrow[A[_,_]] {
  def arr[B,C](f: B => C): A[B,C]
  def >>>[B,C,D](f: B => C, g: C => D): A[B,D]
  def first[B,C,D](f: B => C): A[(B,D),(C,D)]
  def second[B,C,D](f: B => C): A[(D,B),(D,C)]
  def ***[B,C,D,E](f: B => C, g: D => E): A[(B,D),(C,E)]
  def &&&[B,C,D](f: B => C, g: B => D): A[B,(C,D)]
}
```

A basic `Arrow` of type `Function1` would then be:

```scala
class InfixFunction1Arrow[B,C](f: B => C) {
  def arr = f
  def >>>[D](g: C => D) = (b: B) => g(f(b))
  def first[D](bd: (B,D)) = (f(bd._1), bd._2)
  def second[D](db: (D,B)) = first(db.swap).swap
  def ***[D,E](g: D => E) = (bd: (B,D)) => (f(bd._1), g(bd._2))
  def &&&[D](g: B => D) = (b: B) => ***(g)((b,b))
}
```

This is nice and object-oriented, but in practice it turns out to be pretty noisy. Consider a chain of computation for inputting, adding, and printing two complex numbers:

```
.--------------------.    .------------------------.
| .---------.        |    |      .----------.      |
| | get num |---.    |    |    .-| add real |-.    |
| '---------'    \   |    |   /  '----------'  \   |    .-----------.    .-------.
|                 >--|--->|--<                  >--|--->| to string |--->| print |
| .---------.    /   |    |   \  .----------.  /   |    '-----------'    '-------'
| | get num |---'    |    |    '-| add imag |-'    |
| '---------'        |    |      '----------'      |
'--------------------'    '------------------------'
```

We can do this, but it's not pretty:

```scala
val arrow = new Function1Arrow()

def addC = (xy: (Complex, Complex)) => arrow.***(add(xy._1._1), add(xy._1._2))(xy._2._1, xy._2._2)

def demo = {
  val getCs: Unit => (Complex, Complex) = Unit => (getC(), getC())
  arrow.>>>(getCs, arrow.>>>(addC, arrow.>>>(toStringC, println)))
}
```

The two most irritating things about this implementation are the prefix notation of the arrow functions and the type noise. It is not easy to tell from this code that it implements the chain correctly. Luckily we can do better.

Let's create a class to get us halfway through each arrow function:

```scala
class InfixFunction1Arrow[B,C](f: B => C) {
  def arr = f
  def >>>[D](g: C => D) = (b: B) => g(f(b))
  def first[D](bd: (B,D)) = (f(bd._1), bd._2)
  def second[D](db: (D,B)) = first(db.swap).swap
  def ***[D,E](g: D => E) = (bd: (B,D)) => (f(bd._1), g(bd._2))
  def &&&[D](g: B => D) = (b: B) => ***(g)((b,b))
}
```

Though not an implementation of the Arrow trait, this does basically the same thing, but with better type inference and the ability to infix the arrow functions:

```scala
implicit def function1ToArrow[B,C](f: B => C): InfixFunction1Arrow[B,C]    = new InfixFunction1Arrow(f)
implicit def function0ToArrow[C](f: => C):     InfixFunction1Arrow[Unit,C] = new InfixFunction1Arrow(Unit => f)

def addC = (xy: (Complex, Complex)) => (add(xy._1._1) *** add(xy._1._2))(xy._2._1, xy._2._2)

def demo = (getC(), getC()) >>> addC >>> toStringC >>> println
```

The `demo` method is now much easier to read, and we can plainly see the chaining of each step in the workflow.

This implementation could be further cleaned up by adding an `InfixFunction2Arrow` class, which would allow functions with two inputs so we don't have to cram our two complex numbers into a `Tuple2` to be fed into the addition function. 
