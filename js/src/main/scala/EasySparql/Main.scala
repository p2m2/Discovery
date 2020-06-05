package EasySparql

import play.api.libs.json._
import java.util.UUID.randomUUID

object TutorialApp {
  def main(args: Array[String]): Unit = {

    println("Hello world!")
    
    println(Obj.bar(42))
    println(Member(42))
    val user = new User("Julien", 30)
    println(user.name)
    println(Func(8, 12))
    
    val query = new SW()
    val r = query.something("h1")
               .set(new URI("http://dbpedia.org/resource/%C3%84lvdalen"))
               .isSubjectOf(new URI("http://www.w3.org/2002/07/owl#sameAs"))
               .select()
    println(r)
  }
}
/*
object Hello extends Greeting with App {
  
    val query = new SW()

    println("----------------------------------------------------------");
    println(query.something("h1")
           .isSubjectOf(new URI("uri://uri1"))
             .isSubjectOf(new URI("uri://uri2"))
         .focus("h1")
           .isObjectOf(new URI("uri://uri3"))
         .focus("h1")
         .sparql())

  val r = query.something("h1")
               .set(new URI("http://dbpedia.org/resource/%C3%84lvdalen"))
               .isSubjectOf(new URI("http://www.w3.org/2002/07/owl#sameAs"))
               .select()
   println(r)
   */
    //query.print()
  /*
    val query = "SELECT ?prop ?place WHERE { <http://dbpedia.org/resource/%C3%84lvdalen> ?prop ?place .}"

    val dbpediaRunner = QueryRunner("http://dbpedia.org/sparql")
    val result = dbpediaRunner
      .query(query)

    printResult("JSON", result.asJson())
    printResult("CSV", result.asCSV())
    printResult("XML", result.asXML())

  def printResult(name: String, result: Option[String]): Unit = {
    val res = result.getOrElse("")
    println(name)
    println(res)
    println()
  }
  */
//}

trait Greeting {
  lazy val greeting: String = "hello"
}

/**
 * using doc to validate JSON config:
 * see https://www.playframework.com/documentation/2.8.x/ScalaJson
 *
 * @param json_conf
 */
class SWConfig(var json_conf: String) {
  var conf: JsValue = Json.parse(json_conf)
  def get(key : String) : String = {
    val name = (conf \ "endpoints").asOpt[String] match {
      case None => return ""//Or handle the lack of a value another way: throw an error, etc.
      case Some(s: String) => return s //return the string to set your value
    }

    println("results")
    println(name)
    //println(Json.prettyPrint(conf \\ key))
   // val r = (conf \\ key) //.map(_.as [String])
   // val readableString: String = Json.prettyPrint(r)
   // println(readableString)
   ""
  }
  def getIndex(key : String, idx : Integer = 0) : String = {
    val name = (conf \ "endpoints").asOpt[Array[String]] match {
      case None => return ""//Or handle the lack of a value another way: throw an error, etc.
      case Some(s: Array[String]) => return s(idx) //return the string to set your value
    }

    println("results")
    println(name)
    //println(Json.prettyPrint(conf \\ key))
   // val r = (conf \\ key) //.map(_.as [String])
   // val readableString: String = Json.prettyPrint(r)
   // println(readableString)
   ""
  }
}

class SW( var config: SWConfig = null ) {
  
  /* root node */
  private var rootNode   : Root = new Root()
  /* focus node */
  private var focusNode  : Node = rootNode

  def print() : Unit = {
    println(" - SW -");
    println(" -- root --");
    pprint.pprintln(rootNode.children)
    println(" -- focusNode --");
    pprint.pprintln(focusNode.children)
  }

  /* manage the creation of an unique ref */
  def getUniqueRef() : String = randomUUID.toString

  /* set the current focus on the select node */
  def focus(ref : String) : SW = {
    var sn = new SelectNode();
    focusNode = sn.setFocus(ref, rootNode)(0)
   
    return this
  }

  def focusManagement(n : Node) : SW = {
    focusNode.addChildren(n) 
    focusNode = n 
    return this
  }

  /* start a request */
  def something( ref : String = getUniqueRef() ) : SW = {
    val lastNode = new Something(ref)
    /* special case when "somthing" is used. become the focus */
    focusManagement(lastNode) 
  }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  def isSubjectOf( uri : URI , ref : String = getUniqueRef() ) : SW = {
    val lastNode = new SubjectOf(ref,uri)
    focusManagement(lastNode)
  }


  /* create node which focus is the subject : ?focusId <uri> ?target */
  def isObjectOf( uri : URI , ref : String = getUniqueRef() ) : SW = {
    val lastNode = new ObjectOf(ref,uri)
    focusManagement(lastNode)
  }

  /* set */
  def set( uri : URI ) : SW = {
    val lastNode = new Value(uri)
    focusManagement(lastNode)
  }

  def debug() : SW = {
    var sc = new SimpleConsole();
    //println( pprint.tokenize(rootNode).mkString )
    //pprint.pprintln(rootNode.children)
    println("--focus--")
    pprint.pprintln(focusNode)
    pprint.pprintln(focusNode.children)
    //rootNode.accept(sc)
    println(sc.get(rootNode))
    return this
  }

  def sparql() : String = {
    var sg = new SparqlGenerator();
    return sg.body(config, rootNode)
  }

  def select() : Option[String] = {
    val sg = new SparqlGenerator()
    val query = sg.prolog(config, rootNode ) + sg.body(config, rootNode ) +sg.solutionModifier(config, rootNode)
    println(" ------------------------------- SPARQL ----------------------------- ")
    println(query)
    println(" ------------------------------- RESULT ----------------------------- ")
    
    
    val dbpediaRunner = QueryRunner("http://dbpedia.org/sparql")
    val result = dbpediaRunner
      .query(query)
    result.asJson()
    None : Option[String]
  }

}
