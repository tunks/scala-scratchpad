# Web-Based Akka Management and Monitoring

_25 Mar 2011_

Whipping up a quick Servlet-based Web application is easy with [Scalatra](https://github.com/scalatra/scalatra). In this example, I show how it can be used to create a basic management and monitoring tool for Akka actors.

This example builds on [Estimating Pi with Akka](https://github.com/JamesEarlDouglas/pi-cost), adding a `ScalatraServlet` and some support infrastructure for coordinating the whole mess.

Upon starting, the user is presented with a page showing the current estimate of Pi, the size of each work unit, the delay between sending each work unit to the actors, the number of working actors, and the status of all actors.

![Pi Cost screenshot](https://raw.github.com/JamesEarlDouglas/pi-cost-web/master/readme/pi-cost-web-start.png)

Once the first actors are added, the work begins. The estimate of Pi updates every quarter second (via Ajax), as does the status of the actors. Larger work units combined with shorter delays will cause work units to queue up in the actor mailboxes, and the load can be managed by adding more actors. In a production application the actors would be scaled out as remote actors, but for this example they are run locally.

![Pi Cost screenshot](https://raw.github.com/JamesEarlDouglas/pi-cost-web/master/readme/pi-cost-web-running.png)

The most significant code addition is the Coordinator class, which keeps track of all the workers and sends work units to them based on a set size and delay. I suspect there is a more elegant and Scalaly way to build a scheduled executor than this, which is a `Runnable` with a `while(true)`.

```scala
class Coordinator(accumulator: ActorRef) extends Runnable {

  var activeWorkers: List[ActorRef] = Nil
  var inactiveWorkers: List[ActorRef] = Nil
  var workers: ActorRef = _

  implicit val accumulatorOption = Option(accumulator)

  var x = 0
  var sleepTime = 1024
  var workSize = 1024

  def addWorker() = {
    inactiveWorkers match {
      case Nil => activeWorkers = Actor.actorOf[Worker].start :: activeWorkers
      case head :: tail =>
        activeWorkers = inactiveWorkers.head :: activeWorkers
        inactiveWorkers = inactiveWorkers.tail
    }
    workers = loadBalancerActor(new CyclicIterator(activeWorkers))
  }

  def removeWorker() = {
    inactiveWorkers = activeWorkers.head :: inactiveWorkers
    activeWorkers = activeWorkers.tail
    workers = loadBalancerActor(new CyclicIterator(activeWorkers))
  }

  def workerCount = activeWorkers.size

  def delay = sleepTime
  def goFaster = sleepTime /= 2
  def goSlower = sleepTime *= 2

  def work = workSize
  def workHarder = workSize *= 2
  def hardlyWork = workSize /= 2

  def run() = {
    while(true) {
      Thread.sleep(sleepTime)
      val length = workSize
      if (activeWorkers.size >= 1) {
        workers ! (x until x + length)
        x += length
      }
    }
  }
}
```

The rest of the addition is the `ScalatraServlet`, which builds all the pretty HTML for the user agent to consume.

```scala
class PiCostWeb extends ScalatraServlet with UrlSupport {

  val accumulator = Actor.actorOf[Accumulator].start
  var coordinator = new Coordinator(accumulator)
  new Thread(coordinator).start

  before {
    contentType = "text/html"
  }

  get("/") {
    <html>
      <head>
        <title>Pi Cost</title>
      </head>
      <body>
        <h1>Pi: <span id="pi"></span></h1>
        <div>Work size: {coordinator.work} <a href="harder/">increase</a> <a href="softer/">decrease</a></div>
        <div>Work delay: {coordinator.delay} ms <a href="faster/">faster</a> <a href="slower/">slower</a></div>
        <div>Workers: {coordinator.workerCount} <a href="addworker/">add</a> <a href="removeworker/">remove</a></div>
        <div>Actors: <span id="status"></span></div>
        <script type="text/javascript">
        <!--
        function ajaxThingy(url, id) {
          var xmlHttpReq = false;

          if (window.XMLHttpRequest) {
            xmlHttpReq = new XMLHttpRequest();
          } else if (window.ActiveXObject) {
            xmlHttpReq = new ActiveXObject("Microsoft.XMLHTTP");
          }
          xmlHttpReq.open('GET', url, true);
          xmlHttpReq.onreadystatechange = function() {
            if (xmlHttpReq.readyState == 4) {
              document.getElementById(id).innerHTML = xmlHttpReq.responseText;
            }
          }
          xmlHttpReq.send();
        }

        ajaxThingy("/pi", "pi");
        setInterval('ajaxThingy("/pi", "pi")', 250)
        setInterval('ajaxThingy("/status", "status")', 250)
        document.forms['compute'].onsubmit = new Function('computePi(); return false')
        -->
      </script>
      </body>
    </html>
  }

  get("/pi") {
    (accumulator !! GetEstimate).getOrElse(4)
  }

  get("/status") {
    (accumulator !! GetStatus).getOrElse(Nil) match {
      case ss: Statuses =>
        xml.NodeSeq.fromSeq(List((ss.values.map(status(_)))).flatten)
    }
  }

  get("/addworker") {
    coordinator.addWorker()
    redirect("/")
  }

  get("/removeworker") {
    coordinator.removeWorker()
    redirect("/")
  }

  get("/faster") {
    coordinator.goFaster
    redirect("/")
  }

  get("/slower") {
    coordinator.goSlower
    redirect("/")
  }

  get("/harder") {
    coordinator.workHarder
    redirect("/")
  }

  get("/softer") {
    coordinator.hardlyWork
    redirect("/")
  }

  private def status(status: Status) = {
    <div><span>{status.uuid}: </span><span>{status.mailboxSize} in queue</span></div>
  }
 
  protected def contextPath = request.getContextPath
}
```

