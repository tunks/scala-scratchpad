package filters

import javax.servlet.Filter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class Sample extends Auth {

  def unauthed(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    val body =
      <html>
        <body>
          <h1>welcome, visitor</h1>
          <form method="post"> 
            <input type="text" name="auth_email" placeholder="email address" />
            <input type="submit" value="sign in" />
          </form>
        </body>
      </html>
    res.getWriter.write(body.toString)
  }

  def keySent(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    val body =
      <html>
        <body>
          <h1>email sent!</h1>
        </body>
      </html>
    res.getWriter.write(body.toString)
  }

}

trait Auth extends Filter {

  import javax.servlet.FilterChain
  import javax.servlet.FilterConfig
  import javax.servlet.ServletRequest
  import javax.servlet.ServletResponse
  import javax.servlet.http.Cookie

  import java.util.Date

  import data._

  def unauthed(req: HttpServletRequest, res: HttpServletResponse): Unit
  def keySent(req: HttpServletRequest, res: HttpServletResponse): Unit

  def destroy() {}

  def init(fc: FilterConfig) {}

  def doFilter(sreq: ServletRequest, sres: ServletResponse, fc: FilterChain) {

    val req = sreq.asInstanceOf[HttpServletRequest]
    val res = sres.asInstanceOf[HttpServletResponse]

    findSignout(req) map { _ =>
      val c = new Cookie("session_key", null)
      res.addCookie(c)
      res.sendRedirect(req.getRequestURI)
    } orElse findEmail(req).map { e =>
       val nonce = DB.addNonce(Email(e))
       val url = req.getRequestURL + "?auth_key=" + nonce.value
       val message = "sign-in at " + url
       email(e, "xwp-auth@local", "your sign-in url", message)
       keySent(req, res)
    } orElse findNonce(req).map { e =>
      val s = DB.addSession(e)
      setSession(s)(res)
      res.sendRedirect(req.getRequestURI)
    } orElse findSession(req).map { x =>
      fc.doFilter(req, res)
    } orElse {
      unauthed(req, res)
      None
    }
  }

  private def findSignout(r: ServletRequest): Option[String] =
    Option(r.getParameter("auth_signout"))

  private def findEmail(r: ServletRequest): Option[String] =
    Option(r.getParameter("auth_email"))

  private def findNonce(r: ServletRequest): Option[Email] =
    for {
      k <- Option(r.getParameter("auth_key"))
      e <- DB.findNonce(Nonce(k))
    } yield e

  private def findSession(r: HttpServletRequest): Option[Session] =
    for {
      cs <- Option(r.getCookies)
      c  <- cs find { c => c.getName == "session_key" }
      key  = c.getValue
      s  <- DB.findSession(key)
    } yield s

  private def setSession(s: Session)(r: HttpServletResponse): Unit = {
    val maxAge: Int = (s.expiration - (new Date).getTime).toInt
    val c = new Cookie("session_key", s.key)
    c.setMaxAge(maxAge)
    r.addCookie(c)
  }

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
