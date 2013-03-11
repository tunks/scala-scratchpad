import java.io.Closeable
import java.io._
import Stream._

object Closer {
  implicit def function1[C <: Closeable, A](f: C => A): Closer[C, A] = new Closer(f)
  implicit def generic[C <: Closeable, A, X[_]](x: X[A]): Closer[C, X[A]] = new Closer(_ => x)
}

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

object Zapp extends App {

  import Closer._
  import Stream._

  def file = {
    println("***OPENING***")
    new RandomAccessFile("jabberwocky.txt", "r")
  }

  val cat: RandomAccessFile => Stream[String] =
    { x =>
      println("***READING LINE***")
      Option(x.readLine()) match {
        case None       => empty
        case Some(line) => cons(line, cat(x))
      }
    }

  val rewind = { r: RandomAccessFile => r.seek(0) }

  def headIt {
    val closer = cat map { _.headOption } map { _ foreach println }
    closer run file
  }

  def headIt2 {
    val closer =
      for {
        stream <- cat
          line <- stream.headOption
             _  = println(line)
      } yield Unit
    closer run file
  }

  def takeIt {
    val closer =
      for {
        stream <- cat
         lines  = stream.take(4)
              _ = lines foreach println
      } yield Unit
    closer run file
  }

  def printIt {
    val closer = cat map { _ foreach println }
    closer run file
  }

  def printItTwice {
    val closer =
      for {
        stream <- cat
             _  = stream foreach println
             _  = stream foreach println
      } yield Unit
    closer run file
  }

}
