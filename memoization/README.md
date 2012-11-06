# Function Memoization in Scala and Haskell

_6 Feb 2012_

Project Euler problem 15 is an interesting case for optimization by memoization.  The problem states:

> Starting in the top left corner of a 2x2 grid, there are 6 routes (without backtracking) to the bottom right corner.

> ![Project Euler problem 15](https://raw.github.com/JamesEarlDouglas/memoization/master/readme/problem15.png)

> How many routes are there through a 20x20 grid?

Ignoring the mathematical shortcuts available for this problem, the direct solution is straightforward.

_Scala:_
```scala
def problem15(coord: (Long, Long)): Long = coord match {
    case (0, _) => 1
    case (_, 0) => 1
    case (x, y) => problem15(x-1, y) + problem15(x, y-1)
  }
```

_Haskell:_
```haskell
problem15 :: Int -> Int -> Integer
problem15 _ 0 = 1
problem15 0 _ = 1
problem15 x y = problem15 (x-1) y + problem15 x (y-1)
```

Both of these do the job, but are terribly inefficient since each subset of the grid is recomputed for each superset.  Enter memoization.  In Scala, we need to do a little work to extract the fixed-point combinator that will be used to collect the memoized values of our function.

```scala
def Y[A,B](f: (A => B) => (A => B)): (A => B) = {
  val cache = collection.mutable.HashMap[A,B]()
  def fix(f: (A => B) => (A => B)): (A => B) = { a: A =>
    if (!cache.contains(a)) { 
      cache(a) = f(fix(f))(a)
    }
    cache(a)
  }
  fix(f)
}
```

This Y combinator encapsulates the recursive invocation and collection of an arbitrary function `A => B`, so I can use a mutable `Map` but never leak that fact outside of the invocation of the combinator.

Unfortunately the implementation of `problem15` above doesn't quite fit.  Since I'm just a beginner, I opted to modify the implementation of `problem15` to take just a single argument, rather than to figure out how to make a higher-order Y combinator.  My approach is simply to convert the former `x, y` pairs into `(x + x*h)`, where `h` is the height of the original grid.

```scala
def problem15m(x: Int, y: Int) = {
  val yf = Y { f: (Int => Long) => n: Int =>
    if (n < y) 1
    else if (n % y == 0) 1
    else f(n-1) + f(n-y)
  }
  ((0 to x*y).map(yf)).sum
}
```

Now we get nice, linear complexity at the low cost of a temporary hash map in memory.

The memoized Haskell solution takes a similar approach, but takes major advantage of Haskell's laziness to avoid the explicit Y combinator + hash map implementation of above.

_Haskell:_
```haskell
problem15m :: Int -> Int -> Integer
problem15m x y = 1 + (sum $ take (x*y) p15')
  where p15' = (map p15'' [0..])
        p15'' :: Int -> Integer
        p15'' n
          | n < y = 1
          | n `mod` y == 0 = 1
          | otherwise = (p15' !! (n-1)) + (p15' !! (n-y))
```

Pure sweetness.
