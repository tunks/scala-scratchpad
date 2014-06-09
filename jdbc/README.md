# JDBC database access in Scala

## Fixed queries

```scala
import java.sql.Connection

case class Book(title: String)

val books: Connection => List[Book] =
  { c =>
    val q = "SELECT TITLE FROM BOOK"
    val stmt = c.createStatement
    val rs = stmt.executeQuery(q)

    def _books(acc: List[Book]): List[Book] =
      if (rs.next()) {
        _books(Book(rs.getString("TITLE")) :: acc)
      } else {
        stmt.close
        acc
      }

    _books(Nil)
  }
```

## Prepared statements

```scala
import java.sql.Connection

case class Book(title: String)

def addBook(b: Book): Connection => Unit =
  { c =>
    val s = "INSERT INTO BOOK (TITLE) VALUES (?)"
    val stmt = c.prepareStatement(s)
    stmt.setString(1, b.title)
    stmt.executeUpdate
    stmt.close
  }

val init: Connection => Unit =
  { c =>
    val s = "CREATE TABLE IF NOT EXISTS BOOK(TITLE VARCHAR(256) NOT NULL)"
    val stmt = c.createStatement
    stmt.executeUpdate(s)
    stmt.close
  }
```

## Composition

```scala
implicit class Queries[A](val g: Connection => A) extends AnyVal {

  def map[B](f: A => B): Connection => B =
    { c => f(g(c)) }

  def flatMap[B](f: A => Connection => B): Connection => B =
    { c => f(g(c)).g(c) }

}
```

## Executor

```scala
def apply[A](f: Connection => A): A = synchronized {

  import java.net.URI
  import java.sql.DriverManager

  val url = "jdbc:h2:mem:testdb"
  val username = "sa"
  val password = ""

  Class.forName("org.h2.Driver")
  val c = DriverManager.getConnection(url, username, password)
  val a = f(c)
  c.close

  a
}
```

## Example

```scala
val booksQ =
  for {
    _     <- init
    _     <- addBook(Book("Surely You're Joking, Mr. Feynman!"))
    books <- books
  } yield books
  
val books = apply(booksQ)
println(books) // List(Book(Surely You're Joking, Mr. Feynman!))
```
