package inrae.semantic_web

import inrae.data.DataTestFactory
import inrae.semantic_web.{SW, StatementConfiguration}
import inrae.semantic_web.rdf._
import utest._
import wvlet.log.Logger.rootLogger.error

import scala.language.postfixOps
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.{Await, blocking}
import scala.concurrent.duration._

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
        .onComplete {
          case Success(_) => {
            assert(false)
          }
          case Failure(_) => {
            assert(true)
          }
        }
    }

    test("something") {
      SW(config).something("h1")
        .select(List("h1"))
        .onComplete {
          case Success(_) => {
            assert(true)
          }
          case Failure(_) => {
            assert(false)
          }
        }
    }

    test("isSubjectOf") {
      SW(config)
        .something("h1")
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .set(URI("aa"))
        .isSubjectOf(URI("bb"), "var")
        .select(List("var"))
        .onComplete {
          case Success(result) => {
            assert(result("results")("bindings").arr.length == 1)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("var")).localName == "cc")
          }
          case Failure(exception) => {
            error(exception)
            assert(false)
          }
        }
    }

    test("count") {
      SW(config)
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .something("h1") //http://rdf.ebi.ac.uk/terms/chembl#BioComponent
        .isSubjectOf(URI("bb2"))
        .count()
        .onComplete {
          case Success(count) => {
            assert(count == 2)
          }
          case Failure(exception) => {
            error(exception)
            assert(false)
          }
        }
    }

    test("findClasses") {
      SW(config)
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .something("h1")
        .set(URI("aa1"))
        .findClasses()
        .onComplete {
          case Success(types) => {
            assert(types.length == 1)
            assert(true)
          }
          case Failure(exception) => {
            error(exception);
            assert(false) }
        }
    }

    test("findClasses with mother class -> owl:Class") {
      SW(config)
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .something("h1")
        .set(URI("aa2"))
        .findClasses(URI("Class","owl"))
        .onComplete {
          case Success(types) => {
            assert(types.length == 1)
          }
          case Failure(exception) => {
            error(exception);
            assert(false)
          }
        }
    }


    test("findObjectProperties") {
      SW(config).something("h1")
        .set(URI("aa"))
        .findObjectProperties()
        .onComplete {
          case Success(response) => {
            assert(response.length == 2)
          }
          case Failure(exception) => {
            error(exception)
            assert(false)
          }
        }
    }

    test("findObjectProperties mother class --> owl:ObjectProperty ") {
      SW(config).something("h1")
        .set(URI("aa"))
        .findObjectProperties(URI("ObjectProperty", "owl"))
        .onComplete {
          case Success(response) => {
            assert(response.length == 1)
            assert(true)
          }
          case Failure(exception) => {
            error(exception)
            assert(false)
          }
        }
    }
  }
}