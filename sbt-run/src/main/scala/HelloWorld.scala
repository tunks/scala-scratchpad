package com.earldouglas

object HelloWorld {

  def run() = new Thread() {
    override def run() =
      while(true) {
        System.out.println("Hello, world!")
        Thread.sleep(2000)
      }
  }.start()

}
