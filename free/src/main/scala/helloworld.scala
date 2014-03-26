package scalaci

import scalaz._
import Scalaz._
import Free._

trait Console[+A]
case class ReadLn[A](k: String => A) extends Console[A]
case class PrintLn[A](s: String, k: () => A) extends Console[A]

object Console {

  implicit object ConsoleFunctor extends Functor[Console] {
    override def map[A,B](c: Console[A])(f: A => B): Console[B] =
      c match {
        case ReadLn(k)    => ReadLn(s => f(k(s)))
        case PrintLn(s,k) => PrintLn(s, () => f(k()))
      }
  }

  val readLn: Free[Console,String] =
    Suspend(ReadLn(s => Return(s)))

  def printLn(s: String): Free[Console,Unit] =
    Suspend(PrintLn(s, () => Return(())))

}

object HelloWorld {

  import Console._

  val greet: Free[Console,Unit] =
    for {
      _ <- printLn("What is your name?")
      n <- readLn
      _ <- printLn("Hello, " + n + "!")
    } yield ()
}

trait HelloWorld {

  def readLn(): String
  def printLn(x: String): Unit

  private val run: Console ~> Id = new (Console ~> Id) {
    def apply[B](c: Console[B]): B =
      c match { 
        case ReadLn(k)    => k(readLn())
        case PrintLn(s,k) => printLn(s) ; k()
      }
    }

  def apply[A](c: Free[Console,A]): A = 
    c.runM(run.apply[Free[Console,A]])

}

class ConsoleHelloWorld extends HelloWorld {
  def readLn() = readLine()
  def printLn(x: String) = println(x)
}

object ConsoleHelloWorld extends App {

  (new ConsoleHelloWorld)(HelloWorld.greet)

}
