package com.earldouglas.unittest

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object `package` {
  type Constructive1[A, B] = (A => B, (A => B) => Boolean)
  type Constructive2[A, B, C] = ((A, B) => C, ((A, B) => C) => Boolean)

  def constructive1[A, B](f: A => B)(g: (A => B) => Boolean) = f
  def constructive2[A, B, C](f: (A, B) => C)(g: ((A, B) => C) => Boolean) = f
}

object Math extends App {

  val sq = constructive1
    { x: Int => x * x }
    { f =>
        f(1) == 1  &&
        f(0) == 0  &&
        f(4) == 16
    }

  val add = constructive2
    { (x: Int, y: Int) => x + y }
    { f =>
        f(0, 1) == 1  &&
        f(1, 0) == 1  &&
        f(4,14) == 18
    }


  println("sq(7) = %d".format(sq(7)))
  println("add(3, 5) = %d".format(add(3, 5)))

}

