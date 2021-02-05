package inrae.semantic_web
import inrae.data.DataTestFactory
import inrae.semantic_web.rdf._
import utest._

import scala.concurrent.ExecutionContext.Implicits.global

object SWDiscoveryHelperTest  extends TestSuite  {

  val insert_data = DataTestFactory.insert_virtuoso1(
    """
      <http://aa> <http://bb> <http://cc> .
      <http://aa> <http://bb2> <http://cc2> .
      <http://aa> <http://bb2> <http://cc3> .

      <http://bb2> a owl:ObjectProperty .

      <http://aa1> a <http://LeafType> .

      <http://aa2> a <http://LeafType> .
      <http://aa2> a <http://OwlClass> .


      <http://aa3> <http://propDatatype> "test" .

      <http://OwlClass> a owl:Class .
      """.stripMargin, this.getClass.getSimpleName)

  val config: StatementConfiguration = DataTestFactory.getConfigVirtuoso1()


  def tests = Tests {
    test("count") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1") //http://rdf.ebi.ac.uk/terms/chembl#BioComponent
          .isSubjectOf(URI("http://bb2"))
          .helper
          .count
          .map(count => assert(count == 2))
      }).flatten
    }

    test("findClasses") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1")
          .set(URI("http://aa1"))
          .helper
          .findClasses()
          .map(types => assert(types.length == 1))
      }).flatten
    }

    test("findClasses with mother class -> owl:Class") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something("h1")
          .set(URI("http://aa2"))
          .helper
          .findClasses(URI("Class", "owl"))
          .map(types => assert(types.length == 1))
      }).flatten
    }


    test("findObjectProperties") {
      insert_data.map(_ => {
        SWDiscovery(config).something("h1")
          .set(URI("http://aa"))
          .helper
          .findObjectProperties()
          .map(response => assert(response.length == 2))
      }).flatten
    }

    test("findObjectProperties mother class --> owl:ObjectProperty ") {
      insert_data.map(_ => {
        SWDiscovery(config).something("h1")
          .set(URI("http://aa"))
          .helper
          .findObjectProperties(URI("ObjectProperty", "owl"))
          .map(response => assert(response.length == 1))
      }).flatten
    }
  }

}
