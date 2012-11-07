// An algebraic data type for I/O

/* http://vimeo.com/20293743 */

sealed trait IO[A]
case class Const[A](a: A) extends IO[A]
case object ReadLn extends IO[String]
case class WriteLn(s: String) extends IO[Unit]
case class Compose[A,B](a: IO[A], b: A => IO[B]) extends IO[B]

def readThenWrite: IO[Unit] = Compose(ReadLn, WriteLn)

// a side-effecty interpreter
def run1[A](program: IO[A]): A = program match {
  case ReadLn => readLine
  case WriteLn(s) => println(s)
  case Const(a) => a
  case Compose(a, f) => run1(f(run1(a)))
}

// a pure (side-effect free) intepreter, e.g. for testing
def run2[A](program: IO[A], in: List[String], out: List[String]): (A, List[String], List[String]) = program match {
  case ReadLn => (in.head, in.tail, out)
  case WriteLn(s) => ((), in, s :: out)
  case Const(s) => (s, in, out)
  case Compose(a, f) =>
    val (a1, in1, out1) = run2(a, in, out)
    run2(f(a1), in1, out1)
}

