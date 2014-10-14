% Putting functional programming to work
% James Earl Douglas<br />[@jearldouglas](https://twitter.com/jearldouglas)
% October 13, 2014<br />Scala Bay

# Abstract

* We like our code to be safe, composable, and reusable
* Functional programming&#x002a; can make it happen
* We can use FP to achieve these goals in real-world&#x002a;&#x002a; code

*This is an adaptation of [Real-World Functional Programming <span style="white-space:nowrap">(bit.ly/real-world-fp)</span>](http://bit.ly/real-world-fp).*

# Words mean stuff

*Functional programming&#x002a;*

&#x002a; consider FP &#x2248; referential transparency

*Real-world&#x002a;&#x002a;*

&#x002a;&#x002a; for readability, we'll use a simplified (but RW analogous) example

# Problem space

Consider a banking ATM.  A user should be able to:

* Check their balance
* Deposit funds
* Withdraw funds (and charge a fee)

# Ugh, another ATM example?

Accurate handling of state is kind of important, because money.

# Data types

We'll track a bank account as a simple transaction register.

* The type of each individual contribution and deduction is `Float`
* The type of an account is `List[Float]`

# State change as a side effect

```scala
var account: List[Float] = ...

def deposit(x: Float): Float = {
  account = account :+ x
  account.sum
}

def withdraw(x: Float): Float = {
  account = account :+ (-x)
  account.sum
}
```

## Drawbacks

* Mutable: `account` reference can change any time
* Imperative: evaluating `deposit` or `withdraw` makes the transaction happen
* Inconsistent: `account` reference is accessed multiple times

# State change as a computational effect

```scala
def deposit(x: Float): List[Float] => (Float, List[Float]) =
  { account => (account.sum, account :+ x) }

def withdraw(x: Float): List[Float] => (Float, List[Float]) =
  { account => (account.sum, account :+ (-x)) }
```

## Improvements

```scala
def deposit(x: Float): List[Float] => (Float, List[Float]) =
  { account => (account.sum, account :+ x) }

def withdraw(x: Float): List[Float] => (Float, List[Float]) =
  { account => (account.sum, account :+ (-x)) }
```

* Immutable: `account` reference can not change
* Declarative: evaluating `deposit` or `withdraw` does not run the transaction
* Consistent: `account` accesses are certain to be identical

# Representing a state action

```scala
case class State[S,A](run: S => (A,S))
```

A `State` wraps a function `run` which, given a state `S`, performs some computation and produces both an `A` and a new state `S`.

# Composing with pure functions

```scala
case class State[S,A](run: S => (A,S)) {

  def map[B](f: A => B): State[S,B] =
    State { run andThen { case (a,s) => (f(a), s) } }

}
```

`map` builds a new state action that, once run, has its output run through `f`, converting it from an `A` to a `B`.

# Composing with pure functions

```scala
type Tx[A] = State[List[Float],A]

val balance: Tx[Float] =
  State { account => (account.sum, account) }

val report: Tx[String] =
  balance map { x: Float => "Your balance is " + x }
```

# Composing with other state actions

```scala
case class State[S,A](run: S => (A,S)) {

  def flatMap[B](f: A => State[S,B]): State[S,B] =
    State { run andThen { case (a,s) => f(a).run(s) } }

}
```

`flatMap` builds a new state action that, once run, uses the output to compute the result of another state action.

# Composing with state actions

```scala
def deduct(x: Float): Tx[Float] =
  State { account =>
    if (account.sum >= x) (x, account :+ (-x))
    else (0, account)
  }

def deductWithFee(x: Float]: Tx[Float] =
  deduct(x) flatMap { y =>
    State { account =>
      val fee = 3
      (y + fee, account :+ (-fee))
    }
  }
```

# The state monad

```scala
case class State[S,A](run: S => (A,S)) {
 
  def map[B](f: A => B): State[S,B] =
    State { run andThen { case (a,s) => (f(a), s) } }

  def flatMap[B](f: A => State[S,B]): State[S,B] =
    State { run andThen { case (a,s) => f(a).run(s) } }

}
```

# The claims

* Safe
* Composable
* Reusable

# Safety

```scala
def contribute(x: Float): Tx[Unit] =
  State { account => ((), account :+ x) }
```

* No mutable references (i.e. `var account = ...`)
* No mutable data structures (e.g. `account.append(...)`)
* State actions are necessarily atomic
* State actions are optionally transactional&#x002a;

*&#x002a; Depending on the later implementation of an interpreter*

# Composability

```scala
def deposit(x: Float): Tx[(Float,Float)] = 
  for {
    _ <- contribute(x) // Tx[Unit]
    b <- balance       // Tx[Float]
  } yield (0,b)

def withdraw(x: Float): Tx[(Float,Float)] =
  for {
    w <- deduct(x)     // Tx[Float]
    b <- balance       // Tx[Float]
  } yield (w,b)
```

* Big state actions can be constructed from little state actions
* Composition induces no side effects
* Composition triggers no state transitions

# Reusability

```scala
 def depositThenWithdraw(d: Float, w: Float): Tx[(Float,Float)] =
  for {
    _ <- deposit(d)  // Tx[(Float,Float)]
    w <- withdraw(w) // Tx[(Float,Float)]
  } yield w
```

* State actions can be composed in different ways to create different behavior

# Interpreter

To make it go, we need a way to run a state action:

```scala
def run(x: Tx[(Float,Float)]): ((Float,Float),List[Float]) = {
  db.beginTransaction()
  val account = db.getAccount(...)
  val ((w,b),account2) = x.run(account)
  db.updateAccount(account2)
  db.commitTransaction(...)
  ((w,b),account2)
}
```

# Problems

* Synchronizing access to mutable references
* Wrapping state actions in transactions

# Solutions

We can't escape mutability, but we *can* push it to the outer edges of our program, and tailor the interpreter to the mechanism of state persistence.

# Demo

<script src="//scalave.earldouglas.com/scalave.js">

  case class State[S,A](run: S => (A,S)) {
    def map[B](f: A => B): State[S,B] =
      State { run andThen { case (a,s) => (f(a), s) } }
    def flatMap[B](f: A => State[S,B]): State[S,B] =
      State { run andThen { case (a,s) => f(a).run(s) } }
  }

  object Tx {
    def apply[A](run: List[Float] => (A, List[Float])) = State(run)
  }

  val action =
    for {
      _ <- Tx { account => ((), account :+ 100F) }
      x  = 20F
      w <- Tx { account =>
             if (account.sum >= x) (x, account :+ (-x))
             else (0F, account)
           }
      b <- Tx { account => (account.sum, account) }
    } yield (w,b)

  action.run(Nil)

</script>
