package com.earldouglas.mackrose

object Example extends App {

  def foo(): String = "bar"

  val one: Int = mackrose2(
    { () =>
      // foo() // This blows up with 'Example is not an enclosing class`
      println("Holy crap, I'm running at compile time!")
      println("It is now %s.".format(new java.util.Date().toString))
    },
    1
  )

  println("one: " + one)
}

