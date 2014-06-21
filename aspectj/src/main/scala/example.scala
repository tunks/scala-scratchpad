package example

object Main extends App {

  def add(a: Int, b: Int) = a + b

  def slowAdd(a: Int, b: Int) = {
    Thread.sleep(100)
    a + b
  }

  add(20, 22)
  add(41, 1)
  slowAdd(52, -10)

}
