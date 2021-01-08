package inrae.semantic_web

import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent, Publisher, Subscriber}

import java.util.UUID.randomUUID
import inrae.semantic_web.internal._
import inrae.semantic_web.internal.pm.SelectNode
import inrae.semantic_web.rdf._
import wvlet.log.Logger
import wvlet.log.Logger.rootLogger._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

final case class DiscoveryException(private val message: String = "",
                                    private val cause: Throwable = None.orNull) extends Exception(message,cause)

object SW {

  private val version : String = "0.0.2"

  info(" --------------------------------------------------" )
  info(" ---- version Discovery :"+ version + "          -----------" )
  info(" --------------------------------------------------" )
}

case class SW(var config: StatementConfiguration) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  /* root node */
  val rootNode   : Root = Root()
  /* focus node */
  var focusNode  : Node = rootNode

  this.prefix("owl",IRI("http://www.w3.org/2002/07/owl#"))
  this.prefix("rdf",IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#"))
  this.prefix("rdfs",IRI("http://www.w3.org/2000/01/rdf-schema#"))
  this.prefix("xsd",IRI("http://www.w3.org/2001/XMLSchema#"))

  class FilterIncrement() {
    var negation = false

    def manageFilter(n:Node,forward : Boolean = false) : SW = {
      val sw = focusManagement(n,forward)
      this.negation = !this.negation
      sw
    }

    def isLiteral : SW = manageFilter(inrae.semantic_web.internal.isLiteral(this.negation),false)
    def isUri : SW = manageFilter(inrae.semantic_web.internal.isURI(this.negation),false)
    def isBlank : SW = manageFilter(inrae.semantic_web.internal.isBlank(this.negation),false)

    /* strings */
    def contains( string : String ) : SW = manageFilter(Contains(string,this.negation),false)
    def strStarts( string : String ) : SW = manageFilter(StrStarts(string,this.negation),false)
    def strEnds( string : String ) : SW = manageFilter(StrEnds(string,this.negation),false)

    /* numeric */
    def equal( value : Literal ) : SW = manageFilter(Equal(value,this.negation),false)
    def notEqual( value : Literal ) : SW = manageFilter(NotEqual(value,this.negation),false)
    def inf( value : Literal ) : SW = manageFilter(Inf(value,this.negation),false)
    def infEqual( value : Literal ) : SW = manageFilter(InfEqual(value,this.negation),false)
    def sup( value : Literal ) : SW = manageFilter(Sup(value,this.negation),false)
    def supEqual( value : Literal ) : SW = manageFilter(SupEqual(value,this.negation),false)

    def not : FilterIncrement = { this.negation = !this.negation ; this }
  }

  val filter : FilterIncrement = new FilterIncrement()

  private val logger = Logger.of[SW]
  // Set the root logger's log level
  Logger.setDefaultLogLevel(config.conf.settings.getLogLevel())

  def help() : SW = {
    println(" ---------------- SW "+SW.version+" ---------------------------")
    println("   ")
    println("    -------------  Query Control ----------")
    println(" something:")
    println(" focus    :")
    println("   ")
    println("    -------------  Add Sparql snippet ----------")
    println(" isSubjectOf(URI(\"http://relation\")):  ?currentFocus URI(\"http://relation\") ?newFocus")
    println(" isObjectOf(URI(\"http://relation\")):  ?newFocus URI(\"http://relation\") ?currentFocus")
    println(" isLinkTo(URI(\"http://object\")):  ?currentFocus ?newFocus URI(\"http://object\")")
    println(" isLinkTo(XSD(\"type\",\"value\")):  ?currentFocus ?newFocus XSD(\"type\",\"value\")")
    println(" isLinkFrom(URI(\"http://object\")):  URI(\"http://object\") ?newFocus ?currentFocus")
    println(" isA ")
    println(" set ")
    println("   ")
    println("    -------------  Print information ----------")
    println(" debug:")
    println(" sparql_console:")
    println("   ")
    println("    -------------  Request ----------")
    println(" select:")
    println(" count:")
    println("   ")
    println("    -------------  Explore according the focus ----------")
    println(" findClassesOf:")
    println(" findObjectPropertiesOf:")
    println(" findDatatypePropertiesOf:")
    println("   ")
    println("  --------------------------------------------------------------" )
    this
  }


  /* manage the creation of an unique ref */
  def getUniqueRef() : String = "_internal_" + randomUUID.toString

  /* set focus on root */
  def root(): SW  = {
    focusNode = rootNode
    this
  }

  /* set the current focus on the select node */
  def focus(ref : String) : SW = {
    trace("focus")
    if (ref == "") throw new Error("reference can not be empty !")
    val arrNode = pm.SelectNode.getNodeWithRef(ref, rootNode)
    if ( arrNode.length > 0 ) {
      focusNode = arrNode(0)
    } else {
      throw new Error("ref unknown :"+ref)
    }
    this
  }

  /* get ref of the current focus */
  def ref(): String = {
    pm.SelectNode.getNodeRef(rootNode,focusNode)
  }

  def prefix(short : String, long : IRI ) : SW = {
    rootNode.prefixes = rootNode.prefixes + ( short -> long )
    this
  }

  def graph(graph : IRI) : SW = {
    rootNode.defaultGraph = rootNode.defaultGraph :+ graph
    this
  }

  def namedGraph(graph : IRI ) : SW = {
    rootNode.namedGraph = rootNode.namedGraph :+ graph
    this
  }

  def checkQueryVariable(term : SparqlDefinition) = {
    /* Check if QueryVariable is referenced with Element.
     *  add a Something element otherwise */
    term match {
        case qv : QueryVariable => if (SelectNode.getNodeWithRef(qv.name,rootNode).length == 0) {
          rootNode.addChildren(Something(qv.name))
        }
        case _ => None
      }
  }

  def setupnode(n : Node, upsource : Boolean = false, forward : Boolean = true ) : SW = {
    trace("setupnode")

    focusManagement(n,forward)

    if ( upsource ) {
      QueryManager(config).setUpSourcesNode(n,rootNode.prefixes).onComplete {
        case Success(Some(sn)) => {
          rootNode.lSourcesNodes = rootNode.lSourcesNodes :+ sn
        }
        case _  => None
      }
    }
    this
  }

  def focusManagement(n : Node, forward: Boolean = true) : SW = {
    trace("-- focusManagement --")
    if (! focusNode.accept(n)) {
      throw new Error("Can not add "+n.toString()+" with the current focus ["+focusNode.toString()+"]")
    }

    focusNode.addChildren(n)
    /* current node is the focusNode */
    if (forward) focusNode = n
    this
  }

  /* start a request with a variable */
  def something( ref : String = getUniqueRef() ) : SW = {
    debug(" -- something -- ")
    setupnode(Something(ref))
  }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  def isSubjectOf( term : SparqlDefinition , ref : String = getUniqueRef() ) : SW = {
    debug(" -- isSubjectOf -- ")
    checkQueryVariable(term)
    setupnode(SubjectOf(ref,term))
  }


  /* create node which focus is the subject : ?target <uri> ?focusId */
  def isObjectOf( term : SparqlDefinition , ref : String = getUniqueRef() ) : SW = {
    debug(" -- isObjectOf -- ")
    checkQueryVariable(term)
    setupnode(ObjectOf(ref,term))
  }

  /* create node which focus is the properties :
  ?focusId ?target <uri>|literal
  */
  def isLinkTo(term : SparqlDefinition, ref : String = getUniqueRef() ) : SW = {
    debug(" -- isLinkTo -- ")
    checkQueryVariable(term)
    setupnode(LinkTo(ref,term))
  }


  /* create node which focus is typed with <uri>:
  ?focusId a <uri>
  */
  def isA( term : SparqlDefinition  ) : SW = {
    debug(" -- isA -- ")
    checkQueryVariable(term)
    val f = focusNode
    isSubjectOf(URI("a")).set(term)
    focusNode = f
    this
  }

  /* create node which focus is the properties :
     <uri> ?target ?focusId
  */
  def isLinkFrom( term : SparqlDefinition, ref : String = getUniqueRef() ) : SW = {
    debug(" -- isLinkFrom -- ")
    checkQueryVariable(term)
    setupnode(LinkFrom(ref,term))
  }

  /*
  Get attribute value of an object.
  return Sw with the old focus
  Attribute value is optional
  */

  def datatype( uri : URI, ref : String ) : SW = {
    debug(" -- datatype -- ")
    val f = focusNode

    focusNode match {
      case n : RdfNode => {
        rootNode.lDatatypeNode = rootNode.lDatatypeNode :+ (DatatypeNode(n.reference(),SubjectOf(ref,uri)))
      }
      case _ => throw new Error("Can not add datatype property with "+focusNode.getClass.toString)
    }

    focusNode = f
    this
  }

  /*
    Specific treatment : add value possibilities for a specific node
    We get the
  */
  def set( term : SparqlDefinition ) : SW = {
    debug(" -- set -- ")
    checkQueryVariable(term)
    setupnode(Value(term),true,false)
  }

  def setList( uris : Seq[URI] ) : SW = {
    debug(" -- setList -- ")
    setupnode(ListValues(uris),true,false)
  }


  def console() : SW = {
    debug(" -- console -- ")
    println("USER REQUEST\n" +
      pm.SimpleConsole.get(rootNode) +
      pm.SimpleConsole.get(focusNode) +
      "QUERY PLANNER\n"+
      "todo....")
    this
  }

  def sparql() : String = {
    debug(" -- sparql -- ")
    QueryManager(config).sparql_string(rootNode)
  }

  /**
   * Discovery request
   *
   */


  /**
   * Return solutions as Future corresponding with the current Node request.
   * @param lRef : selected variables
   * @param limit : upper bound on the number of solutions returned
   * @param offset : solution are generated after this offset
   * @return
   */
  def select(lRef: Seq[String] = List(), limit : Int = 0, offset : Int = 0) : SWTransaction = {
    debug(" -- select -- ")
    SWTransaction(this,lRef,limit,offset)
  }

  def count() : Future[Int] = {
    debug(" -- count -- ")
    val qm =QueryManager(config)
    qm.countNbSolutions(rootNode)
  }

  /**
   * Give an iterable object to browse and obtain all solution performed by a select.
   * @param lRef : selected variables
   * @return iterable on select function
   */
  def selectByPage(lRef: Seq[String] = List())  : Future[(Int,Seq[LazyFutureSwResults])] = {
    count().map(
      nsolutions => {
        val nit : Int = nsolutions / config.conf.settings.pageSize
        (nit+1,(0 to nit).map( p =>{
          val limit = config.conf.settings.pageSize
          val offset = p*config.conf.settings.pageSize
          LazyFutureSwResults( () => select(lRef,limit,offset) )
        }))
      })
  }

  /**
   * Discovery functionalities
   *
   */

  def findClasses(motherClass: URI = URI("") ) : Future[Seq[URI]] = {
    debug(" -- findClasses -- ")
    (motherClass match {
      case uri : URI if uri == URI("")  => isSubjectOf(URI("a"),"_esp___type")
      case _ : URI =>  isSubjectOf(URI("a"),"_esp___type")
                  .isSubjectOf(URI("a"))
                  .set(motherClass)
    })
      .focus("_esp___type")
      .select(List("_esp___type"))
      .commit()
      .raw
      .map( json => {
        json("results")("bindings").arr.map(
          row => SparqlBuilder.createUri(row("_esp___type"))
        ).toSeq
      })
  }

  def findProperties(motherClassProperties: URI = URI("") , kind : String = "objectProperty" ) : Future[Seq[URI]] = {
    debug(" -- findProperties -- ")
    val refCurrent = ref()

    var state = root()
      .something("_esp___type")
      .focus(refCurrent)
      .isLinkTo(QueryVariable("_esp___type"),"_esp___property")

    /* inherited from something ??? */
    if (motherClassProperties != URI("")) {
      state = state.isSubjectOf(URI("a"))
          .set(motherClassProperties)
    }

    /* object or datatype properties owl def. */
    ( kind  match {
      case "objectProperty" => state.focus("_esp___type").filter.isUri
      case "datatypeProperty" => state.focus("_esp___type").filter.isLiteral
      case _ => state
    }).select(List("_esp___property"))
      .commit()
      .raw
      .map( json => {
        json("results")("bindings").arr.map(
          row => {
            SparqlBuilder.createUri(row("_esp___property")) }
        ).toSeq
      })
  }

  def findObjectProperties(motherClassProperties: URI = URI("") ) : Future[Seq[URI]] = {
    debug(" -- findObjectProperties -- ")
    findProperties(motherClassProperties,"objectProperty")
  }
  def findDatatypeProperties(motherClassProperties: URI = URI("") ) : Future[Seq[URI]] = {
    debug(" -- findDatatypeProperties -- ")
    findProperties(motherClassProperties,"datatypeProperty")
  }
}
