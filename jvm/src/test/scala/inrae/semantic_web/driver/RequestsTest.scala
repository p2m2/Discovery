package inrae.semantic_web.driver

import inrae.data.DataTestFactory
import inrae.semantic_web.rdf.{SparqlBuilder, URI}
import inrae.semantic_web.{SWDiscovery, StatementConfiguration}
import utest.{TestSuite, Tests, test}

object RequestsTest extends TestSuite {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val insert_data = DataTestFactory.insert_virtuoso1(
    """
      <http://aaaaaa> <http://bbbbbb> <http://cc> .
      """.stripMargin, this.getClass.getSimpleName)

  val config: StatementConfiguration = StatementConfiguration.setConfigString(
    s"""
        {
         "sources" : [{
           "id"       : "local_sparql",
           "url"      : "${DataTestFactory.url_endpoint}"
         }],
         "settings" : {
            "logLevel" : "off",
            "sizeBatchProcessing" : 100
          }
         }
        """.stripMargin)

  val config2: StatementConfiguration = StatementConfiguration.setConfigString(
    """
        {
         "sources" : [{
           "id"       : "local_content",
           "content"  : "<http://aaaaaa> <http://bbbbbb2> <http://cc2> .",
           "mimetype" : "text/turtle"
         }],
         "settings" : {
            "logLevel" : "off",
            "sizeBatchProcessing" : 100
          }
         }
        """.stripMargin)

  val config3: StatementConfiguration = StatementConfiguration.setConfigString(
    """
        {
         "sources" : [{
           "id"       : "local_content",
           "content"  : "<http://aaaaaa> <http://bbbbbb2> <http://cc2> .",
           "mimetype" : "text/turtle"
         },{
           "id"       : "local_content2",
           "content"  : "<http://aaaaaa> <http://bbbbbb2> <http://cc3> .",
           "mimetype" : "text/turtle"
         }],
         "settings" : {
            "logLevel" : "off",
            "sizeBatchProcessing" : 100
          }
         }
        """.stripMargin)

  val mixconfig: StatementConfiguration = StatementConfiguration.setConfigString(
    s"""
        {
         "sources" : [
         {
           "id"       : "local_sparql",
           "url"      : "${DataTestFactory.url_endpoint}"
         },
         {
           "id"       : "local_content",
           "content"  : "<http://aaaaaa> <http://bbbbbb> <http://cc2> .",
           "mimetype" : "text/turtle"
         }],
         "settings" : {
            "logLevel" : "off",
            "sizeBatchProcessing" : 100
          }
         }
        """.stripMargin)


  def tests = Tests {

    test("federation") {
        SWDiscovery(mixconfig)
          .something("sub")
          .isSubjectOf(URI("http://bbbbbb"),"obj")
          //.console()
          .select(List("sub","obj"))
          .commit()
          .raw
          .map(result =>
          {
            assert(result("results")("bindings").arr.length == 2)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("sub")).localName == "http://aaaaaa")
          }
          )
    }

    test("inline turtle") {
      insert_data.map(_ => {
        SWDiscovery(config2)
          .something("h1")
          .isSubjectOf(URI("http://bbbbbb2"))
          //.console()
          .select(List("h1","v"))
          .commit()
          .raw
          .map(result =>
          {
            assert(result("results")("bindings").arr.length > 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("h1")).localName == "http://aaaaaa")
          }
          )
      }).flatten
    }

    test("inline turtle 2") {
      insert_data.map(_ => {
        SWDiscovery(config3)
          .something("h1")
          .isSubjectOf(URI("http://bbbbbb2"),"v")
          //.console()
          .select(List("v"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 2)
          }
          )
      }).flatten
    }
  }

}
