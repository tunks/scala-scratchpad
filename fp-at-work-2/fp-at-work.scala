case class State[S,A](run: S => (A,S)) {
 
  def map[B](f: A => B): State[S,B] =
    State { run andThen { case (a,s) => (f(a), s) } }

  def flatMap[B](f: A => State[S,B]): State[S,B] =
    State { run andThen { case (a,s) => f(a).run(s) } }

}

trait ATM {

  type Tx[A] = State[List[Float],A]

  def show(x: Float): String =
    (if (x < 0) "-" else "") + "$%.2f".format(math.abs(x))

  val balance: Tx[Float] =
    State { account => (account.sum, account) }

  def contribute(x: Float): Tx[Unit] =
    State { account => ((), account :+ x) }

  def deduct(x: Float): Tx[Float] =
    State { account =>
      if (account.sum >= x) (x, account :+ (-x))
      else (0, account)
    }

  def deposit(x: Float): Tx[(Float,Float)] = 
    for {
      _ <- contribute(x)
      b <- balance
    } yield (0,b)

  def withdraw(x: Float): Tx[(Float,Float)] =
    for {
      w <- deduct(x)
      b <- balance
    } yield (w,b)

  def depositThenWithdraw(d: Float, w: Float): Tx[(Float,Float)] =
    for {
      _ <- deposit(d)
      w <- withdraw(w)
    } yield w

}

object BankOfScalaATM extends ATM

object FirstRubyBankATM extends ATM {

  override def withdraw(x: Float): Tx[(Float,Float)] =
    for {
      w <- deduct(x)
      _ <- deduct(3) // $3.00 fee
      b <- balance
    } yield (w,b)

}

object Main extends App {

  import BankOfScalaATM._

  var account: List[Float] = Nil

  def run(x: Tx[(Float,Float)]): ((Float,Float),List[Float]) = {
    val ((w,b),a) = x.run(account)
    account = a
    ((w,b),a)
  }

  def demo(x: Tx[(Float,Float)]) = {
    val ((w,b),a) = run(x)
    println()
    println("* withdrew: " + show(w))
    println("* new balance: " + show(b))
    println("* account: " + a.map(show).mkString("[",",","]"))
    println()
  }

  println()
  println("# DEPOSIT $20.00:")
  demo(deposit(20))

  println("# WITHDRAW $100.00:")
  demo(withdraw(100))

  println("# DEPOSIT $20.00, WITHDRAW $20.00:")
  demo(depositThenWithdraw(20,20))

  println("# DEPOSIT $100.00, WITHDRAW $20.00, $3.00 FEE:")
  demo(FirstRubyBankATM.depositThenWithdraw(100,20))

}

