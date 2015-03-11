package example

import org.w3.banana._
import org.w3.banana.jena.JenaModule
import org.w3.banana.sesame.SesameModule

trait SPARQLExampleDependencies
  extends RDFModule
  with RDFOpsModule
  with SparqlOpsModule
  with SparqlHttpModule

trait DBpediaSPARQLExample extends SPARQLExampleDependencies { self =>

  import ops._
  import sparqlOps._
  import sparqlHttp.sparqlEngineSyntax._
  import java.net.URL

  def main(args: Array[String]): Unit = {

    /* gets a SparqlEngine out of a Sparql endpoint */

    val endpoint = new URL("http://dbpedia.org/sparql/")

    /* creates a Sparql Select query */

    val query = parseSelect("""
PREFIX ont: <http://dbpedia.org/ontology/>
SELECT DISTINCT ?language WHERE {
 ?language a ont:ProgrammingLanguage .
 ?language ont:influencedBy ?other .
 ?other ont:influencedBy ?language .
} LIMIT 100
""").get

    /* executes the query */

    val answers: Rdf#Solutions = endpoint.executeSelect(query).getOrFail()

    /* iterate through the solutions */

    val languages: Iterator[Rdf#URI] = answers.iterator map { row =>
      /* row is an Rdf#Solution, we can get an Rdf#Node from the variable name */
      /* both the #Rdf#Node projection and the transformation to Rdf#URI can fail in the Try type, hense the flatMap */
      row("language").get.as[Rdf#URI].get
    }

    println(languages.to[List])
  }

}

trait WikidataSPARQLExample extends SPARQLExampleDependencies { self =>

  import ops._
  import sparqlOps._
  import sparqlHttp.sparqlEngineSyntax._
  import java.net.URL

  def run(queryS: String): Rdf#Solutions = {
    val endpoint = new URL("http://localhost:9999/bigdata/sparql")
    val query = parseSelect(queryS).get
    endpoint.executeSelect(query).getOrFail()
  }

  def main(args: Array[String]): Unit = {

    val queryS = """
        |prefix wdq: <http://www.wikidata.org/entity/>
        |prefix wdo: <http://www.wikidata.org/ontology#>
        |prefix xs: <http://www.w3.org/2001/XMLSchema#>
        |select ?entity ?date ?bar WHERE {
        |  ?entity wdq:P569s ?dateS .
        |  ?dateS wdq:P569v ?dateV .
        |  ?dateV wdo:preferredCalendar wdq:Q1985727 .
        |  ?dateV wdo:time ?date .
        |  ?entity wdq:P735s ?nameS .
        |  ?nameS wdq:P735v ?nameV .
        |  ?nameV ?foo ?bar
        |  FILTER (?date > "1918-04-11"^^xs:date && ?date < "1918-06-11"^^xs:date)
        |} LIMIT 10
      """.stripMargin

    val answers: Rdf#Solutions = run(queryS);
    answers.iterator foreach { row =>
//      row.
      val uri: Rdf#URI = row("entity").get.as[Rdf#URI].get
      val bar= row("bar").get
      println("<" + uri + ">  " + bar)
    }
  }

}

object SPARQLExampleWithJena extends WikidataSPARQLExample with JenaModule
