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


  DataTestFactory.insert(
    """
      <aa> <bb> <cc> .
      <bbb> <ccc> <aaa>
      """.stripMargin,this.getClass.getSimpleName)

  val config: StatementConfiguration = DataTestFactory.getConfig()

  override def utestBeforeEach(path: Seq[String]): Unit = {

  }

  override def utestAfterEach(path: Seq[String]): Unit = {

  }

  override def utestAfterAll(): Unit = {
    DataTestFactory.delete(this.getClass.getSimpleName)
  }

  def tests = Tests {


    test("Create a simple query") {
      val config: StatementConfiguration = StatementConfiguration()
      config.setConfigString(""" { "sources" : [] } """)
      SW(config).something("h1")
    }

    test("Create a query finding an object") {

      val query = SW(config)

        query.something("h1")
          .graph(IRI(DataTestFactory.graph(this.getClass.getSimpleName)))
          .set(URI("aa"))
          .isSubjectOf(URI("bb"),"var")
          .select(List("var"))
          .onComplete {
            case Success(result) => {
              assert(result("results")("bindings").arr.length == 1)
              assert(SparqlBuilder.createUri(result("results")("bindings")(0)("var")).localName=="cc")
            };
            case Failure(exception) => {
              error(exception)
              assert(false)
            };
          }
    }
/*
        test("debug") {
          val query = new SW(config)

          query.something("h1")
              .set(URI("http://dbpedia.org/resource/%C3%84lvdalen"))
              .isSubjectOf(URI("http://www.w3.org/2002/07/owl#sameAs"))
        }

        test("count") {
          val query = new SW(config)
          query.something("h1") //http://rdf.ebi.ac.uk/terms/chembl#BioComponent
            .isSubjectOf(URI("http://dbpedia.org/ontology/deathDate"))
            .count()
            .onComplete {
            case Success(count) => {
              println(count)
              assert(true)
            }
            case Failure(exception) => {
              System.err.println(exception)
              assert(false)
            }
          }
        }
/*
    TestRunner.runAsync(tests).map { results =>
      val leafResults = results.leaves.toSeq
      assert(leafResults(0).value.isSuccess) // root
      assert(leafResults(1).value.isSuccess) // testSuccess
    }*/

        test("findTypeOf") {
          val query = new SW(config)

          Future {
            query.something("h1")
              .set(URI("http://dbpedia.org/resource/Abbie_Hoffman"))
              .findClassesOf()
              .onComplete {
                case Success(types) => println(types); assert(true)
                case Failure(exception) => println(exception); assert(false)
              }
          }
        }
        test("findTypeOf - owl:Class") {
          val query = new SW(config)

          Future {
            query.something("h1")
              .set(URI("http://dbpedia.org/resource/Abbie_Hoffman"))
              .findClassesOf(URI("http://www.w3.org/2002/07/owl#Class"))
              .onComplete {
                case Success(types) => println(types); assert(true)
                case Failure(exception) => println(exception); assert(false)
              }
          }
        }

        test("findTypeOf - rdf:type") {
          val query = SW(config)

          Future {
            query.something("h1")
              .set(URI("http://dbpedia.org/resource/Abbie_Hoffman"))
              .findClassesOf(URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
              .onComplete {
                case Success(types) => println(types); assert(true)
                case Failure(exception) => println(exception); assert(false)
              }
          }
        }

        test("findObjectPropertiesOf") {
          val query = SW(config)

          Future {
            query.something("h1")
              .set(URI("http://dbpedia.org/resource/Abbie_Hoffman"))
              .findObjectProperties()
              .onComplete {
                case Success(response) => {
                  println(response)
                  assert ( response.length > 3 )
                  assert(true)
                }
                case Failure(exception) => println(exception); assert(false)
              }
          }
          test("findObjectPropertiesOf inherited from ") {
            val query = new SW(config)

            Future {
              query.something("h1")
                .set(URI("http://dbpedia.org/resource/Abbie_Hoffman"))
                .findObjectProperties(URI("ObjectProperty","owl"))
                .onComplete {
                  case Success(response) => {
                    println(response)
                    assert ( response.length > 3 )
                    assert(true)
                  }
                  case Failure(exception) => println(exception); assert(false)
                }
            }
          }


        }  */
  }
}