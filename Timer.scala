// Times a method execution

def time (f: => Unit): Long = {
  val start = System.currentTimeMillis
  f
  val stop = System.currentTimeMillis
  stop - start
}

