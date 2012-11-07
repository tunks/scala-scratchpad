// KryptoSolver clumsily finds solutions to Krypto puzzles using brute force. Usage: KryptoSolver.solve(List(24,6,6,17,8),4) 

object KryptoSolver {

  def solve(v: List[Int], result: Int): List[List[Any]] = {
    for (operands <- permutations(v).distinct;
         operations <- operationCombos;
         expression <- expressions(operands, operations) if (safeEquals(expression, result))) yield expression
  }

  def expressions(operands: List[Int], operations: List[String]): List[List[Any]] = {
    val ns = operands.toArray
    val os = operations.toArray
    List(ns(0), ns(1), ns(2), ns(3), os(0), ns(4), os(1), os(2), os(3)) ::
    List(ns(0), ns(1), ns(2), os(0), ns(3), ns(4), os(1), os(2), os(3)) ::
    List(ns(0), ns(1), os(0), ns(2), os(1), ns(3), ns(4), os(2), os(3)) ::
    List(ns(0), ns(1), os(0), ns(2), os(1), ns(3), os(2), ns(4), os(3)) ::
    List(ns(0), ns(1), ns(2), ns(3), ns(4), os(0), os(1), os(2), os(3)) ::
    List(ns(0), ns(1), ns(2), os(0), ns(3), os(1), os(2), ns(4), os(3)) ::
    List(ns(0), ns(1), ns(2), os(0), os(1), ns(3), ns(4), os(2), os(3)) ::
    List(ns(0), ns(1), ns(2), os(0), os(1), ns(3), os(2), ns(4), os(3)) ::
    List(ns(0), ns(1), os(0), ns(2), ns(3), ns(4), os(1), os(2), os(3)) ::
    List(ns(0), ns(1), ns(2), os(0), ns(3), os(1), ns(4), os(2), os(3)) ::
    List(ns(0), ns(1), ns(2), ns(3), os(0), os(1), ns(4), os(2), os(3)) ::
    List(ns(0), ns(1), ns(2), ns(3), os(0), os(1), os(2), ns(4), os(3)) ::
    List(ns(0), ns(1), os(0), ns(2), ns(3), os(1), ns(4), os(2), os(3)) ::
    List(ns(0), ns(1), os(0), ns(2), ns(3), os(1), os(2), ns(4), os(3)) :: Nil
  }

  def safeEquals(expression: List[Any], result: Int) = {
    try {
      compute(expression) == result
    } catch {
      case _ => false
    }
  }
  
  @annotation.tailrec final def compute(expression: List[Any], queue: List[Int] = Nil): Int = {
    expression.head match {
      case operand: Int      => if (expression.tail == Nil) operand else compute(expression.tail, operand :: queue)
      case operation: String => val result = operation match {
          case "+" => queue.tail.head + queue.head
          case "-" => queue.tail.head - queue.head
          case "*" => queue.tail.head * queue.head
          case "/" => if (queue.tail.head % queue.head == 0) queue.tail.head / queue.head else throw new Exception
        }
        compute(result :: expression.tail, queue.tail.tail)
    }
  }

  val operationCombos: List[List[String]] = {
    val operations: List[String] = List("+", "-", "*", "/")
    for (o1 <- operations; o2 <- operations; o3 <- operations; o4 <- operations) yield List(o1, o2, o3, o4)
  }
  
  def permutations[A](xs: List[A]): List[List[A]] = xs match {
    case Nil => List(Nil)
    case _   => for(x <- xs; ys <- permutations(xs diff List(x))) yield x :: ys
  }
}

