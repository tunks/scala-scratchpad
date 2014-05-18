import scala.language.higherKinds
import scala.language.implicitConversions

trait Monad[F[_],A] {
  def map[B](f: A => B): F[B]
  def flatMap[B](f: A => F[B]): F[B]
}

case class StateT[F[_],A,S](run: S => F[(A,S)])
  (implicit m: F[(A,S)] => Monad[F,(A,S)]) {

  def map[B](g: A => B): S => F[(B,S)] =
    { s => m(run(s)) map { case (a,s2) => (g(a),s2) } }

  def flatMap[B](g: A => (S => F[(B,S)])): (S => F[(B,S)]) =
    { s => m(run(s)) flatMap { case (a,s2) => g(a)(s2) } }

}

object Main extends App {

  implicit class OptionMonad[A](x: Option[A])
    extends Monad[Option,A] {

    def map[B](f: A => B): Option[B] = x map f
    def flatMap[B](f: A => Option[B]): Option[B] = x flatMap f
  }

  implicit def stateTMonad[F[_],A,S](run: S => F[(A,S)])
    (implicit m: F[(A,S)] => Monad[F,(A,S)]) = StateT(run)
  

  def put[K,V](k: K, v: V): StateT[Option,V,Map[K,V]] =
    StateT(m => Some(v, m + (k -> v)))

  def get[K,V](k: K): StateT[Option,V,Map[K,V]] =
    StateT(m => m.get(k) map { v => (v,m) })

  def getAndDouble(k: String): StateT[Option,Int,Map[String,Int]] =
    StateT({ m => m.get(k) map { v => (v, m + (k -> v * 2)) } })

  val resultS: StateT[Option,Int,Map[String,Int]] =
    for {
      a <- getAndDouble("foo")
      b <- get("foo")
    } yield b

  println(resultS.run(Map("foo" -> 21))) // Some((42,Map(foo -> 42)))

  println(resultS.run(Map.empty)) // None

}
