package inrae.semantic_web.sparql
import inrae.semantic_web.rdf._
import org.apache.jena.query._

import scala.jdk.CollectionConverters._

case class QueryResult(results : ResultSet, mimetype : String="") {

  //https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/rdf/model/RDFNode.html?is-external=true
  
  def get(): ResultsFormat = {

    val rf = ResultsFormat()
    val vars = results.getResultVars.asScala
    scribe.debug(" -- vars -- ")
    scribe.debug(vars.toString())
    while (results.hasNext) {
      val r = results.next()
      val values = vars.map(
        varName => try {
          val obj = r.get(varName)
          val t = obj.isLiteral() match {
            case true => {
              val lit = obj.asLiteral()
              Literal(lit.getString,lit.getDatatypeURI,Some(lit.getLanguage))
            }
            case false => {
              val res = obj.asResource()
              URI(res.getLocalName,res.getNameSpace)
            }
          }
          (varName -> t)
        } catch {
          case e : Exception => {
            // todo : avoid this treatment
            (varName -> URI("-- exception --"))
          }
        }
      ).toMap

      rf.rows = rf.rows :+ rf.ResultsRow(values)
    }
    scribe.debug("Size results format :"+rf.rows.length)
    scribe.debug(rf.toString())
    return rf
  }
}
