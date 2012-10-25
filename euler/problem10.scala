// Find the sum of all the primes below two million.

def sieve(n: Int): Set[Int] = {

  var as = (2 to n).toSet

  for (i <- (2 to n/2)) {
    if (as.contains(i)) {
      for (j <- (2 to n/i)) {
        as = as - (j*i)
      }
    }
  }

  as
}

lazy val problem10 = sieve(2000000-1).foldLeft(0L)(_+_)
