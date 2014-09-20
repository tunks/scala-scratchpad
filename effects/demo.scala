package demo

import effects._

trait DB extends EffectRunner {

   override def runEffect[A](a: Effect[A]): A =
     a match {
       case Pure(a)         => a
       case Save(n)         => save(n)
       case Enumerate       => enumerate()
       case FindByName(n)   => findByName(n)
       case FindByStance(s) => findByStance(s)
     }

  private var db: Map[String, Stance] = Map.empty

  private def save(n: NakMuay): Unit =
    db = db + (n.name -> n.stance)

  private def enumerate(): Iterable[NakMuay] =
    db map { case (n,s) => NakMuay(n, s) }

  private def findByName(n: String): Option[NakMuay] =
    db get n map { s => NakMuay(n, s) }

  private def findByStance(s: Stance): Iterable[NakMuay] =
    for {
      (name, stance) <- db
      if stance == s
    } yield NakMuay(name, stance)

}

object Main extends App with DB with ProgramRunner {

  val program: Program[Map[Stance,Int]] =
    for {
      nmo <- FindByName("Saenchai")
      _   <- nmo match {
               case Some(nm) => Pure(())
               case None     => Save(NakMuay("Saenchai", Southpaw))
             }
      x   <- Save(NakMuay("Yodwicha", Orthodox))
      _   <- Save(NakMuay("Petboonchu", Orthodox))
      os  <- FindByStance(Orthodox)
      oc   = os.size
      sps <- FindByStance(Southpaw)
      spc  = sps.size
    } yield Map(Orthodox -> oc, Southpaw -> spc)

  println(runProgram(program))

}
