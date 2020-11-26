package inrae.semantic_web

import inrae.semantic_web.internal.{Node, Root, Something, SubjectOf}
import inrae.semantic_web.rdf.{URI,IRI}
import utest._

object QueryPlannerExecutorTest extends TestSuite {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val mpref = Map( "rdf" -> IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#"))

  val configTest: StatementConfiguration = new StatementConfiguration()
  configTest.setConfigString(
    """
      |{
      | "sources" : [{
      |   "id"  : "etp1",
      |   "url" : "https://etp1/sparql",
      |   "typ" : "tps",
      |   "method" : "POST"
      | },{
      |   "id"  : "etp2",
      |   "url" : "https://etp2/sparql",
      |   "typ" : "tps",
      |   "method" : "POST"
      | }]}
      |""".stripMargin)

  def tests = Tests {
    /*
       S1
       S2
       S1,S2 -> (ETP1)
     */

    test("Simple case S1,S2 => ETP1") {


      val r  = Root()
      val s1 = Something("s1")
      val s2 = SubjectOf("s2",new URI("uri2"))
      r.addChildren(s1)
      s1.addChildren(s2)
      val plan = QueryPlanner.INTERSECTION_RESULTS_SET(
        Map( "etp1" -> List(s1,s2))
      )
      QueryPlannerExecutor.executePlanning(r,plan,List("s1","s2"),configTest,mpref)
    }
  }
}
