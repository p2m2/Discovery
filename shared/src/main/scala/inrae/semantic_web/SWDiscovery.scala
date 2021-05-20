package inrae.semantic_web

import inrae.semantic_web
import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent}
import inrae.semantic_web.internal._
import inrae.semantic_web.internal.pm.SelectNode
import inrae.semantic_web.rdf._
import inrae.semantic_web.sparql.QueryResult
import inrae.semantic_web.strategy.StrategyRequestBuilder
import wvlet.log.Logger
import wvlet.log.Logger.rootLogger._

import java.util.UUID.randomUUID
import scala.concurrent.Future
import upickle.default.{macroRW, read, write, ReadWriter => RW}

final case class SWDiscoveryException(private val message: String = "",
                                      private val cause: Throwable = None.orNull) extends Exception(message,cause)

object SWDiscovery {

  private val version : String = SWDiscoveryVersionAtBuildTime.version

  implicit val rw: RW[SWDiscovery] = macroRW

  info(" --------------------------------------------------" )
  info(" ---- Discovery :"+ SWDiscovery.version + "         -----------" )
  info(" --------------------------------------------------" )

}

case class SWDiscovery(
                        config: StatementConfiguration=StatementConfiguration(),
                        rootNode : Root = Root(),
                        fn : Option[String] = None)
{
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val focusNode : String = fn match {
    case Some(v) => v
    case None => rootNode.reference()
  }

  case class FilterIncrement(negation : Boolean = false) {

    def manageFilter(n:Node,forward : Boolean = false) : SWDiscovery = focusManagement(n,forward)

    def isLiteral : SWDiscovery = manageFilter(inrae.semantic_web.internal.isLiteral(this.negation,getUniqueRef()))
    def isUri : SWDiscovery = manageFilter(inrae.semantic_web.internal.isURI(this.negation,getUniqueRef()))
    def isBlank : SWDiscovery = manageFilter(inrae.semantic_web.internal.isBlank(this.negation,getUniqueRef()))

    /* strings */
    def regex( pattern : SparqlDefinition, flags : SparqlDefinition="" ) : SWDiscovery =
      manageFilter(Regex(pattern,flags,this.negation,getUniqueRef()))
    def contains( string : SparqlDefinition ) : SWDiscovery = manageFilter(Contains(string,this.negation,getUniqueRef()))
    def strStarts( string : SparqlDefinition ) : SWDiscovery = manageFilter(StrStarts(string,this.negation,getUniqueRef()))
    def strEnds( string : SparqlDefinition ) : SWDiscovery = manageFilter(StrEnds(string,this.negation,getUniqueRef()))

    /* numeric */
    def equal( value : SparqlDefinition ) : SWDiscovery = manageFilter(Equal(value,this.negation,getUniqueRef()))
    def notEqual( value : SparqlDefinition ) : SWDiscovery = manageFilter(NotEqual(value,this.negation,getUniqueRef()))
    def inf( value : SparqlDefinition ) : SWDiscovery = manageFilter(Inf(value,this.negation,getUniqueRef()))
    def infEqual( value : SparqlDefinition ) : SWDiscovery = manageFilter(InfEqual(value,this.negation,getUniqueRef()))
    def sup( value : SparqlDefinition ) : SWDiscovery = manageFilter(Sup(value,this.negation,getUniqueRef()))
    def supEqual( value : SparqlDefinition ) : SWDiscovery = manageFilter(SupEqual(value,this.negation,getUniqueRef()))

    def not : FilterIncrement = { FilterIncrement(true) }
  }

  def filter : FilterIncrement = FilterIncrement()

  case class BindIncrement(`var` : String) {
    def manage(n:ExpressionNode,forward : Boolean = true) : SWDiscovery =
      focusManagement(Bind(n,`var`),forward).root.something(`var`).focus(`var`)
    /* primary expression */

    /* String fun */
    def subStr(startingLoc : SparqlDefinition,length : SparqlDefinition ) : SWDiscovery = manage(SubStr(startingLoc,length,getUniqueRef()))
    def replace(pattern : SparqlDefinition, replacement : SparqlDefinition, flags : SparqlDefinition="") : SWDiscovery =
      manage(Replace(pattern,replacement,flags,getUniqueRef()))

    /* Numeric  fun */
    def abs() : SWDiscovery = manage(Abs(getUniqueRef()))
    def round() : SWDiscovery = manage(Round(getUniqueRef()))
    def ceil() : SWDiscovery = manage(Ceil(getUniqueRef()))
    def floor() : SWDiscovery = manage(Floor(getUniqueRef()))
    def rand() : SWDiscovery = manage(Rand(getUniqueRef()))
  }

  def bind(`var` : String) : BindIncrement = BindIncrement(`var`)

  //private val logger = Logger.of[SWDiscovery]
  // Set the root logger's log level
  Logger.setDefaultLogLevel(config.conf.settings.getLogLevel)

  def usage : SWDiscovery = {
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
    SWDiscovery(config,rootNode,Some(focusNode))
  }

  /* set focus on root */
  def root: SWDiscovery  = SWDiscovery(config,rootNode,Some(rootNode.reference()))

  def finder : SWDiscoveryHelper = SWDiscoveryHelper(this)

  /* get current focus */
  def focus() : String = focusNode

  /* set the current focus on the select node */
  def focus(ref : String) : SWDiscovery = {
    trace("focus")
    pm.SelectNode.getNodeWithRef(ref, rootNode).lastOption match {
      case Some(node) => SWDiscovery(config,rootNode,Some(node.reference()))
      case None => throw SWDiscoveryException(s"$ref does not exist.")
    }
  }

  def refExist(ref:String) : SWDiscovery = {

    pm.SelectNode.getNodeWithRef(ref, rootNode).lastOption match {
      case Some(_) => SWDiscovery(config,rootNode,Some(focusNode))
      case None => throw SWDiscoveryException(s"$ref does not exist.")
    }
  }

  def prefix(short : String, long : IRI ) : SWDiscovery = SWDiscovery(config,rootNode.addPrefix(short , long ),Some(focusNode))


  def graph(graph : IRI) : SWDiscovery = SWDiscovery(config,rootNode.addDefaultGraph(graph),Some(focusNode))


  def namedGraph(graph : IRI ) : SWDiscovery = SWDiscovery(config,rootNode.addNamedGraph(graph),Some(focusNode))

  def checkQueryVariable(term : SparqlDefinition): SWDiscovery = {
    /* Check if QueryVariable is referenced with Element.
     *  add a Something element otherwise */
    term match {
        case qv : QueryVariable if SelectNode.getNodeWithRef(qv.name,rootNode).length == 0  =>
          SWDiscovery(config,rootNode.addChildren(rootNode.reference(),Something(qv.name)),Some(focusNode))
        case _ => this
      }
  }

  def focusManagement(n : Node, forward: Boolean = true) : SWDiscovery = {
    // get all node
    val current = rootNode.getChild[Node](rootNode.asInstanceOf[Node]).filter( _.idRef == focusNode )

    if ( current.lastOption.map( _.accept(n) ).getOrElse(false)) {
      val newRootNode = rootNode.addChildren(focusNode,n)
      /* current node is the focusNode */
      if (forward) {
        SWDiscovery(config,newRootNode,Some(n.reference()))
      }  else {
        SWDiscovery(config,newRootNode,Some(focusNode))
      }
    } else {
        throw SWDiscoveryException(s"Can not add this node [$n]at the current focus[$current]")
      }
  }

  def getUniqueRef(baseNameVar : String=""): String = {
    baseNameVar + (baseNameVar match {
      case "object" => rootNode.getChild(SubjectOf("",URI(""))).length
      case "subject" => rootNode.getChild(ObjectOf("",URI(""))).length
      case "something" => rootNode.getChild(Something("")).length
      case "linkTo" => rootNode.getChild(LinkTo("",URI(""))).length
      case "linkFrom" => rootNode.getChild(LinkFrom("",URI(""))).length
      case "datatype" => rootNode.getChild(DatatypeNode("",SubjectOf("",URI("")),"")).length
      case _ => randomUUID.toString
    }).toString
  }

  /* start a request with a variable */
  def something( ref : String = getUniqueRef("something") ) : SWDiscovery = {
    debug(" -- something -- ")
    focusManagement(Something(ref))
  }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  def isSubjectOf( term : SparqlDefinition , ref : String = getUniqueRef("object")  ) : SWDiscovery =
    checkQueryVariable(term).focusManagement(SubjectOf(ref,term))


  /* create node which focus is the subject : ?target <uri> ?focusId */
  def isObjectOf( term : SparqlDefinition , ref : String = getUniqueRef("subject")  ) : SWDiscovery =
    checkQueryVariable(term).focusManagement(ObjectOf(ref,term))

  /* create node which focus is the properties :
  ?focusId ?target <uri>|literal
  */
  def isLinkTo(term : SparqlDefinition, ref : String = getUniqueRef("linkTo") ) : SWDiscovery =
    checkQueryVariable(term).focusManagement(LinkTo(ref,term))


  /* create node which focus is typed with <uri>:
  ?focusId a <uri>
  */
  def isA( term : SparqlDefinition  ) : SWDiscovery =
    checkQueryVariable(term)
    .isSubjectOf(URI("a"))
    .set(term)
    .focus(focusNode)

  /* create node which focus is the properties :
     <uri> ?target ?focusId
  */
  def isLinkFrom( term : SparqlDefinition, ref : String = getUniqueRef("linkFrom")  ) : SWDiscovery =
    checkQueryVariable(term).focusManagement(LinkFrom(ref,term))

  /*
  Get attribute value of an object.
  return Sw with the old focus
  Attribute value is optional
  */

  def datatype( uri : URI, ref : String = getUniqueRef("datatype") ) : SWDiscovery =
    SWDiscovery(
      config,
      root.focusManagement(DatatypeNode(focusNode,SubjectOf(ref,uri),ref), false).rootNode,
      Some(focusNode))


  def set( term : SparqlDefinition ) : SWDiscovery =
    checkQueryVariable(term).focusManagement(Value(term),forward = false)

  def setList( terms : Seq[SparqlDefinition] ) : SWDiscovery = focusManagement(ListValues(terms),forward = false)

//https://fr.wikipedia.org/wiki/Encodage-pourcent

  def getSerializedString : String =
    write(this)
   .replace("%","%25")
      .replace("!","%21")
      .replace("#","%23")
      .replace("$","%24")
      .replace("&","%26")
      .replace("\'","%27")
      .replace("(","%28")
      .replace(")","%29")
      .replace("*","%2A")
      .replace("+","%2B")
      .replace(",","%2C")
      .replace("/","%2F")
      .replace( ":","%3A")
      .replace( ";","%3B")
      .replace( "=","%3D")
      .replace( "?","%3F")
      .replace( "@","%40")
      .replace("[","%5B")
      .replace("]","%5D")
      .replace (" ","%0A")
      .replace ("{","%7B")
      .replace("}","%7D")
      .replace("\"","%22")
      .replace("-","%2D")
      .replace(".","%2E")
      .replace("<","%3C")
      .replace(">","%3E")
      .replace("\\","%5C")
      .replace("^","%5E")
      .replace("_","%5F")
      .replace("`","%60")
      .replace("|","%7C")
      .replace("~","%7E")



  def setSerializedString(query : String) : SWDiscovery = read[SWDiscovery](
    query
      .replace("%21","!")
      .replace("%23","#")
      .replace("%24","$")
      .replace("%26","&")
      .replace("%27","\'")
      .replace("%28","(")
      .replace("%29",")")
      .replace("%2A","*")
      .replace("%2B","+")
      .replace("%2C",",")
      .replace("%2F","/")
      .replace( "%3A",":")
      .replace( "%3B",";")
      .replace( "%3D","=")
      .replace( "%3F","?")
      .replace( "%40","@")
      .replace("%5B","[")
      .replace("%5D","]")
      .replace ("%0A"," ")
      .replace ("%7B","{")
      .replace("%7D","}")
      .replace("%22","\"")
      .replace("%2D","-")
      .replace("%2E",".")
      .replace("%3C","<")
      .replace("%3E",">")
      .replace("%5C","\\")
      .replace("%5E","^")
      .replace("%5F","_")
      .replace("%60","`")
      .replace("%7C","|")
      .replace("%7E","~")
      .replace("%25","%")
  )


  def console : SWDiscovery = {
    debug(" -- console -- ")
    println("USER REQUEST\n" +
      pm.SimpleConsole().get(rootNode) + "\n" +
      "FOCUS NODE:"+ focusNode +
      "\nENDPOINT:"+config.sources().map(v => println(v.url)).mkString(",") +"\n\n" +
      "\n -- SPARQL Request -- \n\n" +
      sparql)
      //"QUERY PLANNER\n"+
      //"todo....")
    this
  }

  def sparql : String = SparqlQueryBuilder.selectQueryString(rootNode)

  /**
   * Discovery request
   *
   */
  def transaction = SWTransaction(this)
  /**
   * Return solutions as Future corresponding with the current Node request.
   * @param lRef : selected variables
   * @param limit : upper bound on the number of solutions returned
   * @param offset : solution are generated after this offset
   * @return
   */
  def select(lRef: Seq[String] = List(), limit : Int = 0, offset : Int = 0) : SWTransaction =
        transaction
        .limit(limit)
        .offset(offset)
        .projection(lRef)

  /**
   * Give an iterable object to browse and obtain all solution performed by a select.
   * @param lRef : selected variables
   * @return iterable on select function
   */
  def selectByPage(lRef: Seq[String] = List())  : Future[(Int,Seq[SWTransaction])] = {
    SWDiscoveryHelper(this).count.map(
      nSolutions => {
        val nit : Int = nSolutions / config.conf.settings.pageSize
        (nit+1,(0 to nit).map( p =>{
          val limit = config.conf.settings.pageSize
          val offset = p*config.conf.settings.pageSize
          select(lRef,limit,offset)
        }))
      })
  }
}
