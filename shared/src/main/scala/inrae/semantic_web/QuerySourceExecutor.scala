package inrae.semantic_web

import inrae.semantic_web.internal.{Node, pm}
import inrae.semantic_web.rdf.IRI
import inrae.semantic_web.sparql.{QueryResult, _}

import scala.concurrent.Future

object QuerySourceExecutor {

  def queryOnSource(n: Node,
                    listVariables : Seq[String],
                    source : ConfigurationObject.Source,
                    prefixes : Map[String,IRI]): Future[QueryResult] = {
    val (refToIdentifier,_) = pm.SparqlGenerator.correspondanceVariablesIdentifier(n)
    val query = pm.SparqlGenerator.prefixes(prefixes)+"\n" +
      pm.SparqlGenerator.prolog(listVariables) + "\n" +
      pm.SparqlGenerator.body(source, n, refToIdentifier) +
      pm.SparqlGenerator.solutionModifier()

    QueryRunner(source).query(query)
  }
}
