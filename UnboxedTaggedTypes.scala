// The setup

type Tagged[U] = { type Tag = U }
type @@[T, U] = T with Tagged[U]

class Tagger[U] {  def apply[T](t : T) : T @@ U = t.asInstanceOf[T @@ U] }
def tag[U] = new Tagger[U]

trait Named
trait Person extends Named

type Name = String @@ Named
def name(name: String): Name = tag(name)

trait Registry {
  def getPerson(name: Name): Option[Person]
}

// Now to test it

case class Dude() extends Person

object InMemRegistry extends Registry {
  private val people: Map[Name, Person] = Seq((name("Joe") -> new Dude())).toMap
  def getPerson(name: Name) = people.get(name)
}

val bob = name("Bob")
val joe = name("Joe")

InMemRegistry.getPerson(bob) // returns None
InMemRegistry.getPerson(joe) // returns Some(Dude())

