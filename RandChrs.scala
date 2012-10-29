// Random string generator

def randChrs(alphabet: Seq[Char]): Iterator[Char] = {
  val sr: java.security.SecureRandom = new java.security.SecureRandom()
  def stream: Stream[Char] = {
    val idx = (sr.nextFloat() * alphabet.size).toInt
    new scala.collection.immutable.Stream.Cons(alphabet(idx), stream)
  }
  stream.iterator
}

val cs = randChrs(('A' to 'Z') ++ ('0' to '9'))

val myRandomString = cs.take(10).mkString("")

