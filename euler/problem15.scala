// The straightforward - and very inefficient - way
def problem15(coord: (Long, Long)): Long = coord match {
    case (0, _) => 1
    case (_, 0) => 1
    case (x, y) => problem15(x - 1, y) + problem15(x, y - 1)
  }

// Optimized with a cache
def problem15c(coord: (Long, Long)) = {

  val cache = new collection.mutable.HashMap[(Long, Long), Long]()

  def apply(coord: (Long, Long)): Long = cache.getOrElse(coord, {
    val paths = coord match {
      case (0, _) => 1
      case (_, 0) => 1
      case (x, y) => apply(x - 1, y) + apply(x, y - 1)
    }
    cache += (coord -> paths)
    cache(coord)
  })

  apply(coord)
}

problem15c(20, 20) //137846528820

// Memoized with a Y combinator
def Y[A,B](f: (A => B) => (A => B)): (A => B) = {

  val cache = collection.mutable.HashMap[A,B]()

  def fix(f: (A => B) => (A => B)): (A => B) = { a: A =>
    if (!cache.contains(a)) {
      cache(a) = f(fix(f))(a)
    }
    cache(a)
  }

  fix(f)
}

def problem15m(w: Int, h: Int) = {

  val yf = Y { f: (Int => Long) => n: Int =>
    if (n < h) 1
    else if (n % h == 0) 1
    else f(n-1) + f(n-h)
  }

  ((0 to w*h).map(yf)).sum
}

problem15m(20, 20) //137846528820

