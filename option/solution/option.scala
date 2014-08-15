sealed trait Option[A] {

  def map[B](f: A => B): Option[B] =
    this match {
      case Some(a) => Some(f(a))
      case None()  => None()
    }

  def flatMap[B](f: A => Option[B]): Option[B] =
    this match {
      case Some(a) => f(a)
      case None()  => None()
    }

  override def toString(): String =
    this match {
      case Some(a) => "Some(" + a + ")"
      case None()  => "None()"
    }

}

case class Some[A](a: A) extends Option[A]
case class None[A]() extends Option[A]

object Main extends App {

  def quotient(divisor: Int)(dividend: Int): Int =
    dividend / divisor

  def remainder(divisor: Int)(dividend: Int): Option[Int] =
    (dividend % divisor) match {
      case 0 => None()
      case r => Some(r)
    }

  val x1: Option[Int] = Some(42)
  val x2: Option[Int] = None()

  println(x1 map quotient(2))      // Some(21)
  println(x2 map quotient(2))      // None()

  println(x1 map quotient(4))      // Some(10)
  println(x2 map quotient(4))      // None()

  println(x1 flatMap remainder(2)) // None()
  println(x2 flatMap remainder(2)) // None()

  println(x1 flatMap remainder(4)) // Some(2)
  println(x2 flatMap remainder(4)) // None()

  val x3 =
    for {
      x <- Some(42)
      q  = quotient(2)(x)
      r <- remainder(4)(x)
    } yield (x,q,r)

  println(x3) // Some((42,21,2))

}
