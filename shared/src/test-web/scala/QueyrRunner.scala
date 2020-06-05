package EasySparql

import org.apache.jena.query.ARQ
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.VCARD

object Examples {
  def main(args: Array[String]): Unit = {
    //    val personURI = "http://somewhere/JohnSmith"
    //    val fullName = "John Smith"
    //
    //    val model = ModelFactory.createDefaultModel()
    //    val johnSmith = model
    //      .createResource(personURI)
    //      .addProperty(VCARD.FN, fullName)
    //
    //    println(johnSmith.toString)

    val query = "SELECT ?prop ?place WHERE { <http://dbpedia.org/resource/%C3%84lvdalen> ?prop ?place .}"

    val dbpediaRunner = QueryRunner("http://dbpedia.org/sparql")
    val result = dbpediaRunner
      .query(query)

    printResult("JSON", result.asJson())
    printResult("CSV", result.asCSV())
    printResult("XML", result.asXML())

  }

  def printResult(name: String, result: Option[String]): Unit = {
    val res = result.getOrElse("")
    println(name)
    println(res)
    println()
  }

}