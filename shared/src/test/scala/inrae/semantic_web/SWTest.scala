package inrae.semantic_web

import inrae.data.DataTestFactory
import inrae.semantic_web.rdf._
import utest._
import wvlet.log.Logger.rootLogger.error

import scala.language.postfixOps
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object SWTest extends TestSuite {

  DataTestFactory.insert_virtuoso1(
    """
      <aa> <bb> <cc> .
      <aa> <bb2> <cc2> .
      <aa> <bb2> <cc3> .

      <bb2> a owl:ObjectProperty .

      <aa1> a <LeafType> .

      <aa2> a <LeafType> .
      <aa2> a <OwlClass> .

      <OwlClass> a owl:Class .
      """.stripMargin, this.getClass.getSimpleName)

  val config: StatementConfiguration = DataTestFactory.getConfigVirtuoso1()

  override def utestBeforeEach(path: Seq[String]): Unit = {

  }

  override def utestAfterEach(path: Seq[String]): Unit = {

  }

  override def utestAfterAll(): Unit = {
    DataTestFactory.delete_virtuoso1(this.getClass.getSimpleName)
  }

  def tests = Tests {
    test("No sources definition") {
      val config: StatementConfiguration = StatementConfiguration()
      config.setConfigString(""" { "sources" : [] } """)
      SW(config).something("h1")
        .select(List("h1"))
        .commit()
        .raw
        .map(_ => assert(false))
        .recover((_) => assert(true))
    }

    test("something") {
      SW(config).something("h1")
        .select(List("h1"))
        .commit()
        .raw
        .map(_ => assert(true))
    }

    test("isSubjectOf") {
      SW(config)
        .something("h1")
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .set(URI("aa"))
        .isSubjectOf(URI("bb"), "var")
        .select(List("var"))
        .commit()
        .raw
        .map(result => {
          assert(result("results")("bindings").arr.length == 1)
          assert(SparqlBuilder.createUri(result("results")("bindings")(0)("var")).localName == "cc")
        })
    }

    test("count") {
      SW(config)
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .something("h1") //http://rdf.ebi.ac.uk/terms/chembl#BioComponent
        .isSubjectOf(URI("bb2"))
        .count()
        .map(count => assert(count == 2))
    }

    test("findClasses") {
      SW(config)
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .something("h1")
        .set(URI("aa1"))
        .findClasses()
        .map(types => assert(types.length == 1))
    }

    test("findClasses with mother class -> owl:Class") {
      SW(config)
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .something("h1")
        .set(URI("aa2"))
        .findClasses(URI("Class", "owl"))
        .map(types => assert(types.length == 1))
    }


    test("findObjectProperties") {
      SW(config).something("h1")
        .set(URI("aa"))
        .findObjectProperties()
        .map(response => assert(response.length == 2))
    }

    test("findObjectProperties mother class --> owl:ObjectProperty ") {
      SW(config).something("h1")
        .set(URI("aa"))
        .findObjectProperties(URI("ObjectProperty", "owl"))
        .map(response => assert(response.length == 1))
    }
  }
}
