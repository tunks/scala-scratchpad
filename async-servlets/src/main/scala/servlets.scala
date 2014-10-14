package servlets

import javax.servlet.http._

object `package` {

  def expensiveComputation(): Int = {
    def fib(n: Int): Int =
      n match {
        case 0 => 1
        case 1 => 1
        case _ => fib(n-2) + fib(n-1)
      }
    fib(34)
  }

}

class Sync extends HttpServlet {

  override def service(req: HttpServletRequest, res: HttpServletResponse) {

    res.setContentType("text/html")
    res.setCharacterEncoding("UTF-8")

    val x = expensiveComputation()

    res.getWriter.write(x.toString)

  }

}

class Async extends HttpServlet {

  val execSvc = java.util.concurrent.Executors.newFixedThreadPool(8)

  override def destroy(): Unit = {
    execSvc.shutdown
  }

  override def service(req: HttpServletRequest, res: HttpServletResponse) {

    val ctx = req.startAsync

    execSvc submit {
      new Runnable() {                        
        override def run(): Unit = {
          res.setContentType("text/html")
          res.setCharacterEncoding("UTF-8")

          val x = expensiveComputation()

          res.getWriter.write(x.toString)

          ctx.complete
        }
      }
    }

  }

}
