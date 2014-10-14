# Asynchronous Scala Servlets

*October 14, 2014*

The Servlet 3.0 specification added asynchronous processing support for Servlet implementations.

Among the advantages of asynchronous processing support is the ability to offload work from the usual request-handling thread pool, freeing up resources to handle more incoming connections.

Consider the following synchronous Servlet implementation:

```scala
class Sync extends HttpServlet {

  override def service(req: HttpServletRequest, res: HttpServletResponse) {

    res.setContentType("text/html")
    res.setCharacterEncoding("UTF-8")

    val x = expensiveComputation()

    res.getWriter.write(x.toString)

  }

}
```

An unavoidable expensive computation will not only block the response from being written, but will consume the thread that handles this request/response cycle.  If enough of these happen concurrently, the container's request thread pool will be starved, and additional requests will fail.

We can see this failure when we blast the server with many simultaneous requests:

```
$ ab -r -n 4000 -c 4000 localhost:8080/sync

Concurrency Level:      4000
Time taken for tests:   219.405 seconds
Complete requests:      4000
Failed requests:        1615
   (Connect: 0, Receive: 560, Length: 495, Exceptions: 560)
Requests per second:    18.23 [#/sec] (mean)
```
Of our 4,000 requests, 1,615 failed.

Although we don't have a way around the amount of CPU time that must be spent servicing each request, we can pass off the work to a service that supports queuing and execution in its own thread pool.

Consider the following asynchronous Servlet implementation:

```scala
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
```

Very little work is done by the `service()` method -- it creates a continuation that captures both the expensive computation and writing to the response, and passes that off to an executor to be queued and run when it becomes available.

Running the same load test as before, we see dramatic improvement:

```
$ ab -r -n 4000 -c 4000 localhost:8080/async

Concurrency Level:      4000
Time taken for tests:   238.737 seconds
Complete requests:      4000
Failed requests:        61
   (Connect: 0, Receive: 4, Length: 53, Exceptions: 4)
Transfer rate:          2.64 [Kbytes/sec] received
```

Our failure count dropped from 1,615 to 61.  Clients will still need to wait for the server to get around to processing their request, but they are much more likely to have a successful (if slow) response.
