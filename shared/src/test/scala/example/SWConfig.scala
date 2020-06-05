package EasySparql

import org.scalatest._

class SWConfigSpec extends FlatSpec with Matchers {
    "The package " should "create SW object with 1 endpoint" in {
        val query = new SW(new SWConfig("""{
            "endpoints" : [ "http://exemple1/sparql" ]
        }"""))

        query.config.get("endpoints") shouldEqual ""
        query.config.getIndex("endpoints",0) shouldEqual "http://exemple1/sparql"
    }

    "The package " should "create SW object with 2 endpoints" in {
        val query = new SW(new SWConfig("""{
            "endpoints" : ["http://exemple1/sparql" , "http://exemple2/sparql"]
        }"""))

        query.config.get("endpoints") shouldEqual ""
        query.config.getIndex("endpoints",0) shouldEqual "http://exemple1/sparql"
        query.config.getIndex("endpoints",1) shouldEqual "http://exemple2/sparql"
    }
}