package inrae.semantic_web

import inrae.semantic_web.rdf.URI
import utest._

object QueryPlannerTest extends TestSuite {

  def tests = Tests {
    /*
    test("Planning test EBI/uniprot") {
      val config: StatementConfiguration = new StatementConfiguration()
      config.setConfigString(
        """
          |{
          | "sources" : [{
          |   "id"  : "ebi",
          |   "url" : "https://www.ebi.ac.uk/rdf/services/sparql",
          |   "typ" : "tps",
          |   "method" : "POST_ENCODED",
          |   "mimetype" : "json"
          | },{
          |   "id"  : "uniprot",
          |   "url" : "https://sparql.uniprot.org/sparql",
          |   "typ" : "tps",
          |   "method" : "POST_ENCODED",
          |   "mimetype" : "json"
          | }]}
          |""".stripMargin)
      val query = new SW(config)

      query.something("h1")
        .prefix("ensembl","http://rdf.ebi.ac.uk/resource/ensembl/")
        .prefix("ensemblterms","http://rdf.ebi.ac.uk/terms/ensembl/")
        .prefix("dc","http://purl.org/dc/elements/1.1/")
        .prefix("core","http://purl.uniprot.org/core/")
        .prefix("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#")
        .set(URI("ENSG00000128573","ensembl"))                  // EBI
        .isSubjectOf(URI("DEPENDENT","ensemblterms"))           // EBI
        .isSubjectOf(URI("identifier","dc"))                    // EBI
        .isSubjectOf(URI("sequence","core"))                    // UNIPROT
        .isSubjectOf(URI("value","rdf"))
        .debug
      } */

    test("Wikidata / British Library") {
      val config: StatementConfiguration = new StatementConfiguration()
      config.setConfigString(
        """
          |{
          | "sources" : [{
          |   "id"  : "wikidata",
          |   "url" : "https://query.wikidata.org/sparql",
          |   "typ" : "tps",
          |   "method" : "POST_ENCODED",
          |   "mimetype" : "json"
          | },{
          |   "id"  : "bnb",
          |   "url" : "http://bnb.data.bl.uk/sparql",
          |   "typ" : "tps",
          |   "method" : "GET",
          |   "mimetype" : "json"
          | }]}
          |""".stripMargin)
      val query = new SW(config)
      query
        .prefix("schema","http://schema.org/")
        .prefix("library","http://purl.org/library/")
        .prefix("wdt","http://www.wikidata.org/prop/direct/")
        .prefix("dct","http://purl.org/dc/terms/")
        .something("author")
        .isSubjectOf(URI("P214","wdt"),"creatorID")
        .isObjectOf(URI("P50","wdt"),"work")
        .isSubjectOf(URI("wdt:P1476","wdt"),"workLabel")
        .focus("creatorID")
        .isObjectOf(URI("creator","dct"),"work")
        .isSubjectOf(URI("title","dct"),"work")
    }
  }
}