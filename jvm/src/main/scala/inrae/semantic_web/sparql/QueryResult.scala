package inrae.semantic_web.sparql
import inrae.semantic_web.rdf._
import org.apache.jena.query._

import scala.collection.JavaConverters._

case class QueryResult(results : ResultSet, mimetype : String="") {
  def print(): Unit = {
    println("================================= QueryResult ===========================================")
    println(ResultSetFormatter.asText(results))
    println("=========================================================================================")
  }

  //https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/rdf/model/RDFNode.html?is-external=true
  
  def get(): ResultsFormat = {

    val rf = ResultsFormat()
    val vars = results.getResultVars.asScala

    while (results.hasNext) {
      val r = results.next()
//: Map[String,RdfType]
      val values = vars.map(
        varName => {
          val obj = r.get(varName)
          val t = obj.isLiteral() match {
            case true => {
              val lit = obj.asLiteral()
              Literal(lit.getString,lit.getDatatypeURI,lit.getLanguage)
            }
            case false => {
              val res = obj.asResource()
              URI(res.getLocalName,res.getNameSpace)
            }
          }
          (varName -> t)
        }
      ).toMap

      rf.rows = rf.rows :+ rf.ResultsRow(values)
    }

    return rf
  }
}
