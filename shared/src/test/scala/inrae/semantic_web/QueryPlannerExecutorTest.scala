package inrae.semantic_web

import inrae.data.DataTestFactory
import inrae.semantic_web.internal.{Root, Something, SubjectOf}
import inrae.semantic_web.rdf.{IRI, URI}
import utest._

import scala.concurrent.Future

object QueryPlannerExecutorTest extends TestSuite {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val v1 = DataTestFactory.insert_virtuoso1(
    """
      <aa_v1> <bb_v1> <cc_v1> .
      <aa_v1> <bb_v1> <cc2_v1> .
      <aa_v1> <bb2_v1> <cc3_v1> .
      """.stripMargin, this.getClass.getSimpleName)

  val v2 = DataTestFactory.insert_virtuoso2(
    """
      <aa_v2> <bb_v2> <cc_v2> .
      <aa_v2> <bb_v2> <cc2_v2> .
      <aa_v2> <bb2_v2> <cc3_v2> .
      """.stripMargin, this.getClass.getSimpleName)

  val mpref = Map( "rdf" -> IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#"))

  val config1: StatementConfiguration = DataTestFactory.getConfigVirtuoso1()
  val config2: StatementConfiguration = DataTestFactory.getConfigVirtuoso2()

  def tests = Tests {
    /*
       S1
       S2
       S1,S2 -> (ETP1)
     */

    test("Simple case S1,S2 => ETP1") {
      Future.sequence(List(v1,v2)).map(
        _ => {
          val r  = Root()
          val s1 = Something("s1")
          val s2 = SubjectOf("s2",new URI("uri2"))
          r.addChildren(s1)
          s1.addChildren(s2)
          val plan = QueryPlanner.INTERSECTION_RESULTS_SET(
            Map( config1.sources()(0).id -> List(s1,s2))
          )

          QueryPlannerExecutor.executePlanning(r, plan, List("s1", "s2"), config1, mpref)
        }
      ).flatten
    }
  }
}
