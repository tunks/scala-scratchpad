# Monads in Scala

*10 September 2013*

In category theory, a monad builds on a certain morphism between categories.  Practically speaking, this allows a function of type `A => F[B]` to be lifted to a function of type `F[A] => F[B]`, for some type constructor `F`.

In Scala, a monad might look like this:

```scala
trait Monad[A, F[_]] extends Functor[A, F] {
  def flatMap[B](f: A => F[B]): F[B]
}
```

I like to picture it like this:

```
.--------------.         .-------------------.
|  Category _  |         |   Category F[_]   |
|--------------|         |-------------------|
| A            |         |   F[A]            |
| B            |         |   F[B]            |
| A  ======================> F[B]            |
|           |  |         |                   |
|           '~~~ flatMap ~~> F[A] => F[B]    |
'--------------'         '-------------------'
```

This can be read as follows:

1. The category `_` contains objects of type `A` and `B`, and a morphism from `A` to `F[B]`, which is in the category `F[_]`.
2. The category `F[_]` contains objects of type `F[A]` and `F[B]`, and a morphism from `F[A]` to `F[B]`.
3. A monad for `F[_]` converts the `A => F[B]` morphism into the `F[A] => F[B]` morphism via `flatMap`.

This is useful when you have a value of type `F[A]` and you want to apply a function of type `A => F[B]` to it, resulting in a value of type `F[B]`.

Though Scala doesn't explicitly have a `Monad` type, many of the data structures in Scala act as monads.  Let's look at some examples.

## Option

In Scala, an `Option` is a box that might contain a value.  It has two implementations: `Some`, which contains a value of a given type, and `None`, which does not contain a value.

```scala
scala> val s1: Option[String] = Some("42")
s1: Option[String] = Some(42)

scala> val s2: Option[String] = Some("forty-two")
s2: Option[String] = Some("forty-two")

scala> val n: Option[String] = None
n: Option[String] = None
```

If we imagine that `Option` represents the category of optional values, we can see that `Some` and `None` are monads.  Here's how it looks:


```
.------------------.         .----------------------------------.
|  Category _      |         |        Category Option[_]        |
|------------------|         |----------------------------------|
| Int              |         |   Option[Int]                    |
| String           |         |   Option[String]                 |
| String  =====================> Option[Int]                    |
|               |  |         |                                  |
|               '~~~ flatMap ~~> Option[String] => Option[Int]  |
'------------------'         '----------------------------------'
```

First, let's define a simple function:

```scala
scala> val atoi: String => Option[Int] =
     |   { x => 
     |     try { Some(x.toInt) }
     |     catch { case _ => None }
     |   }
atoi: String => Option[Int] = <function1>
```

Now let's flatMap that function onto some `Option` instances:

```scala
scala> val s3 = s1.flatMap(atoi)
s3: Option[Int] = Some(42)

scala> val s4 = s2.flatMap(atoi)
s4: Option[Int] = None

scala> val n1 = n.flatMap(atoi)
n1: Option[Int] = None
```

When using `flatMap` on `atoi`, we start with an instance of type `Option[String]`, apply the function `atoi` of type `String => Option[Int]`, resulting in an instance of type `Option[Int]`.

Since `Option[A]` defines a function `flatMap(f: A => Option[B]): Option[B]`, `Option` is a monad.

## List

In Scala, a `List` is a box that contains zero or more values in a particular order.

```scala
scala> val l1: List[String] = List("6", "7", "42")
l1: List[String] = List(6, 7, 42)

scala> val l2: List[String] = List("6", "seven", "42")
l2: List[String] = List(6, seven, 42)

scala> val n: List[String] = Nil
n: List[String] = List()
```

If we imagine that `List` represents the category of lists of values, we can see that it is also a monad.  Here's how it looks:

```
.------------------.         .------------------------------.
|  Category _      |         |       Category List[_]       |
|------------------|         |------------------------------|
| Int              |         |   List[Int]                  |
| String           |         |   List[String]               |
| String  =====================> List[Int]                  |
|               |  |         |                              |
|               '~~~ flatMap ~~> List[String] => List[Int]  |
'------------------'         '------------------------------'
```

As before, let's define a simple function:

```scala
scala> val digits: String => List[Int] =
     |   { x =>
     |     try { x.split("").toList.tail.map(c => c.toInt) }
     |     catch { case _ => Nil }
     |   }
digits: String => List[Int] = <function1>
```

Now let's flatMap that function onto some `List` instances:

```scala
scala> val l3 = l1.flatMap(digits)
l3: List[Int] = List(6, 7, 4, 2)

scala> val l3 = l2.flatMap(digits)
l3: List[Int] = List(6, 4, 2)

scala> val n1 = n.flatMap(digits)
n1: List[Int] = List()
```

When using `flatMap` on `digits`, we start with an instance of type `List[String]`, apply the function `digits` of type `String => List[Int]`, resulting in an instance of type `List[Int]`.

Since `List[A]` defines a function `flatMap(f: A => List[B]): List[B]`, `List` is a monad.
