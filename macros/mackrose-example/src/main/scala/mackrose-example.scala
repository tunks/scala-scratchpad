package com.earldouglas.mackrose

object Example extends App {

  val one = mackrose2(
    { () =>
      println("Holy crap, I'm running at compile time!")
      println("It is now %s.".format(new java.util.Date().toString))
    },
    1
  )

  println("one: " + one)
}

