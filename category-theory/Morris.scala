/* http://blog.tmorris.net/monads-do-not-compose/ */

trait Functor[F[_]] {
  def fmap[A, B](f: A => B, a: F[A]): F[B]
}

trait Applicative[F[_]] extends Functor[F] {
  def ap[A, B](f: F[A => B], a: F[A]): F[B]
  def point[A](a: A): F[A]
  override final def fmap[A, B](f: A => B, a: F[A]) =
    ap(point(f), a)
}

trait Monad[F[_]] extends Applicative[F] {
  def flatMap[A, B](f: A => F[B], a: F[A]): F[B]
  override final def ap[A, B](f: F[A => B], a: F[A]) =
    flatMap((ff: A => B) => fmap((aa: A) => ff(aa), a), f)
}
