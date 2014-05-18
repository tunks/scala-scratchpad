import scala.language.higherKinds
import scala.language.implicitConversions

trait Monad[F[_]] {
  def map[A,B](x: F[A])(f: A => B): F[B]
  def flatMap[A,B](x: F[A])(f: A => F[B]): F[B]
}

trait Monadic[F[_],A] {
  def map[B](f: A => B): F[B]
  def flatMap[B](f: A => F[B]): F[B]
}

case class StateT[F[_]: Monad,A,S](run: S => F[(A,S)])
    extends Monadic[({ type λ[α] = StateT[F,α,S] })#λ,A] {

  private val m: Monad[F] = implicitly[Monad[F]]

  def map[B](g: A => B): StateT[F,B,S] =
    StateT({ s => m.map(run(s))({ case (a,s2) => (g(a),s2) }) })

  def flatMap[B](g: A => StateT[F,B,S]): StateT[F,B,S] =
    StateT({ s => m.flatMap(run(s))({ case (a,s2) => g(a).run(s2) }) })

}

object OptionMonad extends Monad[Option] {
  def map[A,B](x: Option[A])(f: A => B): Option[B] = x map f
  def flatMap[A,B](x: Option[A])(f: A => Option[B]): Option[B] = x flatMap f
}

object Main extends App {

  implicit val optionMonad = OptionMonad

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
