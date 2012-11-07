// Compares the relative performance of String.replace and String.replaceAll

def compareReplacements(n: Int) { 
  val s1 = new String("'hello world'");
  val start1 = System.currentTimeMillis
  for (i <- 1 to n) s1.replace("'", "\\'")
  println("rep: " + (System.currentTimeMillis - start1))

  val s2 = new String("'hello world'");
  val start2 = System.currentTimeMillis
  for (i <- 1 to n) s2.replaceAll("'", "\\'")
  println("repAll: " + (System.currentTimeMillis - start2))
}

