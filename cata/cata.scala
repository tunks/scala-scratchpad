trait MyOption[+A] {

  import MyOption.some
  import MyOption.none

  // single abstract method
  def cata[X](some: A => X, none: => X): X

  def map[B](f: A => B): MyOption[B] =
    cata({ a => some(f(a)) }, none)

  def flatMap[B](f: A => MyOption[B]): MyOption[B] =
    cata(f, none)

  def getOrElse[AA >: A](e: => AA): AA =
    cata(identity, e)

  def filter(p: A => Boolean): MyOption[A] =
    cata({ a => if (p(a)) some(a) else none }, none)

  def foreach(f: A => Unit): Unit =
    cata(f, ())

  def isDefined: Boolean =
    cata({ _ => true }, false)

  def isEmpty: Boolean =
    cata({ _ => false }, true)

  // WARNING: not defined for None
  def get: A =
    cata(identity, error("not defined for none"))

  def orElse[AA >: A](o: MyOption[AA]): MyOption[AA] =
    cata({ a => some(a) }, o)

  def toLeft[X](right: => X): Either[A, X] =
    cata({ a => Left(a) }, Right(right))

  def toRight[X](left: => X): Either[X, A] =
    cata({ a => Right(a) }, Left(left))

  def toList: List[A] =
    cata({ a => List(a) }, Nil)

  def iterator: Iterator[A] =
    cata(
      { a =>
        new Iterator[A] {
          private var iterated = false
          def hasNext() = !iterated
          def next() =
            if (!iterated) {
              iterated = true
              a
            } else {
              throw new NoSuchElementException()
            }
          def remove() =
            throw new java.lang.UnsupportedOperationException()
        }
      },
      new Iterator[A] {
        def hasNext() = false
        def next() = throw new NoSuchElementException()
        def remove() =
          throw new java.lang.UnsupportedOperationException()
      }
    )
}

object MyOption {
  def none[A] = new MyOption[A] {
    def cata[X](s: A => X, n: => X) = n
  }

  def some[A](a: A) = new MyOption[A] {
    def cata[X](s: A => X, n: => X) = s(a)
  }
}
