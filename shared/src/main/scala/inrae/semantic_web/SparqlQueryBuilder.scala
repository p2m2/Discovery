package inrae.semantic_web
import wvlet.log.Logger.rootLogger._
import inrae.semantic_web.internal.{Root, pm}
import inrae.semantic_web.rdf.IRI

object SparqlQueryBuilder {
  /**
   *
   * @param n : Node to transform
   * @param refToIdentifier : mapping user variable name to internal variable name
   * @param limit : upper bound on the number of solutions returned
   * @param offset : solution are generated after this offset
   * @return
   */
  def baseQuery(n: Root,refToIdentifier : Map[String,String],limit : Int, offset : Int) : String = {

    (pm.SparqlGenerator.from(n.defaultGraph) + "\n" +
      pm.SparqlGenerator.fromNamed(n.namedGraph) + "\n" +
      pm.SparqlGenerator.start_where() + "\n" +
      pm.SparqlGenerator.body(n, refToIdentifier) + "\n" +
      pm.SparqlGenerator.solutionModifier(limit,offset))
  }

  /**
   *
   *
   * @param n : Node to transform
   * @param refToIdentifier : mapping user variable name to internal variable name
   * @param listVariables : selected variable
   * @param limit : upper bound on the number of solutions returned
   * @param offset : solution are generated after this offset
   * @return
   */
  def selectQueryString(n: Root,
                  refToIdentifier : Map[String,String],
                  listVariables : Seq[String],
                        limit : Int,
                        offset : Int): String = {
    debug(" -- selectQueryString -- ")

    (pm.SparqlGenerator.prefixes(n.prefixes) + "\n" +
      pm.SparqlGenerator.queryFormSelect(listVariables) + "\n" +
      baseQuery(n,refToIdentifier,limit,offset)).replace("\n\n","\n")

  }

  def countQueryString(n: Root,
                        refToIdentifier : Map[String,String],
                       varCount : String): String = {
    debug(" -- countQueryString -- ")

    (pm.SparqlGenerator.prefixes(n.prefixes) + "\n" +
      pm.SparqlGenerator.prologCountSelection(varCount) + "\n" +
      baseQuery(n,refToIdentifier,0,0)).replace("\n\n","\n")

  }
}
