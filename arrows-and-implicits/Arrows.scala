trait Arrow[A[_,_]] {
  def arr[B,C](f: B => C): A[B,C]
  def >>>[B,C,D](f: B => C, g: C => D): A[B,D]
  def first[B,C,D](f: B => C): A[(B,D),(C,D)]
  def second[B,C,D](f: B => C): A[(D,B),(D,C)]
  def ***[B,C,D,E](f: B => C, g: D => E): A[(B,D),(C,E)]
  def &&&[B,C,D](f: B => C, g: B => D): A[B,(C,D)]
}

class Fn1Arrow extends Arrow[Function1] {
  override def arr[B,C](f: B => C): B => C = f
  override def >>>[B,C,D](f: B => C, g: C => D): B => D = b => g(f(b))
  override def first[B,C,D](f: B => C): ((B,D)) => (C,D) = bd => (f(bd._1), bd._2)
  override def second[B,C,D](f: B => C): ((D,B)) => (D,C) = db => first(f)(db.swap).swap
  override def ***[B,C,D,E](f: B => C, g: D => E): ((B,D)) => (C,E) = bd => (f(bd._1), g(bd._2))
  override def &&&[B,C,D](f: B => C, g: B => D): B => (C,D) = b => ***(f, g)((b,b))
}

object ComplexNums {
  type Complex = (Int, Int)
  val show: Complex => String = x => "(" + x._1 + ", " + x._2 + ")"
  val add: Int => Int => Int = x => y => x + y
  def getC(): Complex = (7, 3)
  def twoCs(): Unit => (Complex, Complex) = Unit => (getC(), getC())
}

trait Fn1ArrowDemo {
  import ComplexNums._

  val arrow = new Fn1Arrow()

  private[this] val addC: ((Complex, Complex)) => Complex =
    xy => arrow.***(add(xy._1._1), add(xy._1._2))(xy._2._1, xy._2._2)

  val fn1ArrowDemo = arrow.>>>(twoCs(), arrow.>>>(addC, arrow.>>>(show, println)))

  print("fn1ArrowDemo() = ")
  fn1ArrowDemo()
}

class InfixFn1Arrow[B,C](f: B => C) {
  def arr = f
  def >>>[D](g: C => D) = (b: B) => g(f(b))
  def first[D](bd: (B,D)) = (f(bd._1), bd._2)
  def second[D](db: (D,B)) = first(db.swap).swap
  def ***[D,E](g: D => E) = (bd: (B,D)) => (f(bd._1), g(bd._2))
  def &&&[D](g: B => D) = (b: B) => ***(g)((b,b))
}

trait InfixFn1ArrowDemo {
  import ComplexNums._

  implicit def f1ToArrow[B,C](f: B => C): InfixFn1Arrow[B,C]    = new InfixFn1Arrow(f)
  implicit def fn0ToArrow[C](f: => C):    InfixFn1Arrow[Unit,C] = new InfixFn1Arrow(Unit => f)

  private[this] val addC: ((Complex, Complex)) => Complex =
    xy => (add(xy._1._1) *** add(xy._1._2))(xy._2._1, xy._2._2)

  val infixFn1ArrowDemo = (getC(), getC()) >>> addC >>> show >>> println

  print("infixFn1ArrowDemo() = ")
  infixFn1ArrowDemo()
}

// Demo

object Demo extends App
               with Fn1ArrowDemo
               with InfixFn1ArrowDemo
