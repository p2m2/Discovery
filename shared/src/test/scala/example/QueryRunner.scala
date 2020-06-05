package EasySparql

import org.scalatest._
import Matchers._

class QueryRunnerSpec extends FlatSpec with Matchers with PrivateMethodTester {
    "The package " should "create QueryRunner" in {
        val dbpediaRunner = QueryRunner("http://dbpedia.org/sparql")
    }
}