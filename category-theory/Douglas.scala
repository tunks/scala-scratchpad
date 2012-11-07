trait Monad[A, F[_]] {
  def map[B](f: A => B): F[B]
  def flatMap[B](f: A => F[B]): F[B]
}

class Reader[E, A](g: E => A) extends Monad[A, ({type λ[α] = E => α})#λ] {
  def map[B](f: A => B): E => B = { e => f(g(e)) }
  def flatMap[B](f: A => (E => B)): E => B = { e => f(g(e))(e) }
}

