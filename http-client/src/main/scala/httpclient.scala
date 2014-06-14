package httpclient

case class Res(
  status: Int,
  headers: Map[String, String],
  body: java.io.InputStream
)
 
case class Req(
  method: String = "GET",
  url: String,
  headers: Map[String, String] = Map.empty,
  body: Option[() => java.io.InputStream] = None
) {

  import java.io._

  def apply[A](k: Res => A): A = {

    import java.net._
    import scala.collection.JavaConversions._

    val conn = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
    conn.setInstanceFollowRedirects(false)
    conn.setRequestMethod(method)

    headers foreach { case (k,v) => conn.setRequestProperty(k,v.mkString(",")) }

    body match {
      case None =>
        conn.setDoOutput(false)
      case Some(is) =>
        conn.setDoOutput(true)
        val os = new DataOutputStream(conn.getOutputStream)
        write(is(), os)
        os.flush
        os.close
    }
 
    val res =
      Res(
        status = conn.getResponseCode,
        headers = conn.getHeaderFields.toMap mapValues { _.mkString(",") },
        body = conn.getInputStream
      )

    val a = k(res)
    res.body.close
    a
  }

  private def write(is: InputStream, os: OutputStream): Unit = {
    val buffer = Array.fill[Byte](1024)(0)
    var bytesRead: Int = is.read(buffer)
    while (bytesRead != -1) {
      os.write(buffer, 0, bytesRead)
      bytesRead = is.read(buffer)
    }
    is.close
  }

}
