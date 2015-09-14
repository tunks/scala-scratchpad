In the following code, what does the `multiply` function do?

```scala
def multiply(x: Int, y: Int): Int = // ...

val z = multiply(6, 7) // 42
``` 

If you guessed that it computes the product of `x` and `y`, you're wrong:

```scala
def multiply(x: Int, y: Int): Int = 42
``` 

`multiply` returns `42` no matter what arguments we pass:

```scala
multiply( 6,  7) // 42
multiply( 2,  3) // 42
multiply(-1, -1) // 42
```

It seems the developer who wrote this function has different
expectations than we do.

We want `multiply` to return the product of its arguments for any
possible arguments.  We'll ignore integer wrap-around for simplicity.

How can we ensure that `multiply` does what we want?

We can use property-based testing to throw lots of random values at it
and check the outcome:

```scala
val propMultiplyInt2 =
  forAll { (x: Int, y: Int) =>
    multiply(x, y) == x * y
  }
```

```
scala> propMultiplyInt2.check
! Falsified after 0 passed tests.
> ARG_0: 0
> ARG_1: 0
> ARG_1_ORIGINAL: 2038343578
```

This approach is fine, but we have to reimplement `multiply` as part of
our test.  Not only is this tautological, but it doesn't scale as the
quantiy and complexity of tests increases.

The (curried) type of `multiply` is `Int => Int => Int`.  This looks
[familiar](https://en.wikipedia.org/wiki/Monoid#Definition):
