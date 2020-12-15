package inrae.semantic_web.sparql

import inrae.data.DataTestFactory
import inrae.semantic_web.StatementConfiguration
import utest._

import scala.util.{Failure, Success}

object QueryRunnerTest extends TestSuite {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  DataTestFactory.insert_virtuoso1("""<aa> <bb> <cc> .""".stripMargin, this.getClass.getSimpleName)

  val config: StatementConfiguration = DataTestFactory.getConfigVirtuoso1()

  def tests = Tests {
    test("Create a Query Runner. run same query. results should be equal.") {
      val query = "select distinct ?a where { ?a <bb> <cc> .} "
      val queryRunner = new QueryRunner(config.sources()(0),config.conf.settings)
      queryRunner.query(query)
        .onComplete( {
          case Success(result1) => {
            queryRunner.query(query)
              .onComplete( {
                case Success(result2) => {
                  assert( result1 == result2 )
                }
                case Failure(e) => assert(false)
              })
          }
          case Failure(e) => assert(false)
        })
    }

  }
}
