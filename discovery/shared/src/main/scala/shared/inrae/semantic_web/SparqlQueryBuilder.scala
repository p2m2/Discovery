package inrae.semantic_web
import wvlet.log.Logger.rootLogger._
import inrae.semantic_web.internal.{Root, pm}
import inrae.semantic_web.rdf.IRI

object SparqlQueryBuilder {

  def queryString(n: Root,
                  refToIdentifier : Map[String,String],
                  listVariables : Seq[String],
                  prefixes : Map[String,IRI]): String = {
    debug(" -- queryString -- ")

    (pm.SparqlGenerator.prefixes(prefixes) + "\n" +
      pm.SparqlGenerator.queryFormSelect(listVariables) + "\n" +
      pm.SparqlGenerator.from(n.defaultGraph) + "\n" +
      pm.SparqlGenerator.fromNamed(n.namedGraph) + "\n" +
      pm.SparqlGenerator.start_where() + "\n" +
      pm.SparqlGenerator.body(n, refToIdentifier) + "\n" +
      pm.SparqlGenerator.solutionModifier())

      .replace("\n\n","\n")
  }
}
