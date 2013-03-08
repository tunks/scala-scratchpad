import java.io.Closeable
import java.io._

object Closer {
  implicit def closer[C <: Closeable, A](f: C => A): Closer[C, A] = new Closer(f)
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

sealed trait Stream[+A] {
  def show: String
  def map[B](f: A => B): Stream[B]
}

class Cons[A](val head: A, _tail: => Stream[A]) extends Stream[A] {
  lazy val tail = _tail
  def show = head.toString + tail.show
  def map[B](f: A => B): Stream[B] = new Cons(f(head), tail map f)
}

object Nil extends Stream[Nothing] {
  def show = ""
  def map[B](f: Nothing => B): Stream[B] = Nil
}

object Zapp extends App {

  import Closer._

  val cat: RandomAccessFile => Stream[String] =
    { x: RandomAccessFile =>
      println("***READING LINE***")
      Option(x.readLine()) match {
        case Some(y) => new Cons(y + "\n", cat(x))
        case None    => Nil
      }
    }

  val toBeginning = { c: RandomAccessFile => c.seek(0) }

  val printItTwice = for {
    s1 <- cat
    x1  = s1.show
    _   = println("FILE CONTENTS:")
    _   = println(x1)
    x2  = s1.show
    _   = println("FILE CONTENTS:")
    _   = println(x2)
    _  <- toBeginning
    s2 <- cat
    x3  = s2.show
    _   = println("FILE CONTENTS:")
    _   = println(x3)
  } yield Unit


  val file = new RandomAccessFile("file.txt", "r")
  printItTwice run file

}
