package com.earldouglas.httpunit

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

object TestServer extends App {
  val server = new Server(8080)
  val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
  context.setContextPath("/")
  server.setHandler(context)
  context.addServlet(new ServletHolder(new OkServer()), "/ok")
  server.start()
}

class OkServer extends HttpServlet {
  override def service(req: HttpServletRequest, res: HttpServletResponse) {
   res.setStatus(200)
  }
}
