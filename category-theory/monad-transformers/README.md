# Monad transformers in Scala

*January 24, 2014*

Monad transformers are useful for nesting different types of monads for use 
within a single for comprehension.

Before we get there, let's define a couple types of monads.

First, the `Maybe` type:

```scala
sealed trait Maybe[+A]
case class Just[A](a: A) extends Maybe[A]
case object Nada extends Maybe[Nothing]
```

This is philosophically the same as Scala's `Option`, but without the 
confusion of whose implementation of `flatMap` we'll be using.  This is 
implemented for `Maybe` in `MaybeMonad`:

```scala
object MaybeMonad {

  implicit def maybeMonad[A](oa: Maybe[A]): Monad[Maybe,A] =
    new MaybeMonad[A](oa)

  def unit[A](a: A): Maybe[A] = Just(a)
  def map[A,B](f: A => B)(oa: Maybe[A]): Maybe[B] =
    oa match {
      case Just(a) => Just(f(a))
      case Nada    => Nada
    }
  def flatMap[A,B](oa: Maybe[A])(f: A => Maybe[B]): Maybe[B] =
    oa match {
      case Just(a) => f(a)
      case Nada    => Nada
    }
}

class MaybeMonad[A](oa: Maybe[A]) extends Monad[Maybe,A] {
  def map[B](f: A => B): Maybe[B] = MaybeMonad.map(f)(oa)
  def flatMap[B](f: A => Maybe[B]): Maybe[B] = MaybeMonad.flatMap(oa)(f)
}
```

If we import the implicit conversion `maybeMonad`, we can use `Maybe` in a for 
comprehension:

```scala
import MaybeMonad.maybeMonad

val a: Maybe[Int] =
  for {
    x <- Just(1)
    y  = x + 1
  } yield y

println("a: " + a) // a: Just(2)
```

Next, the `State` type and its monad:

```scala
type State[S,+A] = S => (A,S)

object StateMonad {

  implicit def stateMonad[S,A](sa: State[S,A]):
      Monad[({type λ[α] = State[S,α]})#λ,A] = new StateMonad[S,A](sa)

  def unit[S,A](a: A): State[S,A] = s => (a,s)
  def map[S,A,B](f: A => B)(sa: State[S,A]): State[S,B] =
    { s =>
      val (a,s2) = sa(s)
      val b = f(a)
      (b, s2)
    }
  def flatMap[S,A,B](sa: State[S,A])(f: A => State[S,B]): State[S,B] =
    { s =>
      val (a,s2) = sa(s)
      val bs = f(a)
      bs(s2)
    }
}

class StateMonad[S,A](sa: State[S,A])
    extends Monad[({type λ[α] = State[S,α]})#λ,A] {
  def map[B](f: A => B): State[S,B] = StateMonad.map(f)(sa)
  def flatMap[B](f: A => State[S,B]): State[S,B] = StateMonad.flatMap(sa)(f)
}
```

Similarly, we can use `State` in a for comprehension by importing `stateMonad`:

```scala
import StateMonad.stateMonad

val b: State[Int,Int] = {

  def get[S]: State[S,S] = s => (s,s)
  def set[S](s: S): State[S,Unit] = _ => ((),s)

  for {
    x <- get[Int]
    y  = x + 1
    _ <- set(5)
  } yield y
}

println("b(0): " + b(0)) // b(0): (1,5)
```

To use `Maybe` in conjunction with `State`, we need to abstract `State` into a 
monad transformer called `StateT`:

```scala
type StateT[S,A,F[_]] = S => F[(A,S)]

object StateTMonad {
  implicit def stateTMonad[S,A,F[_]](sa: StateT[S,A,F])
      (implicit m: F[(A,S)] => Monad[F,(A,S)]):
      Monad[({type λ[α] = StateT[S,α,F]})#λ,A] = new StateTMonad[S,A,F](sa)
}

class StateTMonad[S,A,F[_]](sa: StateT[S,A,F])
    (implicit m: F[(A,S)] => Monad[F,(A,S)])
    extends Monad[({type λ[α] = StateT[S,α,F]})#λ,A] {

  def map[B](f: A => B): StateT[S,B,F] =
    { s =>
      val fas: F[(A,S)] = sa(s)
      val fab: F[(B,S)] =
        m(fas).map {
          case (a,s2) =>
            val b = f(a)
            (b,s2)
        }
      fab
    }
  def flatMap[B](f: A => StateT[S,B,F]): StateT[S,B,F] =
    { s =>
      val fas: F[(A,S)] = sa(s)
      val fab: F[(B,S)] =
        m(fas).flatMap {
          case (a,s2) =>
            val bs = f(a)
            bs(s2)
        }
      fab
    }
}
```

Now with our three implicit conversions, we can put `Maybe` and `State` 
together:

```scala
import MaybeMonad.maybeMonad
import StateMonad.stateMonad
import StateTMonad.stateTMonad

val c: StateT[Int,Int,Maybe] = {

  def get[S]: StateT[S,S,Maybe] = s => Just((s,s))
  def set[S](s: S): StateT[S,Unit,Maybe] = _ => Just(((),s))

  def lift[S,A](oa: Maybe[A]): StateT[S,A,Maybe] = s => oa.map(a => (a,s))

  def remainder(a: Int, b: Int): Maybe[Int] =
    a % b match {
      case 0 => Nada
      case r => Just(r)
    }

  for {
    x <- get[Int]
    y <- lift[Int,Int](remainder(x, 2))
    _ <- set(5)
  } yield y
}

println("c(0): " + c(0)) // Nada
println("c(1): " + c(1)) // Just((1,5))
```
