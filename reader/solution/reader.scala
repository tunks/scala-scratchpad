case class Reader[E,A](run: E => A) {

  def map[B](g: A => B): Reader[E,B] =
    Reader(e => g(run(e)))

  def flatMap[B](g: A => Reader[E,B]): Reader[E,B] =
    Reader(e => g(run(e)).run(e))

}

object Main extends App {

  def get(k: String): Reader[Map[String,Int],Int] =
    Reader(m => m(k))

  val resultR: Reader[Map[String,Int],Tuple4[Int,Int,Int,Int]] =
    for {
      foo <- get("foo")
      bar  = foo * 2
      baz <- get("baz")
      raz  = foo + bar
    } yield (foo,bar,baz,raz)

  println(resultR.run(Map("foo" -> 14, "baz" -> 2))) // (14,28,2,42)

}
