package inrae.semantic_web

import inrae.data.DataTestFactory
import inrae.semantic_web.rdf._
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success}

object SWDiscoverySelectIterable$ extends TestSuite {

  DataTestFactory.delete_virtuoso1(this.getClass.getSimpleName)

  val data = """
      <aa> <bb> 1 .
      <aa> <bb> 2 .
      <aa> <bb> 3 .
      <aa> <bb> 4 .
      <aa> <bb> 5 .
      <aa> <bb> 6 .
      <aa> <bb> 7 .
      <aa> <bb> 8 .
      <aa> <bb> 9 .
      <aa> <bb> 10 .
      <aa> <bb> 11 .
      <aa> <bb> 12 .

      """.stripMargin

  DataTestFactory.insert_virtuoso1(data, this.getClass.getSimpleName)

  val nbValues = data.split(" ").filter( _ == "<aa>").length

  val pageSize = 5

  val nblock = (nbValues / pageSize) + 1


  var config: StatementConfiguration = StatementConfiguration().setConfigString(
    s"""
        {
         "sources" : [{
           "id"       : "local",
           "url"      : "${DataTestFactory.url_endpoint}",
           "type"     : "tps",
           "method"   : "POST",
           "mimetype" : "json"
         }],
         "settings" : {
            "logLevel" : "info",
            "sizeBatchProcessing" : 100,
            "pageSize" : ${pageSize}
          }
         }
        """.stripMargin)

  override def utestAfterAll(): Unit = {
    //DataTestFactory.delete_virtuoso1(this.getClass.getSimpleName)
  }

  def tests = Tests {

    test("something") {
      val s = SWDiscovery(config)
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .something()
        .set(URI("<aa>"))
        .isSubjectOf(URI("bb"), "obj")
        .selectByPage(List("obj"))
        .onComplete {
          case Success((nb,results)) => {
            assert(nb == nblock)
            val listR = Future.sequence((0 to nblock-1).map( iblock => {
              results(iblock).commit().raw.map({
                r => {
                  assert(r("results")("bindings").arr.length<=pageSize)
                  r("results")("bindings").arr.map( json => SparqlBuilder.createLiteral(json("obj")))
                                              .map( lit => lit.toInt() )}
              })
            }))
            listR.onComplete {
              case Success(list) => {
                assert(list.flatten.sorted == List(1,2,3,4,5,6,7,8,9,10,11,12))
              }
              case Failure(e) => {
                assert(false)
              }
            }
          }
          case Failure(e) => {
            assert(false)
          }
        }
    }

  }
}
