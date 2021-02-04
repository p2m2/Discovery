package inrae.semantic_web

import inrae.data.DataTestFactory
import inrae.semantic_web.rdf.{IRI, URI}
import utest.{TestSuite, Tests}

import scala.concurrent.ExecutionContext.Implicits.global

object OrderByTest extends TestSuite {
  val insert_data = DataTestFactory.insert_virtuoso1(
    """
      <http://aa> <http://bb> "2"^^sd:integer .
      <http://aa> <http://bb> "3"^^sd:integer .
      <http://aa> <http://bb> "1"^^sd:integer .
      """.stripMargin, this.getClass.getSimpleName)

  val config: StatementConfiguration = DataTestFactory.getConfigVirtuoso1()

  def tests = Tests {
    insert_data.map(_ => {
      SWDiscovery(config)
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .something()
        .isSubjectOf(URI("http://bb"),"v")
        .select(Seq("v"))
        .commit()
        .raw.map( r => println(r))
    }).flatten
  }
}
