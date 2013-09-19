package tryt

object `package` extends App {
  
  import scala.util.{Try,Success,Failure}
  
  trait Functor[F[_],A] {
    def map[B](f: A => B): F[B]
  }

  def fgFunctor[F[_],G[_],A](fg: F[G[A]])(implicit ff: F[G[A]] => Functor[F,G[A]], gf: G[A] => Functor[G,A]) =
    new Functor[({ type λ[α] = F[G[α]] })#λ,A] {
      def map[B](f: A => B): F[G[B]] = fg map { ga => ga map f }
    }

  implicit def listFunctor[A](xs: List[A]): Functor[List,A] =
    new Functor[List,A] {
      def map[B](f: A => B) = xs map f
    }

  implicit def tryFunctor[A](t: Try[A]): Functor[Try,A] =
    new Functor[Try,A] {
      def map[B](f: A => B) = t map f
    }

  case class TryT[F[_],T](f: F[Try[T]])

  implicit def tryTFunctor[F[_],T](t: TryT[F,T])(implicit f: F[Try[T]] => Functor[F,Try[T]]) = fgFunctor[F,Try,T](t.f)

  val t1: Try[Int] = Success(1)
  val t2: Try[Int] = Failure(new Exception("meh"))
  val ts: List[Try[Int]] = List(t1,t2)
  val tt: TryT[List,Int] = TryT(ts)
  
  val plusOne: Int => Int = x => x + 1
  def repeat[A]: Try[A] => List[Try[A]] = t => List(t,t)

  val tt1 = tt map plusOne

  println(tt1) // List(Success(2), Failure(java.lang.Exception: meh))
  
}
