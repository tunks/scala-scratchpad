# Actor-Based Continuations with Akka and Swarm

_17 Apr 2011_

Swarm is a Scala continuations framework which allows computation to be moved from one machine to another. This example shows how it can be extended to use Akka actors to transport computation between actors.

![Transporter UML](https://github.com/JamesEarlDouglas/scala-scratchpad/blob/master/akka-swarm/readme/akka-swarm.png)

To use Swarm, `Transporter.transport` must be implemented to deliver continuations between nodes. For Akka, this means sending each continuation as a message.

```scala
case class ActorLocation(val name: String, val host: String, val port: Short) extends Location

object AkkaTransporter extends Transporter {
  override def transport(f: (Unit => Bee), destination: Location) {
    destination match {
      case ActorLocation(name, host, port) => remote.actorFor(name, host, port) ! f
    }
  }
}
```

In the Swarm API, continuations are passed as functions of `Unit => Bee` to destinations represented by implementations of `Location`. Here, a destination is identified by the name, host, and port of a remote actor.

A sample actor is used to indicate when it has control of the continuation.

```scala
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
```

The actor has an implicit `Transporter`, which is used by Swarm behind the scenes.

To demonstrate sending continuations between actors, two `main` classes are used: one to set up the remote actors, and one to kick off the continuation.

```scala
object RemoteActorStarter {
  def main(args: Array[String]) {
    remote.start("localhost", 2552)
    remote.register("continuationActor1", actorOf[ContinuationActor])
    remote.register("continuationActor2", actorOf[ContinuationActor])
  }
}

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
```

`RemoteActorStarter` registers two `ContinuationActor` instances named `continuationActor1` and `continuationActor2`. `AkkaTest` then uses Swarm to spawn the continuation defined as the method `f`. This will print a message, transfer the code to the first actor using the implicit `AkkaTransporter`, print another message, transfer the code to the second actor, and print a third message.

The flow of execution of this code looks something like the following.

![Flow of execution](https://github.com/JamesEarlDouglas/scala-scratchpad/blob/master/akka-swarm/readme/akka-swarm-flow.png)

The output of the above `main` methods shows the code moving from actor to actor.

_AkkaTest:_

```bash
[info] Running swarm.akka.AkkaTest 
Here I am!
```

_RemoteActorStarter:_

```bash
[info] Running swarm.akka.RemoteActorStarter 

Entering actor uuid 079c1a40-6853-11e0-a43c-001e6522d06c
I'm here now!
Extiting actor uuid 079c1a40-6853-11e0-a43c-001e6522d06c


Entering actor uuid 07a25bd0-6853-11e0-a43c-001e6522d06c
Over here!
Extiting actor uuid 07a25bd0-6853-11e0-a43c-001e6522d06c
```

## Installation

This project depends on Swarm being installed in your local Maven repository.

1) Download Swarm:

```bash
> git clone git@github.com:sanity/Swarm.git
```

2) Generate a Maven POM:

```bash
> cd Swarm
> sbt update make-pom
```

3) Install in your local Maven repository:

```bash
> mkdir -p ~/.m2/repository/swarm-dpl/swarm_2.8.1/1.0-SNAPSHOT
> cp target/scala_2.8.1/swarm_2.8.1-1.0-SNAPSHOT.* ~/.m2/repository/swarm-dpl/swarm_2.8.1/1.0-SNAPSHOT/
```

