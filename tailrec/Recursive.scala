/*
To evaluate the expression (1 + 2) * (3 - 4):

scala> recursive(List(1.,2.,"+",3.,4.,"-","*"))
res1: Double = -3.0
*/

@scala.annotation.tailrec def recursive(expression: List[Any], queue: List[Double] = Nil): Double = {
  expression.head match {
    case operand: Double   => if (expression.tail == Nil) operand else recursive(expression.tail, operand :: queue)
    case operation: String => val result = operation match {
        case "+" => queue.tail.head + queue.head
        case "-" => queue.tail.head - queue.head
        case "*" => queue.tail.head * queue.head
        case "/" => queue.tail.head / queue.head
      }
      recursive(result :: expression.tail, queue.tail.tail)
  }
}
