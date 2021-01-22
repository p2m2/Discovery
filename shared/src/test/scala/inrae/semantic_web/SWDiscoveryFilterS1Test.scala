package inrae.semantic_web

import inrae.data.DataTestFactory
import inrae.semantic_web.rdf.{IRI, SparqlBuilder, URI}
import utest._

object SWDiscoveryFilterS1Test extends TestSuite {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val insert_data =
    DataTestFactory.insert_virtuoso1(
      """
      <aaSWFilterTest> a <http://www.w3.org/2002/07/owl#Thing> .
      <aaSWFilterTest> <some> "test" .
      <aaSWFilterTest2> a <url_w3_class_stuff> .
      <aaSWFilterTest2> <some> "test" .

      <http://www.w3.org/2002/07/owl#Thing> a <http://www.w3.org/2002/07/owl#Class> .
      <url_w3_class_stuff> a <http://www.w3.org/2002/07/owl#Class> .
      """.stripMargin, this.getClass.getSimpleName)

  val config: StatementConfiguration = DataTestFactory.getConfigVirtuoso1()

  def tests = Tests {
    test("SW Filter contains") {
      insert_data.map(_ => {
        val trans = SWDiscovery(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("instance")
            .isSubjectOf(URI("a"))
              .set(URI("Class", "owl"))
          .focus("instance")
            .filter.contains("w3")
          .focus("instance")
            .filter.not.contains("http://www.w3.org/2002/07/owl")
          .select(List("instance"))


        trans.commit()
          .raw
          .map(result => {
            assert(result("results")("bindings").arr.length > 0)
            assert(SparqlBuilder.createUri(result("results")("bindings")(0)("instance")).localName.contains("w3"))
            assert(!SparqlBuilder.createUri(result("results")("bindings")(0)("instance")).localName.contains("http://www.w3.org/2002/07/owl"))
          })
      }).flatten
    }
  }

  TestRunner.runAsync(tests).map { _ => {
    DataTestFactory.delete_virtuoso1(this.getClass.getSimpleName)
  }
  }
}
