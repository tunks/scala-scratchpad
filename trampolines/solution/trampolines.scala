sealed trait Trampoline[+A]
case class Done[+A](a: A) extends Trampoline[A]
case class More[+A](f: () => Trampoline[A]) extends Trampoline[A]

object Trampoline {

  def done[A](a: A): Trampoline[A] =
    Done(a)

  def more[A](f: () => Trampoline[A]): Trampoline[A] =
    More(f)

  def run[A](t: Trampoline[A]): A =
    t match {
      case Done(a) => a
      case More(f) => run(f())
    }

}

object Main extends App {

  def odd(x: Int): Boolean =
    if (x == 0) false
    else even(x - 1)

  def even(x: Int): Boolean =
    if (x == 0) true
    else odd(x - 1)

  println(even(100)) // true
  println(even(101)) // false

  println(odd(100)) // false
  println(odd(101)) // true

  //println(even(100000)) // stack overflow

  import Trampoline.run

  def oddT(x: Int): Trampoline[Boolean] =
    if (x == 0) Done(false)
    else More(() => evenT(x - 1))

  def evenT(x: Int): Trampoline[Boolean] =
    if (x == 0) Done(true)
    else More(() => oddT(x - 1))

  println(run(evenT(100000))) // true
  println(run(evenT(100001))) // false

  println(run(oddT(100000))) // false
  println(run(oddT(100001))) // true

}
