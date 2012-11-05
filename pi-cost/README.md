# Estimating Pi with Akka

_4 Mar 2011_


This example demonstrates the simplicity in using Akka to scale up a basic distributable task: estimating the value of Pi. It was created with Scala 2.8.1, Akka 1.0, and SBT 0.7.4. To try it out, run the following:

```bash
> sbt update run
```

To change the number of workers, add or remove `Worker` actors from the workers list in the `run` method.

Pi is estimated by summing elements in an infinite series derived from the following equation:

![Pi equation](https://raw.github.com/JamesEarlDouglas/pi-cost/master/readme/pi-series.png)

The evaluation of each element in the the series can be carried out independently, then combined with one another at the end, making this a perfect problem for distributed computing.

This algorithm splits the series into finite lists of elements, each of which is evaluated by a worker then given to an accumulator to keep track of the total sum.

A worker is implemented as an Akka `Actor`.

```scala
class Worker extends Actor {
  def receive = {
    case range: Range => self.reply((for (k <- range) yield (4 * math.pow(-1, k) / (2 * k + 1))).sum)
  }
}
```

A `Worker` iterates over a `Range` of k, evaluating each kth element in the series. The sum of the results is sent as a message to the accumulator.

The accumulator is implemented as an Akka `Actor`.

```scala
class Accumulator(iterations: Int) extends Actor {

  var count: Int = _
  var pi: Double = _
  var start: Long = _

  def receive = {
    case result: Double =>
      pi += result;
      count += 1;
      if (count == iterations) Actor.registry.shutdownAll
  }

  override def preStart = {
    start = System.currentTimeMillis
  }

  override def postStop = {
    println("\n>>> result: " + pi)
    println(">>> run time: " + (System.currentTimeMillis - start) + " ms\n")
  }
}
```

The `Accumulator` listens for results from `Worker`s, keeping a running sum of each. It measures its run time with `preStart` and `postStop`, and stops all actors once it has received the expected number of worker replies.

A simple runner is used to manage the actors and distribute the work to the workers.

```scala
object Runner {

  def main(args: Array[String]) = run(10000, 10000)

  def run(iterations: Int, length: Int) = {
    implicit val accumulator = Option(Actor.actorOf(new Accumulator(iterations)).start)

    val workers = loadBalancerActor(new CyclicIterator(List(
      Actor.actorOf[Worker].start,
      Actor.actorOf[Worker].start
    )))
    for (x <- 0 until iterations) workers ! ((x * length) to ((x + 1) * length - 1))
  }
}
```

The `Runner` has a `main` method which starts an `Accumulator` and a list of `Worker`s, then sends 10,000 ranges to the workers in a round robin fashion using a `CyclicIterator`.

My laptop has two CPU cores, both of which are running in powersave mode as I write this on an airplane. Following are the results of computing 10,000 lists of 10,000 elements with one, two, four, and eight workers. Pi was estimated each time to be 3.1415926435897883.

| # Actors | Run Time (ms) |
|:---------|---------------|
| 1        | 35,268        |
| 2        | 21,063        |
| 4        | 20,919        |
| 8        | 20,730        |

This code has a few limitations which would need to be addressed if this were to form the basis for a serious attempt at distributed computing. The workers could be remote actors instead of local actors, allowing the work to be scaled out. Waiting for the workers to finish by counting the results recieved by the accumulator is not a good idea, since it is vulnerable to crashed or delayed workers.
