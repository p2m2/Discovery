package EasySparql

import org.scalatest._
import Matchers._

class SWSpec extends FlatSpec with Matchers with PrivateMethodTester {
    "The package " should "create SW object" in {
        val query = new SW()
    }

    "The package " should "create Something object" in {
        val query = new SW().something()
        val focusNode  = PrivateMethod[Node](Symbol("focusNode"))
        (query invokePrivate focusNode()).asInstanceOf[Something].uniqRef should not be ""
    }

    "The package " should "create Something object with a reference toto" in {
        val query = new SW().something("toto")
        val focusNode  = PrivateMethod[Node](Symbol("focusNode"))
        (query invokePrivate focusNode()).asInstanceOf[Something].uniqRef shouldEqual "toto"
    }


}