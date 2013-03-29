# Constructive mathematics with Scala macros

*28 Mar 2013*

Since 2.10.0 Scala includes macros which allow compile-time code execution of arbitrary Scala code.  This lets us extend the behavior of the Scala compiler, and we can do a little "compile-time testing" of our code by letting the compiler prove the validity of our functions.  When the proof fails to hold, the code does not compile.

Let's look at an overly simple constructive interpretation of an existential quantifier.

* let *P(a, b)* be the predicate *a * a = b*
* *∃xP(x, 25)* is proven by the construction of an object *x*, where *|x| = 5*

Our predicate is the squaring function, and we prove one of the possible outcomes, *x * x = 25*, with *x = 5*.

In Scala, this might look like a function which takes a unary function and an input/output pair.

```scala
val sq = constructive1({ x: Int => x * x }, (5,25))
```

The function `constructive1` takes our squaring function and an input/output pair, verifies that the input passed to the function produces the output, and returns the verified function.  We can then use `sq`, confident in the assumption that `sq(5) == 25`.

```scala
def constructive1[A,B](f: Function1[A,B], p: Tuple2[A,B]): Function1[A,B] = macro constructive1_impl[A,B]

def constructive1_impl[A,B](c: Context)(f: c.Expr[Function1[A,B]], p: c.Expr[Tuple2[A,B]]): c.Expr[Function1[A,B]] = {

  val fe: c.Expr[Function1[A,B]] = c.Expr[Function1[A,B]](c.resetAllAttrs(f.tree))
  val pe: c.Expr[Tuple2[A,B]]    = c.Expr[Tuple2[A,B]](c.resetAllAttrs(p.tree))

  val _f: Function1[A,B] = c.eval(fe)
  val _p: Tuple2[A,B]    = c.eval(pe)

  val b = _f(_p._1)
  if (b != _p._2) c.abort(c.enclosingPosition, "expected f(%s) = %s, but was %s".format(_p._1, _p._2, b))

  c.universe.reify(f.splice)
}
```

Due to an [implementation detail](https://issues.scala-lang.org/browse/SI-5748) of the current Scala compiler, evaluation of arguments to a macro requires an untyped tree:

```scala
scala.tools.reflect.ToolBoxError: reflective toolbox has failed: cannot operate on trees that are already typed
```

To get around this, we wrap `f.tree` in a call to `c.resetAllAttrs`.  Unfortunately, this severely constrains the arguments we can pass to our macro.  For example, imagine we want to define our squaring function outside of the macro call site:

```scala
def _sq(x: Int): Int = x * x
val sq = constructive1(_sq, (5, 25))
```

This throws [a compiler error](http://stackoverflow.com/questions/5143849/scala-is-not-an-enclosing-class), due to the lack of type information resulting from the `c.resetAllAttrs` call above.

For simple cases where the function under test can be defined entirely within the argument to the macro, this approach works well.

```scala
val sq = constructive1({ x: Int => x * x }, (5,42))
// fails to compile, because 5 * 5 != 42
```

```scala
val sq = constructive1({ x: Int => x * x }, (5,25))
// compiles, because 5 * 5 == 25
```
