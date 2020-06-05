package EasySparql

import org.scalatest._

class RdfTypeSpec extends FlatSpec with Matchers {
 /* 
  "The Hello object" should "say hello" in {
    Hello.greeting shouldEqual "hello"
  }
*/
  "The package" should "create URI" in {
    val uri = new URI("test")
    uri.value shouldEqual "test"
  }

  "The package" should "create PropertyPath" in {
    val uri = new PropertyPath("<test>/<test2>*")
    uri.value shouldEqual "<test>/<test2>*"
  }
  
  "The package" should "create default Literal as xsd:string" in {
    val uri = new Literal("bidule")
    uri.value shouldEqual "bidule"
    uri.datatype shouldEqual "xsd:string"
    uri.tag shouldEqual null
  }

  "The package" should "create a Literal with a type" in {
    val uri = new Literal("bidule","type")
    uri.value shouldEqual "bidule"
    uri.datatype shouldEqual "type"
    uri.tag shouldEqual null
  }
   
  "The package" should "create a Literal with a type and a tag" in {
    val uri = new Literal("bidule","type","toto")
    uri.value shouldEqual "bidule"
    uri.datatype shouldEqual "type"
    uri.tag shouldEqual "toto"
  }

}
