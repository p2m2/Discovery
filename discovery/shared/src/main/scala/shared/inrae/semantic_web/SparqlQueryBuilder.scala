package inrae.semantic_web
import wvlet.log.Logger.rootLogger._
import inrae.semantic_web.internal.{Root, pm}
import inrae.semantic_web.rdf.IRI

object SparqlQueryBuilder {

  def baseQuery(n: Root,refToIdentifier : Map[String,String]) : String = {

    (pm.SparqlGenerator.from(n.defaultGraph) + "\n" +
      pm.SparqlGenerator.fromNamed(n.namedGraph) + "\n" +
      pm.SparqlGenerator.start_where() + "\n" +
      pm.SparqlGenerator.body(n, refToIdentifier) + "\n" +
      pm.SparqlGenerator.solutionModifier())
  }

  def selectQueryString(n: Root,
                  refToIdentifier : Map[String,String],
                  listVariables : Seq[String]): String = {
    debug(" -- selectQueryString -- ")

    (pm.SparqlGenerator.prefixes(n.prefixes) + "\n" +
      pm.SparqlGenerator.queryFormSelect(listVariables) + "\n" +
      baseQuery(n,refToIdentifier)).replace("\n\n","\n")

  }

  def countQueryString(n: Root,
                        refToIdentifier : Map[String,String],
                       varCount : String): String = {
    debug(" -- countQueryString -- ")

    (pm.SparqlGenerator.prefixes(n.prefixes) + "\n" +
      pm.SparqlGenerator.prologCountSelection(varCount) + "\n" +
      baseQuery(n,refToIdentifier)).replace("\n\n","\n")

  }
}
