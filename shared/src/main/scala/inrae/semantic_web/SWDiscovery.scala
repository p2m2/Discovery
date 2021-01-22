package inrae.semantic_web

import inrae.semantic_web.internal._
import inrae.semantic_web.internal.pm.SelectNode
import inrae.semantic_web.rdf._
import wvlet.log.Logger
import wvlet.log.Logger.rootLogger._

import java.util.UUID.randomUUID
import scala.concurrent.Future
import scala.util.Success

final case class DiscoveryException(private val message: String = "",
                                    private val cause: Throwable = None.orNull) extends Exception(message,cause)

object SWDiscovery {

  private val version : String = SWDiscoveryVersionAtBuildTime.version

  info(" --------------------------------------------------" )
  info(" ---- Discovery :"+ SWDiscovery.version + "         -----------" )
  info(" --------------------------------------------------" )
}

case class SWDiscovery(config: StatementConfiguration,rootNode : Root = Root(), focusNode  : Node  ) {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  this.prefix("owl",IRI("http://www.w3.org/2002/07/owl#"))
  this.prefix("rdf",IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#"))
  this.prefix("rdfs",IRI("http://www.w3.org/2000/01/rdf-schema#"))
  this.prefix("xsd",IRI("http://www.w3.org/2001/XMLSchema#"))

  class FilterIncrement() {
    var negation = false

    def manageFilter(n:Node,forward : Boolean = false) : SWDiscovery = {
      val sw = focusManagement(n,forward)
      this.negation = !this.negation
      sw
    }

    def isLiteral : SWDiscovery = manageFilter(inrae.semantic_web.internal.isLiteral(this.negation),false)
    def isUri : SWDiscovery = manageFilter(inrae.semantic_web.internal.isURI(this.negation),false)
    def isBlank : SWDiscovery = manageFilter(inrae.semantic_web.internal.isBlank(this.negation),false)

    /* strings */
    def contains( string : String ) : SWDiscovery = manageFilter(Contains(string,this.negation),false)
    def strStarts( string : String ) : SWDiscovery = manageFilter(StrStarts(string,this.negation),false)
    def strEnds( string : String ) : SWDiscovery = manageFilter(StrEnds(string,this.negation),false)

    /* numeric */
    def equal( value : Literal ) : SWDiscovery = manageFilter(Equal(value,this.negation),false)
    def notEqual( value : Literal ) : SWDiscovery = manageFilter(NotEqual(value,this.negation),false)
    def inf( value : Literal ) : SWDiscovery = manageFilter(Inf(value,this.negation),false)
    def infEqual( value : Literal ) : SWDiscovery = manageFilter(InfEqual(value,this.negation),false)
    def sup( value : Literal ) : SWDiscovery = manageFilter(Sup(value,this.negation),false)
    def supEqual( value : Literal ) : SWDiscovery = manageFilter(SupEqual(value,this.negation),false)

    def not : FilterIncrement = { this.negation = !this.negation ; this }
  }

  def filter : FilterIncrement = new FilterIncrement()

  private val logger = Logger.of[SWDiscovery]
  // Set the root logger's log level
  Logger.setDefaultLogLevel(config.conf.settings.getLogLevel())

  def help() : SWDiscovery = {
    println(" ---------------- SWDiscovery "+SWDiscovery.version+" ---------------------------")
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
  def root(): SWDiscovery  = {
    focusNode = rootNode
    this
  }

  /* set the current focus on the select node */
  def focus(ref : String) : SWDiscovery = {
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

  def prefix(short : String, long : IRI ) : SWDiscovery = {
    rootNode.prefixes = rootNode.prefixes + ( short -> long )
    this
  }

  def graph(graph : IRI) : SWDiscovery = {
    rootNode.defaultGraph = rootNode.defaultGraph :+ graph
    this
  }

  def namedGraph(graph : IRI ) : SWDiscovery = {
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

  def setupnode(n : Node, upsource : Boolean = false, forward : Boolean = true ) : SWDiscovery = {
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

  def focusManagement(n : Node, forward: Boolean = true) : SWDiscovery = {
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
  def something( ref : String = getUniqueRef() ) : SWDiscovery = {
    debug(" -- something -- ")
    setupnode(Something(ref))
  }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  def isSubjectOf( term : SparqlDefinition , ref : String = getUniqueRef() ) : SWDiscovery = {
    debug(" -- isSubjectOf -- ")
    checkQueryVariable(term)
    setupnode(SubjectOf(ref,term))
  }


  /* create node which focus is the subject : ?target <uri> ?focusId */
  def isObjectOf( term : SparqlDefinition , ref : String = getUniqueRef() ) : SWDiscovery = {
    debug(" -- isObjectOf -- ")
    checkQueryVariable(term)
    setupnode(ObjectOf(ref,term))
  }

  /* create node which focus is the properties :
  ?focusId ?target <uri>|literal
  */
  def isLinkTo(term : SparqlDefinition, ref : String = getUniqueRef() ) : SWDiscovery = {
    debug(" -- isLinkTo -- ")
    checkQueryVariable(term)
    setupnode(LinkTo(ref,term))
  }


  /* create node which focus is typed with <uri>:
  ?focusId a <uri>
  */
  def isA( term : SparqlDefinition  ) : SWDiscovery = {
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
  def isLinkFrom( term : SparqlDefinition, ref : String = getUniqueRef() ) : SWDiscovery = {
    debug(" -- isLinkFrom -- ")
    checkQueryVariable(term)
    setupnode(LinkFrom(ref,term))
  }

  /*
  Get attribute value of an object.
  return Sw with the old focus
  Attribute value is optional
  */

  def datatype( uri : URI, ref : String ) : SWDiscovery = {
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
  def set( term : SparqlDefinition ) : SWDiscovery = {
    debug(" -- set -- ")
    checkQueryVariable(term)
    setupnode(Value(term),true,false)
  }

  def setList( uris : Seq[URI] ) : SWDiscovery = {
    debug(" -- setList -- ")
    setupnode(ListValues(uris),true,false)
  }


  def console() : SWDiscovery = {
    debug(" -- console -- ")
    println("USER REQUEST\n" +
      pm.SimpleConsole.get(rootNode) +
      pm.SimpleConsole.get(focusNode) +
      "\nENDPOINT:"+config.sources().map(v => println(v.url)).mkString(",") +"\n\n" +
      "\n -- SPARQL Request -- \n\n" +
      sparql())
      //"QUERY PLANNER\n"+
      //"todo....")
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
  def selectByPage(lRef: Seq[String] = List())  : Future[(Int,Seq[SWTransaction])] = {
    count().map(
      nsolutions => {
        val nit : Int = nsolutions / config.conf.settings.pageSize
        (nit+1,(0 to nit).map( p =>{
          val limit = config.conf.settings.pageSize
          val offset = p*config.conf.settings.pageSize
          select(lRef,limit,offset)
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
