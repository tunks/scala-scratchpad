package httpclient

import org.scalatest._

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import javax.servlet.Servlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class Tests extends FunSuite with BeforeAndAfterAll {

  var servers: Map[Int,Server] = Map.empty

  override def afterAll(): Unit =
    servers.values foreach { _.stop }

  def serve(context: String)(servlet: Servlet): (Server, Int) = synchronized {
    val port   = servers.size + 8080
    val server = new Server()
    servers = servers + (port -> server)

    val conn = new ServerConnector(server)
    conn.setHost("localhost")
    conn.setPort(port)
    server.addConnector(conn)
 
    val handler = new ServletContextHandler(server, "/", true, false)
    val holder = new ServletHolder(servlet)
    handler.addServlet(holder, context)
    server.setHandler(handler)
    server.start()

    (server, port)
  }

  def asStream(x: String): java.io.InputStream =
    new java.io.ByteArrayInputStream(x.getBytes("UTF-8"))

  def asString(x: java.io.InputStream): String =
    scala.io.Source.fromInputStream(x, "UTF-8").getLines().mkString("\n")

  test("GET /hello") {

    val (server, port) =
      serve("/hello")(
        new HttpServlet {
          override def doGet(req: HttpServletRequest, res: HttpServletResponse) =
            res.getOutputStream.write("Hello, world!".getBytes("UTF-8"))
        }
      )

    val req = Req(url = "http://localhost:" + port + "/hello")

    req { res =>
      assert(res.status === 200)
      assert((asString(res.body)) === "Hello, world!")
    }

  }

  test("redirect") {

    val (server, port) =
      serve("/redirect")(
        new HttpServlet {
          override def doGet(req: HttpServletRequest, res: HttpServletResponse) =
            res.sendRedirect("/overhere")
        }
      )

    val req = Req(url = "http://localhost:" + port + "/redirect")

    req { res =>
      assert(res.status === 302)
      assert(res.headers.get("Location") ===
               Some("http://localhost:" + port + "/overhere"))
    }

  }

  test("echo") {

    val (server, port) =
      serve("/echo")(
        new HttpServlet {
          import java.io._
          private def write(is: InputStream, os: OutputStream): Unit = {
            val buffer = Array.fill[Byte](1024)(0)
            var bytesRead: Int = is.read(buffer)
            while (bytesRead != -1) {
              os.write(buffer, 0, bytesRead)
              bytesRead = is.read(buffer)
            }
            is.close
          }
          override def doPost(req: HttpServletRequest, res: HttpServletResponse) = {
            res.setStatus(201)
            write(req.getInputStream, res.getOutputStream)
          }
        }
      )

    val req =
      Req(
        url = "http://localhost:" + port + "/echo",
        body = Some(() => asStream("foo bar"))
      )

    req { res =>
      assert(res.status === 201)
      assert(asString(res.body) === "foo bar")
    }

  }

}
