sealed trait List[+A]

object Cons {

  private case class _Cons[A](head: A, tail: List[A]) extends List[A]

  def apply[A](head: A, tail: List[A]): List[A] =
    _Cons.apply(head, tail)

  def unapply[A](cons: _Cons[A]) =
    _Cons.unapply(cons)

}

object Nil {

  private case object _Nil extends List[Nothing]

  def apply(): List[Nothing] =
    _Nil

  def unapply[A](list: List[A]): Boolean =
    list == _Nil

}

object Main extends App {

  def sum(xs: List[Int], acc: Int = 0): Int =
    xs match {
      case Cons(h, t) => sum(t, acc + h)
      case Nil()      => acc
    }

  println(sum(Cons(1, Cons(2, Cons(3, Cons(4, Nil()))))))
}
