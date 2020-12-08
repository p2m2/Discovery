package inrae.semantic_web.sparql
import inrae.semantic_web.internal.Root
import utest._

object SparqlAlgebraTest extends TestSuite {

  def tests = Tests {
    test("Root creation") {
      val root = Root()
      val sa = SparqlAlgebra.nodeToSparqlAlgebra(root,root,false,-1,-1)
      println(sa)
    }
  }
}
