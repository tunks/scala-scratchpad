# Functors in Scala

*9 September 2013*

In category theory, a functor is a mapping between categories.  Practically speaking, this allows a function of type `A => B` to be lifted to a function of type `F[A] => F[B]`, for some type constructor `F`.


In Scala, a functor might look like this:

```scala
trait Functor[A, F[_]] {
  def map[B](f: A => B): F[B]
}
```

I like to picture it like this:

```
.--------------.         .-------------------.
|  Category _  |         |   Category F[_]   |
|--------------|         |-------------------|
| A            |         |   F[A]            |
| B            |         |   F[B]            |
| A => B   ~~~~~~~ map ~~~~> F[A] => F[B]    |
'--------------'         '-------------------'
```

This can be read as follows:

1. The category `_` contains objects of type `A` and `B`, and a morphism from `A` to `B`.
2. The category `F[_]` contains objects of type `F[A]` and `F[B]`, and a morphism from `F[A]` to `F[B]`.
3. A functor for `F[_]` maps morphisms from `_`, for example converting `A => B` into `F[A] => F[B]`.

This is useful when you have a value of type `F[A]` and you want to apply a function of type `A => B` to it, resulting in a value of type `F[B]`.

Though Scala doesn't explicitly have a `Functor` type, many of the data structures in Scala act as functors.  Let's look at some examples.

## Option

In Scala, an `Option` is a box that might contain a value.  It has two implementations: `Some`, which contains a value of a given type, and `None`, which does not contain a value.

```scala
scala> val s: Option[Int] = Some(42)
s: Option[Int] = Some(42)

scala> val n: Option[Int] = None
n: Option[Int] = None
```

If we imagine that `Option` represents the category of optional values, we can see that `Some` and `None` are functors.  Here's how it looks:


```
.------------------.         .----------------------------------.
|  Category _      |         |        Category Option[_]        |
|------------------|         |----------------------------------|
| Int              |         |   Option[Int]                    |
| String           |         |   Option[String]                 |
| Int => Int     ~~~~~ map ~~~~> Option[Int] => Option[Int]     |
| Int => String  ~~~~~ map ~~~~> Option[Int] => Option[String]  |
'------------------'         '----------------------------------'
```

First, let's define some simple functions:

```scala
scala> val dbl: Int => Int = { x => x * 2 }
dbl: Int => Int = <function1>

scala> val itoa: Int => String = { x => x.toString }
itoa: Int => String = <function1>
```

Now let's map those functions onto some `Option` instances:

```scala
scala> val s1 = s.map(dbl)
s1: Option[Int] = Some(84)

scala> val n1 = n.map(dbl)
n1: Option[Int] = None
```

When using `map` on `dbl`, we start with an instance of type `Option[Int]`, apply the function `dbl` of type `Int => Int`, resulting in an instance of type `Option[Int]`.

```scala
scala> val s2 = s.map(itoa)
s2: Option[String] = Some(42)

scala> val n2 = n.map(itoa)
n2: Option[String] = None
```

When using `map` on `itoa`, we start with an instance of type `Option[Int]`, apply the function `itoa` of type `Int => String`, resulting in an instance of type `Option[String]`.

Since `Option[A]` defines a function `map(f: A => B): Option[B]`, `Option` is a functor.

## List

In Scala, a `List` is a box that contains zero or more values in a particular order.

```scala
scala> val l: List[Int] = List(6,7,42)
l: List[Int] = List(6, 7, 42)

scala> val n: List[Int] = Nil
n: List[Int] = List()
```

If we imagine that `List` represents the category of lists of values, we can see that it is also a functor.  Here's how it looks:


```
.------------------.         .------------------------------.
|  Category _      |         |       Category List[_]       |
|------------------|         |------------------------------|
| Int              |         |   List[Int]                  |
| String           |         |   List[String]               |
| Int => Int     ~~~~~ map ~~~~> List[Int] => List[Int]     |
| Int => String  ~~~~~ map ~~~~> List[Int] => List[String]  |
'------------------'         '------------------------------'
```

As before, let's define some simple functions:

```scala
scala> val dbl: Int => Int = { x => x * 2 }
dbl: Int => Int = <function1>

scala> val itoa: Int => String = { x => x.toString }
itoa: Int => String = <function1>
```

Now let's map those functions onto some `List` instances:

```scala
scala> val l1 = l.map(dbl)
l1: List[Int] = List(12, 14, 84)

scala> val n1 = n.map(dbl)
n1: List[Int] = List()
```

When using `map` on `dbl`, we start with an instance of type `List[Int]`, apply the function `dbl` of type `Int => Int`, resulting in an instance of type `List[Int]`.

```scala
scala> val l2 = l.map(itoa)
l2: List[String] = List(6, 7, 42)

scala> val n2 = n.map(itoa)
n2: List[String] = List()
```

When using `map` on `itoa`, we start with an instance of type `List[Int]`, apply the function `itoa` of type `Int => String`, resulting in an instance of type `List[String]`.

Since `List[A]` defines a function `map(f: A => B): List[B]`, `List` is a functor.
