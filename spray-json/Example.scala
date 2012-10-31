import cc.spray.json._
import cc.spray.json.DefaultJsonProtocol._

object Example extends App {
  
  val rawJson = """{ "some": "JSON source" }"""
  println("parsing a raw JSON string: " + rawJson)
  val jsonAst: JsValue = JsonParser(rawJson)
  println(jsonAst)
  println()
  
  println("parsing a JSON AST: " + jsonAst)
  println(PrettyPrinter(jsonAst))
  println()
  
  println("converting tuple to a JSON AST: " + """(1, "one")""")
  val jsonAst2 = (1, "one").toJson
  println(jsonAst2)
  println()
  
  println("converting a JSON AST to a tuple: " + jsonAst2)
  val tuple = jsonAst2.fromJson[Tuple2[Int, String]]
  println(tuple)
  println()
  
  println("converting a case class to a JSON AST: case class Foo(bar: Int, baz: String)")
  case class Foo(bar: Int, baz: String)
  object FooFormat extends JsonFormat[Foo] {
    def write(x: Foo) = JsArray(JsNumber(x.bar), JsString(x.baz))
    def read(value: JsValue) = value match {
      case JsArray(bar :: baz :: nil) =>
        Foo(IntJsonFormat.read(bar), StringJsonFormat.read(baz))
      case _ => throw new DeserializationException("Foo expected")
    }
  }
  val foo = Foo(2, "two")
  val jsonAst3 = foo.toJson(FooFormat)
  println(jsonAst3)
  println()
}
