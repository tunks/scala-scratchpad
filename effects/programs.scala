package programs

import effects._

object `package` {

  val program1: Program[Map[String,Int]] =
    for {
      nmo <- GetByKey("Saenchai")
      _   <- nmo match {
               case Right(_) => Pure(())
               case Left(NotFound(x)) =>
                 Log(s"not found: $x") andThen
                 Log("creating new record for Senchai") andThen
                 Save(("Saenchai", "southpaw"))
             }
      _   <- Save(("Yodwicha", "orthodox"))
      _   <- Save(("Petboonchu", "orthodox"))
      os  <- GetByValue("orthodox")
      oc   = os.size
      sps <- GetByValue("southpaw")
      spc  = sps.size
    } yield Map("orthodox" -> oc, "southpaw" -> spc)

}
