package demo

import effects._

trait DB extends Effects {

   override def runEffect[A](a: Effect[A]): A =
     a match {
       case Pure(a)        => a
       case Save(n)        => save(n)
       case Enumerate      => enumerate()
       case GetByName(n)   => getByName(n)
       case GetByStance(s) => getByStance(s)
     }

  private var db: Map[String, Stance] = Map.empty

  private def save(n: NakMuay): Unit =
    db = db + (n.name -> n.stance)

  private def enumerate(): Iterable[NakMuay] =
    db map { case (n,s) => NakMuay(n, s) }

  private def getByName(n: String): Either[NotFound,NakMuay] =
    db get n map { s => Right(NakMuay(n, s)) } getOrElse Left(NotFound(n))

  private def getByStance(s: Stance): Iterable[NakMuay] =
    for {
      (name, stance) <- db
      if stance == s
    } yield NakMuay(name, stance)

}

object Main extends App with DB with Programs {

  val program: Program[Map[Stance,Int]] =
    for {
      nmo <- GetByName("Saenchai")
      _   <- nmo match {
               case Right(_) => Pure(())
               case Left(_)  => Save(NakMuay("Saenchai", Southpaw))
             }
      x   <- Save(NakMuay("Yodwicha", Orthodox))
      _   <- Save(NakMuay("Petboonchu", Orthodox))
      os  <- GetByStance(Orthodox)
      oc   = os.size
      sps <- GetByStance(Southpaw)
      spc  = sps.size
    } yield Map(Orthodox -> oc, Southpaw -> spc)

  println(runProgram(program))

}
