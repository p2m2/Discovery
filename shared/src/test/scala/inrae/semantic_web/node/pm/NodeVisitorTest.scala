package inrae.semantic_web.node.pm
import inrae.semantic_web.node.{RdfNode, Something}
import utest.{TestSuite, Tests, test}


object NodeVisitorTest extends TestSuite {

  def tests = Tests {
    test("getNodeWithRef") {
      val listNodes : Array[RdfNode] = NodeVisitor.getNodeWithRef("test",Something("test"))
      assert(listNodes.length==1)
      assert(listNodes(0).idRef == "test")
    }
    test("getNodeWithRef empty") {
      val listNodes : Array[RdfNode] = NodeVisitor.getNodeWithRef("test",Something("autre"))
      assert(listNodes.length==0)
    }

    test("getNodeWithRef deep") {
      val listNodes : Array[RdfNode] = NodeVisitor.getNodeWithRef("test",Something("autre",List(Something("test"))))
      assert(listNodes.length==1)
    }

    test("browse") {
      assert(NodeVisitor.map(Something("test"),0,  (n,p ) => {
        assert(n.idRef == "test")
        assert(p == 0)
        n.idRef->p
      }) == List( "test" -> 0 ) )
    }

    test("browse deep") {
      assert(NodeVisitor.map(Something("autre",List(Something("test"))), 0, (n,p) => {
        n.idRef->p
      }) == List( "autre" -> 0 , "test" -> 1 ) )
    }
  }
}
