// Pause recursion with continuations

import scala.util.continuations._

object InterrupterJones {

  type dd = cpsParam[Double, Double]

  def calc() = reset(next())

  private var next: () => Double @dd =
    () => pi()

  private def pi(n: Int = 0): Double @dd =
    4 * math.pow(-1, n) / (2 * n + 1) + suspend(pi(n + 1))
  
  private def suspend(f: => Double @dd): Double @dd = 
    shift { k: (Double => Double) =>
      val curr = k(0.0)
      next = () => curr + f
      curr
    }
}

