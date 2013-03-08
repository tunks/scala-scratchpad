package com.earldouglas.slack.tests

import org.scalatest._

object EchoServer {

  import com.sun.net.httpserver._
  import java.util.concurrent.Executors
  import scala.collection.JavaConversions._

  private var serverO: Option[HttpServer] = None

  def start() = synchronized {
    val server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(8553), 0)
    server.createContext("/echo", new HttpHandler() {
      override def handle(exchange: HttpExchange) {
        val path = exchange.getRequestURI.getPath
        exchange.getResponseHeaders().clear()
        exchange.getResponseHeaders().set("Content-type", "text/plain")
        exchange.sendResponseHeaders(200, 0)
        val responseBody = exchange.getResponseBody()
        responseBody.write((exchange.getRequestMethod() + " " + exchange.getRequestURI.toString).getBytes)
        exchange.getRequestHeaders().foreach { h => responseBody.write(("\n" + h._1 + ": " + h._2.mkString(",")).getBytes) }
        responseBody.write(scala.io.Source.fromInputStream(exchange.getRequestBody()).getLines().mkString("\n", "\n", "").getBytes)
        responseBody.close()
      }
    })
    server.setExecutor(Executors.newCachedThreadPool())

    serverO = Some(server)
    serverO foreach { _.start() }
  }

  def stop() = synchronized {
    serverO foreach { _.stop(0) }
  }
}

class Tests extends FunSuite with BeforeAndAfterAll {

  override def beforeAll() { EchoServer.start() }
  override def  afterAll() {  EchoServer.stop() }

  import com.earldouglas.slack._
  import java.io.ByteArrayInputStream

  test("/echo") {
    assert(("http://localhost:8553/echo" get status) === 200)
    assert(("http://localhost:8553/echo" get headers) === Map("Transfer-encoding" -> "chunked",
                                                                   "Content-type" -> "text/plain"))
    assert(("http://localhost:8553/echo" get header("content-type")) === Some("text/plain"))
    assert(("http://localhost:8553/echo" get header("CoNtEnT-TyPe")) === Some("text/plain"))
    assert(("http://localhost:8553/echo" get header("Type-content")) === None)

    assert(("http://localhost:8553/echo" post(new ByteArrayInputStream("Foo".getBytes), 3) content) ===
            """POST /echo
              |Host: localhost:8553
              |Content-length: 3
              |Connection: Keep-Alive
              |User-agent: Apache-HttpClient/4.2.1 (java 1.5)
              |Foo""".stripMargin)

    assert(("http://localhost:8553/echo" post("Foo") content) ===
            """POST /echo
              |Host: localhost:8553
              |Content-length: 3
              |Connection: Keep-Alive
              |User-agent: Apache-HttpClient/4.2.1 (java 1.5)
              |Foo""".stripMargin)

    assert(("http://localhost:8553/echo" put("Foo") status) === 200)

    assert(("http://localhost:8553/echo" put("Foo") content) ===
            """PUT /echo
              |Host: localhost:8553
              |Content-length: 3
              |Connection: Keep-Alive
              |User-agent: Apache-HttpClient/4.2.1 (java 1.5)
              |Foo""".stripMargin)

    assert(("http://localhost:8553/echo" options content) ===
            """OPTIONS /echo
              |Host: localhost:8553
              |Connection: Keep-Alive
              |User-agent: Apache-HttpClient/4.2.1 (java 1.5)""".stripMargin)

    assert(("http://localhost:8553/echo" get content) ===
            """GET /echo
              |Host: localhost:8553
              |Connection: Keep-Alive
              |User-agent: Apache-HttpClient/4.2.1 (java 1.5)""".stripMargin)

    assert(("http://localhost:8553/echo" header("Foo", "Bar") get content) ===
            """GET /echo
              |Host: localhost:8553
              |Connection: Keep-Alive
              |Foo: Bar
              |User-agent: Apache-HttpClient/4.2.1 (java 1.5)""".stripMargin)

    assert(("http://localhost:8553/echo" header("Foo", "Bar") header("Baz", "Raz") get content) ===
            """GET /echo
              |Host: localhost:8553
              |Connection: Keep-Alive
              |Baz: Raz
              |Foo: Bar
              |User-agent: Apache-HttpClient/4.2.1 (java 1.5)""".stripMargin)
  }
}

