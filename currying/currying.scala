trait Curried {

  def fmap[A,B](xs: Seq[A])(f: A => B): Seq[B] = xs.map(f)

  fmap(List(1,2,3))(x => x + 1) // [2,3,4]

}

trait Unurried {

  def fmap[A,B](xs: Seq[A], f: A => B): Seq[B] = xs.map(f)

  // fails to compile: "missing parameter type" on "x =>"
  // fmap(List(1,2,3), x => x + 1)

  fmap(List(1,2,3), { x: Int => x + 1 } ) // [2,3,4]

}
