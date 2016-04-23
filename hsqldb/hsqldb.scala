object Main extends App {

  Class.forName("org.hsqldb.jdbcDriver")

  val c = java.sql.DriverManager.getConnection("jdbc:hsqldb:mem:mydb", "sa", "")

  val s1 =
    c.prepareStatement(
      """|CREATE TABLE IF NOT EXISTS FOLKS (
         |  ID CHAR(36) NOT NULL,
         |  NAME VARCHAR(1024),
         |  PRIMARY KEY (ID)
         |)""".stripMargin
    )
  s1.execute
  s1.close

  val s2 =
    c.prepareStatement(
      """|INSERT INTO FOLKS (ID, NAME)
         |VALUES (?, ?)""".stripMargin
    )
  s2.setString(1, "e9fcb4bd-c821-47c9-bc56-f997c361c1e2")
  s2.setString(2, "Folky McFolkface")
  s2.execute
  s2.close

  val s3 = c.prepareStatement("SELECT ID, NAME FROM FOLKS WHERE ID = ?")
  s3.setString(1, "e9fcb4bd-c821-47c9-bc56-f997c361c1e2")
  val rs3 = s3.executeQuery
  while (rs3.next()) {
    val id   = rs3.getString("ID")
    val name = rs3.getString("NAME")
    println(s"${id}: ${name}")
  }
  s3.close

}
