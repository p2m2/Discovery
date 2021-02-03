package inrae.semantic_web

import inrae.data.DataTestFactory
import inrae.semantic_web.rdf._
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object SWDiscoveryTest extends TestSuite {

  val insert_data = DataTestFactory.insert_virtuoso1(
    """
      <http://aa> <http://bb> <http://cc> .
      <http://aa> <http://bb2> <http://cc2> .
      <http://aa> <http://bb2> <http://cc3> .

      <http://bb2> a owl:ObjectProperty .

      <http://aa1> a <http://LeafType> .

      <http://aa2> a <http://LeafType> .
      <http://aa2> a <http://OwlClass> .


      <http://aa3> <http://propDatatype> "test" .

      <http://OwlClass> a owl:Class .
      """.stripMargin, this.getClass.getSimpleName)

  val config: StatementConfiguration = DataTestFactory.getConfigVirtuoso1()


  def tests = Tests {
    test("help") {
      SWDiscovery(config).something("h1").usage.isObjectOf(URI("something_uri"))
    }

    test("No sources definition") {
      insert_data.map(_ => {
        val config: StatementConfiguration = StatementConfiguration.setConfigString(""" { "sources" : [] } """)
        SWDiscovery(config).something("h1")
          .select(List("h1"))
          .commit()
          .raw
          .map(_ => assert(false))
          .recover((_) => assert(true))
      }).flatten
    }

    test("something") {
      insert_data.map(_ => {
        SWDiscovery(config).something("h1")
          .select(List("h1"))
          .commit()
          .raw
          .map(_ => assert(true))
      }).flatten
    }

    test("isSubjectOf") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .something("h1")
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .set(URI("http://aa"))
          .isSubjectOf(URI("http://bb"), "var")
          .select(List("var"))
          .commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length == 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("var")).localName == "http://cc")
          })
      }).flatten
    }

    test("datatype") {
      insert_data.map(_ => {
        SWDiscovery(config).something("h1")
          .set(URI("http://aa3"))
          .datatype(URI("http://propDatatype"), "d")
          .select(List("d"))
          .commit()
          .raw
          .map(
            response => {
              assert(response("results")("datatypes")("d")("http://aa3")(0)("value").toString().length > 0)
            }
          )
      }).flatten
    }


    test("bad focus") {
      Try(SWDiscovery(config)
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .something("h1") //http://rdf.ebi.ac.uk/terms/chembl#BioComponent
        .focus("h2")) match {
        case Success(_) => assert(false)
        case Failure(_) => assert(true)
      }
    }

    test("use named graph") {
      Try( SWDiscovery(config)
          .namedGraph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1") //http://rdf.ebi.ac.uk/terms/chembl#BioComponent
          .isSubjectOf(URI("http://bb2"))) match {
        case Success(_) => assert(true)
        case Failure(_) => assert(false)
      }
    }

    test("test console") {
      Try( SWDiscovery(config)
        .namedGraph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .something("h1") //http://rdf.ebi.ac.uk/terms/chembl#BioComponent
        .isSubjectOf(URI("http://bb2"))
        .console()) match {
        case Success(_) => assert(true)
        case Failure(_) => assert(false)
      }
    }
  }
}
