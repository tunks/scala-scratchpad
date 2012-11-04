package swarm.akka

import akka.actor.Actor._
import akka.actor.Actor
import swarm.{Swarm, Bee, Transporter, Location}

case class ActorLocation(val name: String, val host: String, val port: Short) extends Location

object AkkaTransporter extends Transporter {
  override def transport(f: (Unit => Bee), destination: Location) {
    destination match {
      case ActorLocation(name, host, port) => remote.actorFor(name, host, port) ! f
    }
  }
}

class ContinuationActor extends Actor {

  implicit val tx: Transporter = AkkaTransporter

  def receive = {
    case f: (Unit => Bee) =>
      println()
      println("Entering actor uuid " + self.uuid)
      Swarm.continue(f)
      println("Extiting actor uuid " + self.uuid)
      println()
  }
}