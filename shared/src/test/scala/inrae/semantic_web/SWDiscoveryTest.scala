package inrae.semantic_web

import inrae.data.DataTestFactory
import inrae.semantic_web.rdf._
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

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

    test("count") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1") //http://rdf.ebi.ac.uk/terms/chembl#BioComponent
          .isSubjectOf(URI("http://bb2"))
          .count()
          .map(count => assert(count == 2))
      }).flatten
    }

    test("findClasses") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1")
          .set(URI("http://aa1"))
          .findClasses()
          .map(types => assert(types.length == 1))
      }).flatten
    }

    test("findClasses with mother class -> owl:Class") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1")
          .set(URI("http://aa2"))
          .findClasses(URI("Class", "owl"))
          .map(types => assert(types.length == 1))
      }).flatten
    }


    test("findObjectProperties") {
      insert_data.map(_ => {
        SWDiscovery(config).something("h1")
          .set(URI("http://aa"))
          .findObjectProperties()
          .map(response => assert(response.length == 2))
      }).flatten
    }

    test("findObjectProperties mother class --> owl:ObjectProperty ") {
      insert_data.map(_ => {
        SWDiscovery(config).something("h1")
          .set(URI("http://aa"))
          .findObjectProperties(URI("ObjectProperty", "owl"))
          .map(response => assert(response.length == 1))
      }).flatten
    }
  }

  TestRunner.runAsync(tests).map { _ => {
    DataTestFactory.delete_virtuoso1(this.getClass.getSimpleName)
    }
  }

}
