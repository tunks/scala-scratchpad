# Type Lambdas in Scala

When reading code that uses higher-kinded types, we sometimes encounter strange-looking constructions like this:

```scala
({type λ[α] = A => α})#λ
```

This is called a type lambda, and its bark is worse than its bite.  In a nutshell, it is a way to fix some of the type variables in a type constructor, to create a sort of partial type constructor.

Consider the following trait:

```scala
trait Functor[A, F[_]] {
  def map[B](f: A => B): F[B]
}
```

An instance of `Functor` requires two type parameters: `A`, which is a plain-old type, and `F`, which is a type constructor that itself needs a single type variable.

Common examples of single-variable type constructors are collections classes, such as `Option` and `List`.  Each is used in conjection with some type to specify a collection of elements of that type: `Option[Int]`, `List[String]`, etc.

Instances of `Functor` for single-variable type constructors are straightforward:

```scala
implicit class ListFunctor[A](xs: List[A]) extends Functor[A, List] {
  def map[B](f: A => B): List[B] = xs.map(f)
}
```

```scala
case class Box[A](a: A)

implicit class BoxFunctor[A](x: Box[A]) extends Functor[A, Box] {
  def map[B](f: A => B): Box[B] = Box(f(x.a))
}

val box21: Box[Int] = Box(21)
val box42: Box[Int] = box21.map(x => x * 2) // Box(42)
val box42s: Box[String] = box42.map(x => x.toString) // Box("42")
```

Now consider a function with a single input:

```scala
def foo[A,B](a: A): B = ...
```

The type of this function is `A => B`, where `A` and `B` can be arbitrary types.

Things get a little trickier when we need to declare a `Functor` instance for a type constructor with multiple type variables.  We cannot represent `A => B` as `F[_]`, because we do not know which type variable `A` or `B` the placeholder `_` represents.

We have to fix either `A` or `B`, so that the placeholder `_` takes the place of the remaining free variable.  Ideally we would represent this using familiar syntax:

```
class Fn1Functor[A, B](g: A => B) extends Functor[B, A => _] {
  def map[C](f: B => C): (A => C) = a => f(g(a))
}
```

Unfortunately this does not compile, because `A => _` does not mean what we think it does.  Instead, we write a type lambda:

```
implicit class Fn1Functor[A, B](g: A => B) extends Functor[B, ({type λ[α] = A => α})#λ] {
  def map[C](f: B => C): (A => C) = a => f(g(a))
}
```

In the body of `Fn1Functor`, a reference to `λ[X]` means `A => X` for any `X`.  There is nothing special about the choice of `λ` for the name of this type; it is just a convention.

This is somewhat visually noisy, but luckily we do not need to worry about the type lambda after we have written it; it is transparent in the usage of `Fn1Functor`:

```scala
val plusOne: Int => Int = { x => x + 1 }
val timesTwo: Int => Int = { x => x * 2 }
val itoa: Int => String = { x => x.toString }

val plusOneTimesTwoToString: Int => String = plusOne.map(timesTwo).map(itoa)

val fortyTwo: String = plusOneTimesTwoToString(20) // "42"
````
