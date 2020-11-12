package inrae.semantic_web

import inrae.semantic_web.internal.{Node, pm}
import inrae.semantic_web.rdf.IRI

object SparqlQueryBuilder {

  def queryString(n: Node,
                  refToIdentifier : Map[String,String],
                  listVariables : Seq[String],
                  prefixes : Map[String,IRI]): String = {
    scribe.debug("queryString")
    scribe.debug(n.toString())
    pm.SparqlGenerator.prefixes(prefixes)+"\n" +
      pm.SparqlGenerator.prolog(listVariables) + "\n" +
      pm.SparqlGenerator.body(n, refToIdentifier) +
      pm.SparqlGenerator.solutionModifier()
  }
}
