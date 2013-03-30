package com.earldouglas.constructive

object Example extends App {

  //val sq = constructive1({ x: Int => x * x * x }, (5,25))
  // fails to compile, because 5 * 5 * 5 != 25

  // def _sq(x: Int): Int = x * x
  // val sq = constructive1(_sq, (5,25))
  // fails to compile, because scalac gets lost

  val sq = constructive1({ x: Int => x * x }, (5,25))
  // compiles, because 5 * 5 == 25

  println("the square of 5 is %d".format(sq(5)))

}
