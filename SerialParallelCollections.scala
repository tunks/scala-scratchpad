def time[A](f: => A): (A, Long) = {
  val start = System.currentTimeMillis
  val a = f
  val end = System.currentTimeMillis
  (a, end - start)
}

def slowDouble(x: Int) = {
  Thread.sleep(10)
  x + x
}

val list = (1 to 1000).toList

def serial = list.map(slowDouble)
time(serial)._2 // runs in about 10.8 sec

def parallel = list.par.map(slowDouble)
time(parallel)._2 // runs in about 2.7 sec
