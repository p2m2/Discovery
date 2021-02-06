package inrae.semantic_web

import inrae.data.DataTestFactory
import inrae.semantic_web.rdf.{IRI, Literal, SparqlBuilder, URI}
import utest.{TestSuite, Tests, test}

import scala.concurrent.ExecutionContext.Implicits.global

object BindTest extends TestSuite {
  val insert_data = DataTestFactory.insert_virtuoso1(
    """
      <http://aa1> <http://bb> "abcdef" .
      <http://aa2> <http://bb> "abcdefghij" .
      <http://aa3> <http://bb> "abcdefghijklmn" .
      <http://aa1> <http://bb> "defijklm" .
      """.stripMargin, this.getClass.getSimpleName)

  val config: StatementConfiguration = DataTestFactory.getConfigVirtuoso1()

  def tests = Tests {
    val regex = "defg"

    test("bind regex") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something()
          .isSubjectOf(URI("http://bb"),"r")
          .bind("reg").regex(regex)
          .filter.equal(Literal("true"))
          .select(Seq("r","reg"))
          .distinct
          .commit()
          .raw.map(r => {
          assert(r("results")("bindings").arr.count(v => v("r")("value").toString.contains(regex)) == 2)
        })
      }).flatten
    }

    test("bind replace") {
      val pat = "defg"
      val repl ="aaaaa"
      val req = SWDiscovery(config)
        .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
        .something()
        .isSubjectOf(URI("http://bb"),"r")
        .bind("rep").replace(pat,repl)


      insert_data.map(_ => {
        req
          .select(Seq("rep"))
          .distinct
          .commit()
          .raw.map(r => {
          assert(r("results")("bindings").arr.count(v => v("rep")("value").toString.contains(repl)) == 2)
        }).recover(e => println(e))
      }).flatten

      insert_data.map(_ => {
        SWDiscovery().setSerializedString(req.getSerializedString)
          .select(Seq("rep"))
          .distinct
          .commit()
          .raw.map(r => {
          assert(r("results")("bindings").arr.count(v => v("rep")("value").toString.contains(repl)) == 2)
        })
      }).flatten

    }

    test("bind abs") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something()
          .set(Literal("-5.5","http://www.w3.org/2001/XMLSchema#decimal"))
          .bind("new_value").abs()
          .select(Seq("new_value"))
          .commit()
          .raw.map(r => {
          assert(SparqlBuilder.createLiteral(r("results")("bindings").arr(0)("new_value")).toDouble == 5.5)
        })
      }).flatten
    }

    test("bind round") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something()
          .set(Literal("-5.5","http://www.w3.org/2001/XMLSchema#decimal"))
          .bind("new_value").round()
          .select(Seq("new_value"))
          .commit()
          .raw.map(r => {
          assert(SparqlBuilder.createLiteral(r("results")("bindings").arr(0)("new_value")).toInt == -5)
        })
      }).flatten
    }
    test("bind ceil") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something()
          .set(Literal("-5.5","http://www.w3.org/2001/XMLSchema#decimal"))
          .bind("new_value").ceil()
          .select(Seq("new_value"))
          .commit()
          .raw.map(r => {
          assert(SparqlBuilder.createLiteral(r("results")("bindings").arr(0)("new_value")).toInt == -5)
        })
      }).flatten
    }

    test("bind floor") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something()
          .set(Literal("-5.5","http://www.w3.org/2001/XMLSchema#decimal"))
          .bind("new_value").floor()
          .select(Seq("new_value"))
          .commit()
          .raw.map(r => {
          assert(SparqlBuilder.createLiteral(r("results")("bindings").arr(0)("new_value")).toInt == -6)
        })
      }).flatten
    }
    test("bind rand") {
      insert_data.map(_ => {
        SWDiscovery(config)
          .graph(IRI(DataTestFactory.graph1(this.getClass.getSimpleName)))
          .something()
          .bind("new_value").rand()
          .select(Seq("new_value"))
          .commit()
          .raw.map(r => {
          val v = SparqlBuilder.createLiteral(r("results")("bindings").arr(0)("new_value")).toDouble
          assert( v<=1.0 && v >0.0)
        })
      }).flatten
    }
  }

}
