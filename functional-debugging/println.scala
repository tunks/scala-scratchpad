package functionaldebugging

object Arithmetic {

  val add: Int => Int => Int =
    x => y => x + y

  val mult: Int => Int => Int =
    x => y => x * y

  val div: Int => Int => Option[Double] =
    x => y => if (y == 0) None else Some(x.toDouble / y)

  val parse: String => Option[Int] =
    x => try { Some(x.toInt) } catch { case t: Throwable => None }

}

trait SideEffects {

  import Arithmetic._

  lazy val result =
    for {
      x1 <- parse("42")
       _  = println("x1: " + x1)
      x2  = mult(x1)(2)
       _  = println("x2: " + x2)
      x3  = add(x2)(42)
       _  = println("x3: " + x3)
      x4 <- div(x3)(3)
       _  = println("x4: " + x4)
    } yield x4
  // [stdout] x1: 42
  // [stdout] x2: 84
  // [stdout] x3: 126
  // [stdout] x4: 42.0
  // Some(42.0)

}

object StateLog {

  def pure[A](a: A): LogOption[A] = new LogOption(log => Some((a, log)))
  def log(x: String): LogOption[Unit] = new LogOption(log => Some(((), log ++ Seq(x))))

  class LogOption[A](g: Seq[String] => Option[(A, Seq[String])]) {
    def run(log: Seq[String]) = g(log)
    def map[B](f: A => B): LogOption[B] =
      new LogOption({ l =>
        g(l) map { x => (f(x._1), x._2) }
      })
    def flatMap[B](f: A => LogOption[B]): LogOption[B] =
      new LogOption({ l =>
        g(l) flatMap { x => f(x._1).run(x._2) }
      })
  }

  implicit def lift[A](x: Option[A]): LogOption[A] =
    new LogOption(l => x.map(a => (a, l)))

}

trait NoSideEffects {

  import Arithmetic._
  import StateLog._

  lazy val resultM =
    for {
      x1 <- lift(parse("42"))
      _  <- log("x1: " + x1)
      x2  = mult(x1)(2)
      _  <- log("x2: " + x2)
      x3  = add(x2)(42)
      _  <- log("x3: " + x3)
      x4 <- lift(div(x3)(3))
      _  <- log("x4: " + x4)
    } yield x4

  lazy val result = resultM.run(Nil)
  // Some((42.0,List(x1: 42, x2: 84, x3: 126, x4: 42.0)))

}
