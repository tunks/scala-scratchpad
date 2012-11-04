package swarm.akka

import swarm.Swarm.swarm
import swarm.{Transporter, Swarm, Bee, NoBee}

object AkkaTest {

  def main(args: Array[String]) {
    implicit val tx: Transporter = AkkaTransporter
    Swarm.spawn(f)
  }

  def f(u: Unit): Bee@swarm = {
    println("Here I am!")
    Swarm.moveTo(ActorLocation("continuationActor1", "localhost", 2552))
    println("I'm here now!")
    Swarm.moveTo(ActorLocation("continuationActor2", "localhost", 2552))
    println("Over here!")
    NoBee()
  }
}
