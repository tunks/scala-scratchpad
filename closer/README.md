# Using `java.io.Closeable` as a Monad

_10 March 2013_

Working with instances of `java.io.Closeable`, which can implement a typeless `close()` method, can be tricky to work with in a functional style.  Let's walk through one way to wrap them up into something a little more composable and type safe.

To start, we'll write a function that wraps and executes closeable action:

```scala
def run[C <: Closeable, A](f: C = A, c: C): A = {
  val a = f(c)
  c.close()
  a
}
```

The `run` function takes a `Closeable`-handling function and a `Closeable` instance, runs the function, closes the `Closeable`, and returns the output of the function.

## Reading a file

Let's consider reading a text file.  We want to be able to compose functions that interact with the file, as well as functions that interact with the contents of the file.  We also want to make sure we only process as much of the file as needed, so we can make efficient use of memory and time.

## `java.io.RandomAccessFile`

Java gives us the `RandomAccessFile` class, which has some handy methods for arbitrary access to a file on disk.  A `RandomAccessFile` is a `Closeable`, so we need to make sure to `close()` it when we're done using the file.

## Composition

Let's find a way to compose a `Closeable`-handling function with a pure function that might operate on its return value.

```scala
def map[B, That](g: A => B): C => B = f andThen g
```

Pretty straightforward; our `f` above is composed with a new `g` that takes `f`'s output as its input, and returns something else.

Now let's compose a `Closeable`-handling function with another `Closeable`-handling function.

```scala
def flatMap[B](g: A => (C => B)): C => B = { c => (f andThen g)(c)(c) }
```

This appears to have a bit more going on, but all we're really doing is composing `f` with `g`, and passing along the `Closeable` instance where necessary.

Putting this all together, we get the `Closer` class.

```scala
class Closer[C <: Closeable, A](val f: C => A) {            

  def run(c: C): A = {
    val a = f(c)
    println("***CLOSING***")
    c.close()
    a
  }

  def map[B, That](g: A => B): C => B = f andThen g

  def flatMap[B](g: A => Closer[C, B]): C => B = { c => (f andThen g)(c).f(c) }

}
```

## Type class that `Closer`

It is useful to be able to transparently convert certain things into `Closer` instances, so let's write a couple of implicit functions to do so.

```scala
object Closer {                                             
  implicit def function1[C <: Closeable, A](f: C => A): Closer[C, A] = new Closer(f)
  implicit def generic[C <: Closeable, A, X[_]](x: X[A]): Closer[C, X[A]] = new Closer(_ => x)
}
```

The `function1` function will help when composing `Closeable`-handling functions, and the `generic` function will help when composing collections-handling functions (especially in `for` expressions).

## Examples

For the following examples, we'll use some common functions.

```scala
// opens a RandomAccessFile for reading
def file = {
  println("***OPENING***")
  new RandomAccessFile("jabberwocky.txt", "r")
}

// reads (if possible) a line from a RandomAccessFile, and sets up for reading
// the next line (if necessary)
val cat: RandomAccessFile => Stream[String] =
  { x =>
    println("***READING LINE***")
    Option(x.readLine()) match {
      case None       => empty
      case Some(line) => cons(line, cat(x))
    }
  }
```

### Print the first line

We can do this with a bunch of calls to `map`...

```scala
val closer = cat map { _.headOption } map { _ foreach println }
closer run file

/* Output:
***OPENING***
***READING LINE***
'Twas brillig, and the slithy toves
***CLOSING***
*/
```

...or with a `for` comprehension.

```scala
val closer =
  for {
    stream <- cat
      line <- stream.headOption
         _  = println(line)
  } yield Unit
  closer run file

/* Output:
***OPENING***
***READING LINE***
'Twas brillig, and the slithy toves
***CLOSING***
*/
```

### Print the first several lines

This acts like the Unix `head` command.

```scala
val closer =
  for {
    stream <- cat
     lines  = stream.take(4)
          _ = lines foreach println
  } yield Unit
  closer run file

/* Output:
***OPENING***
***READING LINE***
'Twas brillig, and the slithy toves
***READING LINE***
Did gyre and gimble in the wabe;
***READING LINE***
All mimsy were the borogoves,
***READING LINE***
And the mome raths outgrabe.
***CLOSING***
*/
```

### Print the entire file

Here we lazily read the file and print each line as we go.

```scala
val closer = cat map { _ foreach println }
closer run file
```

### Print the entire file twice

With this we can lazily read the file, print each line, then print each line again without re-reading the file.

```scala
val closer =
  for {
    stream <- cat
         _  = stream foreach println
         _  = stream foreach println
    } yield Unit
closer run file
```
