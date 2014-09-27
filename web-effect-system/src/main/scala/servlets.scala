package servlets

import javax.servlet.http.HttpServlet
import javax.servlet.http.{ HttpServletRequest => HReq }
import javax.servlet.http.{ HttpServletResponse => HRes }
import effects._

sealed trait Response
case class Mustache(view: String, model: Any = ()) extends Response
case class Redirect(uri: String) extends Response
case class Error(code: Int, message: String) extends Response

object DB {
  var things: List[String] = List("foo", "bar")
}

class Service(req: HReq, res: HRes) extends ProgramRunner {

  private val mf = {
    val dmf = new com.github.mustachejava.DefaultMustacheFactory()
    dmf.setObjectHandler(new com.twitter.mustache.ScalaObjectHandler)
    dmf
  }

  override def runEffect[A](a: Effect[A]): A =
    a match {
      case Pure(a)      => a
      case Log(message) => println(s"[log] $message")
      case SaveThing(x) => DB.things = x :: DB.things
      case GetThings    => DB.things
    }

  def respond(x: Response): Unit =
    x match {
      case Mustache(view, model) => render(view, model)
      case Redirect(uri)         => redirect(uri)
      case e:Error               => render("error", e)
    }

  private def redirect(uri: String): Unit =
    res.sendRedirect(uri)

  private def render(view: String, model: Any): Unit = {
    res.setContentType("text/html")
    res.setCharacterEncoding("UTF-8")
    val is = getClass.getClassLoader.getResourceAsStream(view + ".mustache")
    val reader = new java.io.InputStreamReader(is)
    val mustache = mf.compile(reader, view)
    mustache.execute(res.getWriter, model)
  }

  def service(p: Program[Response]): Unit = respond(runProgram(p))

}

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
 
class Demo extends HttpServlet {

  case class ThingsModel(things: Seq[String])

  override def doGet(req: HReq, res: HRes): Unit =
    (new Service(req, res)).service(Programs.getThings)

  override def doPost(req: HReq, res: HRes): Unit =
    (new Service(req, res)).service(Programs.addThing(req))

}
