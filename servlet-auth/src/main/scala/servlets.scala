package servlets

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

case class Nonce(value: String)
case class SessionId(value: String)
case class Email(value: String)

object DB {

  private var nonces: Map[Nonce,Email] = Map.empty
  private var sessions: Map[SessionId,Email] = Map.empty

  def addNonce(n: Nonce, e: Email): Unit =
    nonces = nonces + (n -> e)

  def findNonce(n: Nonce): Boolean =
    nonces contains n

  def getNonce(n: Nonce): Email = {
    val e = nonces(n)
    nonces = nonces - n
    e
  }

  def addSession(s: SessionId, e: Email): Unit =
    sessions = sessions + (s -> e)

  def findSession(s: SessionId): Boolean =
    sessions contains s
}

trait AuthedService {

  def apply(s: SessionId, req: HttpServletRequest, res: HttpServletResponse): Unit

  def svc(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    findNonce(req) match {
      case Some(x) if DB.findNonce(x) =>
        val e = DB.getNonce(x)
        val s = createSession
        DB.addSession(s,e)
        setSession(s)(res)
        res.sendRedirect(req.getRequestURI)
      case _ =>
        findSession(req) match {
          case Some(x) if DB.findSession(x) => apply(x, req, res)
          case _ => res.sendRedirect("/auth")
        }
    }
  }

  private def findNonce(r: HttpServletRequest): Option[Nonce] =
    Option(r.getParameter("key")) map { Nonce.apply }

  private def findSession(r: HttpServletRequest): Option[SessionId] =
    for {
      cs <- Option(r.getCookies)
      c  <- cs find { c => c.getName == "session_id" }
      s   = SessionId(c.getValue)
    } yield s

  private def setSession(s: SessionId)(r: HttpServletResponse): Unit = {
    import javax.servlet.http.Cookie
    val c = new Cookie("session_id", s.value)
    c.setMaxAge(60 * 60 * 24 * 14)
    r.addCookie(c)
  }

  private def createSession: SessionId =
    SessionId(java.util.UUID.randomUUID.toString)

}


class Root extends HttpServlet with AuthedService {

  override def doGet(req: HttpServletRequest, res: HttpServletResponse) {
    svc(req, res)
  }

  def apply(s: SessionId, req: HttpServletRequest, res: HttpServletResponse) {
 
    res.setContentType("text/html")
    res.setCharacterEncoding("UTF-8")

    val body =
      <html>
        <body>
          <h1>authd!</h1>
          <p>session: { s.value }</p>
        </body>
      </html>

    res.getWriter.write(body.toString)
  }

}

class Authenticate extends HttpServlet {

  override def doGet(req: HttpServletRequest, res: HttpServletResponse) {
 
    res.setContentType("text/html")
    res.setCharacterEncoding("UTF-8")

    val body =
      Option(req.getParameter("await")) match {
        case Some(_) => 
          <html>
            <body>
              <h1>email sent</h1>
            </body>
          </html>
        case None =>
          <html>
            <body>
              <h1>sign in</h1>
              <form method="post" action="/auth">
                <input type="text" name="email" placeholder="email address" />
                <input type="submit" value="sign in" />
              </form>
            </body>
          </html>
      }

    res.getWriter.write(body.toString)

  }

  override def doPost(req: HttpServletRequest, res: HttpServletResponse) =
    Option(req.getParameter("email")) match {
      case Some(e) =>
        val nonce = createNonce
        DB.addNonce(nonce, Email(e))
        val message = "sign-in at http://localhost:8080/?key=" + nonce.value
        email(e, "xwp-auth@local", "your sign-in url", message)
        res.sendRedirect("/auth?await")
      case None =>
        res.sendRedirect("/auth")
    }

  private def createNonce: Nonce =
    Nonce(java.util.UUID.randomUUID.toString)

  private def email(to: String, from: String, s: String, m: String): Unit = {

    import javax.mail.Message
    import javax.mail.internet.MimeMessage
    import javax.mail.internet.InternetAddress
    import javax.mail.Session
    import java.util.Properties
    import javax.mail.Transport

    val properties = new Properties
    properties.put("mail.transport.protocol", "smtp")
    properties.put("mail.smtp.host", "localhost")
    properties.put("mail.smtp.port", "25")
    val session = Session.getDefaultInstance(properties)

    val mmessage = new MimeMessage(session)
    mmessage.setFrom(new InternetAddress(from))
    mmessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to))
    mmessage.setSubject(s)
    mmessage.setText(m)
    Transport.send(mmessage)

  }

}
