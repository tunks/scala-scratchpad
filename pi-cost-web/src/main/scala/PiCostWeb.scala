package com.earldouglas.picostweb

import akka.actor._
import akka.routing.Routing._
import akka.routing.CyclicIterator
import org.scalatra._
 
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
        <div>Work size: {coordinator.work} <a href="harder">increase</a> <a href="softer">decrease</a></div>
        <div>Work delay: {coordinator.delay} ms <a href="faster">faster</a> <a href="slower">slower</a></div>
        <div>Workers: {coordinator.workerCount} <a href="addworker">add</a> <a href="removeworker">remove</a></div>
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

case object GetEstimate
case object GetStatus
case class Statuses(val values: Iterable[Status])
case class Status(val uuid: Uuid, val mailboxSize: Int)
case class Result(val value: Double, val status: Status)

class Accumulator extends Actor {

  var pi: Double = _
  val statuses = new collection.mutable.HashMap[Uuid, Status]

  def receive = {
    case GetEstimate => self reply pi
    case GetStatus => self.reply(new Statuses(statuses.values))
    case result: Result =>
      pi += result.value
      statuses(result.status.uuid) = result.status
  }
}

class Worker extends Actor {
  def receive = {
    case range: Range =>
      val value = (for (k <- range) yield (4 * math.pow(-1, k) / (2 * k + 1))).sum
      val result = new Result(value, new Status(self.uuid, self.mailboxSize))
      self reply result
  }
}

