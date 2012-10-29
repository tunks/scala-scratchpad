// Multiple inheritance of type parameters

object MultiTypes {

  trait Nationality
  trait American extends Nationality
  trait Russian extends Nationality
  trait English extends Nationality

  trait ChildOf[+N <: Nationality] { def name: String }
  case class Person[+N <: Nationality](name: String) extends ChildOf[N]

  def american(x: ChildOf[American]) = { x.name }
  def russian(x: ChildOf[Russian]) = { x.name }
  def english(x: ChildOf[English]) = { x.name }

  val americanAndRussian = Person[American with Russian]("Joe")

  american(americanAndRussian)
  russian(americanAndRussian)
  // english(americanAndRussian) // (rightly) does not compile

}

