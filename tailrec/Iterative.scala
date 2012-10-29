/*
To evaluate the expression (1 + 2) * (3 - 4):

scala> iterative(List(1.,2.,"+",3.,4.,"-","*"))
res0: Double = -3.0
*/

def iterative(elements: List[Any]): Double = {
  var queue: List[Double] = Nil

  for (e <- elements) e match {
    case operand: Double   => queue = operand :: queue
    case operation: String => val result = operation match {
        case "+" => queue.tail.head + queue.head
        case "-" => queue.tail.head - queue.head
        case "*" => queue.tail.head * queue.head
        case "/" => queue.tail.head / queue.head
      }
      queue = result :: queue.tail.tail
  }

  queue.head
}

