# Currying for type inference in Scala

One of the reasons I use currying in Scala is to help the compiler out with type inference.

Take `fmap` as an example.  We can define it without currying:

```scala
def fmap[A,B](xs: Seq[A], f: A => B): Seq[B] = xs.map(f)
```

But this leads to some difficulty using it:

```scala
fmap(List(1,2,3), x => x + 1) // fails to compile: "missing parameter type" on "x =>"
fmap(List(1,2,3), { x: Int => x + 1 } ) // returns [2,3,4] -- but this code is noisy
```

We can redefine it with currying:

```scala
def fmap[A,B](xs: Seq[A])(f: A => B): Seq[B] = xs.map(f)
```

Now we can use it without the extra type noise:

```scala
fmap(List(1,2,3))(x => x + 1) // returns [2,3,4]
```

This works because `fmap(List(1,2,3))` returns a function `(A => B) => Seq[B]` for which Scala has inferred the type of `A` to be `Int`, yielding `(Int => B) => Seq[B]`.
