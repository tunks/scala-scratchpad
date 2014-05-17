object Implicits {

  implicit class RichValue[A](val x: A) extends AnyVal {
    def -->[B](f: A => B): B = f(x)
  }

  implicit class RichTuple2[A,B](val x: Tuple2[A,B]) extends AnyVal {
    def map[C,D](f: (A,B) => (C,D)): Tuple2[C,D] = f(x._1, x._2)
    def mapFst[C](f: A => C): Tuple2[C,B] = (f(x._1), x._2)
    def mapSnd[C](f: B => C): Tuple2[A,C] = (x._1, f(x._2))
  }

}

case class State[A,S](run: S => (A,S)) {

  import Implicits.RichTuple2

  def map[B](g: A => B): State[B,S] =
    State(s => run(s) mapFst g)

  def flatMap[B](g: A => State[B,S]): State[B,S] =
    State(s => run(s) map { (a,s2) => g(a).run(s2) })

}

object Main extends App {

  def put(k: String, v: Int): State[Int, Map[String,Int]] =
    State(m => (v, m + (k -> v)))

  def get(k: String): State[Int, Map[String,Int]] =
    State(m => (m(k), m))

  def getAndDouble(k: String): State[Int, Map[String,Int]] =
    State({ m =>
      val v = m(k)
      (v, m + (k -> v * 2))
    })

  val resultS: State[Tuple5[Int,Int,Int,Int,Int], Map[String,Int]] =
    for {
      a <- put("foo", 21)      // a = 21, state = Map(foo -> 21)
      b <- get("foo")          // b = 21, state = Map(foo -> 21)
      c <- getAndDouble("foo") // c = 21, state = Map(foo -> 42)
      d <- getAndDouble("foo") // d = 42, state = Map(foo -> 84)
      e <- get("foo")          // e = 84, state = Map(foo -> 84)
    } yield (a,b,c,d,e)

  println(resultS.run(Map.empty)) // ((0,21,21,42,84),Map(foo -> 84))

}
