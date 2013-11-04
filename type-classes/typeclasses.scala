package typeclasses

trait Showable {
  def show: String
}
 
object Stdout {
  def printLine(x: Showable): Unit = println(x.show)
}
 
object Main1 extends App {
  Stdout.printLine(42) // fails to compile, because 42 is an Int, and Int does not extend Showable
}
 
object Main2 extends App {

  implicit class IntShowable(x: Int) extends Showable {
    def show: String = x.toString
  }

  Stdout.printLine(42) // prints "42" to stdout
}
