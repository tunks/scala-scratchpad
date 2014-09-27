# A simple Web effect system for Scala

*September 26, 2014*

This example builds on our earlier [effect system][] to introduce HTTP support 
through the Servlet API.

Given our `Program` trait from before, and the `Effect` trait that extends it, 
we implement a few effects:

```scala
sealed trait Effect[A] extends Program[A]
case class Pure[A](a: A) extends Effect[A]
case class Log(msg: String) extends Effect[Unit]
case class SaveThing(x: String) extends Effect[Unit]
case object GetThings extends Effect[Seq[String]]
```

Given a database implementation, we can write our effects interpreter:

```scala
object DB {
  var things: List[String] = List("foo", "bar")
}

def runEffect[A](a: Effect[A]): A =
  a match {
    case Pure(a)      => a
    case Log(message) => println(s"[log] $message")
    case SaveThing(x) => DB.things = x :: DB.things
    case GetThings    => DB.things
  }
```

We also introduce a trait to represent different kinds of HTTP responses:

```scala
sealed trait Response
case class Mustache(view: String, model: Any = ()) extends Response
case class Redirect(url: String) extends Response
case class Error(code: Int, message: String) extends Response
```

Our goal is to represent Web services as instances of `Program[Response]`, and 
create a Servlet-based responder that can write an HTTP response given an
instance of `Response`.

```scala
def respond(x: Response): Unit =
  x match {
    case Mustache(view, model) => render(view, model)
    case Redirect(uri)         => redirect(uri)
    case e:Error               => render("error", e)
  }
```

Now we can build our `Program[Response]` structures:

```scala
object Programs {

  case class ThingsModel(things: Seq[String])

  val getThings: Program[Response] =
    for {
      things <- GetThings
    } yield Mustache("things", ThingsModel(things))

  def addThing(req: HReq): Program[Response] =
    for {
      thingO <- Pure(Option(req.getParameter("thing")))
      resp   <- thingO map { thing =>
                  for {
                    _ <- SaveThing(thing)
                    _ <- Log(s"saving thing: $thing")
                  } yield Redirect(req.getRequestURI)
                } getOrElse Pure(Error(400, "missing 'thing' parameter"))
    } yield resp

}
```

Finally, we can tie them to some endpoints:

```scala
class Demo extends HttpServlet {

  case class ThingsModel(things: Seq[String])

  override def doGet(req: HReq, res: HRes): Unit =
    (new Service(req, res)).service(Programs.getThings)

  override def doPost(req: HReq, res: HRes): Unit =
    (new Service(req, res)).service(Programs.addThing(req))

}
```

  [effect system]: https://github.com/earldouglas/scala-scratchpad/tree/master/effect-system#a-simple-effect-system-for-scala
