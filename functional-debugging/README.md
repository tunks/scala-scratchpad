# Functional debugging in Scala

*Oct 31, 2013*

Printing to standard output might be one of the earliest debugging techniques we discover.  We write a program, sprinkle some `println` statements here and there, and look for where the program fails or what its runtime values are.

I still occasionally find myself reaching for this technique, sometimes in the form of a logging framework, to get basic runtime inspection of a program, but I find it hard to correlate log messages with program state, and even harder to consistently reproduce across development, testing, and production environments.  

Fortunately, the state monad offers a way out of this bind - we can have our logging and maintain referential transparency too!

Let's start with a side-effectey example.  Consider the following arithmetic functions:

```scala
val add: Int => Int => Int =
  x => y => x + y

val mult: Int => Int => Int =
  x => y => x * y

val div: Int => Int => Option[Double] =
  x => y => if (y == 0) None else Some(x.toDouble / y)

val parse: String => Option[Int] =
  x => try { Some(x.toInt) } catch { case t: Throwable => None }
```

The `add` and `mult` functions both take two integers and produce another integer.  The `div` and `parse` functions, which can fail (since they're only partially defined over their domains), both produce an `Option` of a number.

We can string these functions together, taking advantage of `Option`'s `map` and `flatMap` functions, to perform a compound operation:

```scala
for {
  x1 <- parse("42")
  x2  = mult(x1)(2)
  x3  = add(x2)(42)
  x4 <- div(x3)(3)
} yield x4  // returns Some(42.0)
```

We can also interleave some debugging statements using `println`:

```scala
for {
  x1 <- parse("42")
   _  = println("x1: " + x1)  // prints "x1: 42" to stdout
  x2  = mult(x1)(2)
   _  = println("x2: " + x2)  // prints "x2: 84" to stdout
  x3  = add(x2)(42)
   _  = println("x3: " + x3)  // prints "x3: 126" to stdout
  x4 <- div(x3)(3)
   _  = println("x4: " + x4)  // prints "x4: 42.0" to stdout
} yield x4                    // returns Some(42.0)
```

But we want to avoid both the side-effect of printing, and the dissociation of the result data from the log messages.

Let's build a special logging data structure that, when composed with `Option` instances, allows us to keep the same shape of our for comprehension, but return the log (along with the final result) as a sequence of log messages:

```scala
class LogOption[A](val run: Seq[String] => Option[(A, Seq[String])]) {
  def map[B](f: A => B): LogOption[B] =
    new LogOption({ l =>
      run(l) map { x => (f(x._1), x._2) }
    })
  def flatMap[B](f: A => LogOption[B]): LogOption[B] =
    new LogOption({ l =>
      run(l) flatMap { x => f(x._1).run(x._2) }
    })
}

implicit def logOption[A](x: Option[A]): LogOption[A] =
  new LogOption(l => x.map(a => (a, l)))

def log(x: String): LogOption[Unit] =
  new LogOption(log => Some(((), log ++ Seq(x))))
```

The `LogOption` class wraps a function that passes in a log and returns an optional pair of a result plus a modified log.  Such a function could be as simple as:

```scala
log => Some((42.0, "returning 42.0" +: log))
```

The `LogOption` class specifies how (via `map`) to apply a function to its result type, as well as how (via `flatMap`) to compose itself with a function thet returns another `LogOption`.

We can use it with only minor modification to the code above:

```scala
val x = for {
  x1 <- logOption(parse("42"))  // lift Option[Int] to LogOption[Int]
  _  <- log("x1: " + x1)
  x2  = mult(x1)(2)
  _  <- log("x2: " + x2)
  x3  = add(x2)(42)
  _  <- log("x3: " + x3)
  x4 <- lift(div(x3)(3))        // lift Option[Double] to LogOption[Double]
  _  <- log("x4: " + x4)
} yield x4
x.run(Nil)  // returns Some((42.0,List(x1: 42, x2: 84, x3: 126, x4: 42.0)))
```

Now we have a way to compose functions that return raw integers and `Option`s of integers, while building up a queue of log messages.  Nothing is written to standard output, no external state is altered, and in fact the code isn't even executed until it is initiated with an empty log via `x.run(Nil)`.

It turns out that `LogOption` is a specialization of the state monad transformer:

```scala
case class StateT[F[_],S,A](run: S => F[(A,S)])
type State[A] = StateT[ID,Seq[String],A]

private type StateTM[F[_],S,A] = Monad[({type λ[α] = StateT[F,S,α]})#λ,A]

implicit def stateT[F[_],S,A](x: StateT[F,S,A])
    (implicit lift: F[(A,S)] => Monad[F,(A,S)]): StateTM[F,S,A] =
  new StateTM[F,S,A] {
    def run(s: S): F[(A,S)] = x.run(s)
    def map[B](f: A => B): StateT[F,S,B] =
      new StateT[F,S,B]({ l => x.run(l) map { x => (f(x._1), x._2) } })
    def flatMap[B](f: A => StateT[F,S,B]): StateT[F,S,B] =
      new StateT[F,S,B]({ l => x.run(l) flatMap { x => f(x._1).run(x._2) } })
  }

type LogT[F[_],A] = StateT[F,Seq[String],A]
def logT[F[_],A](x: F[A])(implicit lift: F[A] => Monad[F,A]): LogT[F,A] =
  new LogT(l => x.map(a => (a, l)))

def log(x: String): LogT[Option,Unit] = new LogT(log => Some(((), log ++ Seq(x))))
```
