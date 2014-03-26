package scalaci

import org.scalatest._

class HelloWorldTests extends FunSuite {

  test("HelloWorld") {

    object MockHelloWorld extends HelloWorld {
      var output: String = ""
      def readLn() = "James"
      def printLn(x: String) = { output = x }
    }

    assert(MockHelloWorld.output === "")

    MockHelloWorld(HelloWorld.greet)

    assert(MockHelloWorld.output === "Hello, James!")
  
  }
}
