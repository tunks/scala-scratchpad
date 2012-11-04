package swarm.akka

import akka.actor.Actor._

object RemoteActorStarter {
  def main(args: Array[String]) {
    remote.start("localhost", 2552)
    remote.register("continuationActor1", actorOf[ContinuationActor])
    remote.register("continuationActor2", actorOf[ContinuationActor])
  }
}