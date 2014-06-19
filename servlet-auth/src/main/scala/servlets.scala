package servlets

import javax.servlet.http.HttpServlet

class Sample extends HttpServlet {

  import javax.servlet.http.HttpServletRequest
  import javax.servlet.http.HttpServletResponse

  import data._

  def findEmail(r: HttpServletRequest): Email =
    (for {
      cs <- Option(r.getCookies)
      c  <- cs find { c => c.getName == "session_key" }
      s  <- DB.findSession(c.getValue)
      e  <- DB.findEmail(s)
    } yield e).get

  override def service(req: HttpServletRequest, res: HttpServletResponse) {

    res.setContentType("text/html")
    res.setCharacterEncoding("UTF-8")

    val email = findEmail(req).value

    val body =
      <html>
        <body>
          <h1>welcome, { email }</h1>
          <p><a href="?auth_signout">sign out</a></p>
        </body>
      </html>

    res.getWriter.write(body.toString)

  }

}
