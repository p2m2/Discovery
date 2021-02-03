package inrae.semantic_web

import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent}
import inrae.semantic_web.internal._
import inrae.semantic_web.internal.pm.{SelectNode, SerializationBuilder}
import inrae.semantic_web.rdf._
import inrae.semantic_web.sparql.QueryResult
import inrae.semantic_web.strategy.StrategyRequestBuilder
import wvlet.log.Logger
import wvlet.log.Logger.rootLogger._

import java.util.UUID.randomUUID
import scala.concurrent.Future
import upickle.default.{macroRW, ReadWriter => RW}

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

    def isLiteral : SWDiscovery = manageFilter(inrae.semantic_web.internal.isLiteral(this.negation))
    def isUri : SWDiscovery = manageFilter(inrae.semantic_web.internal.isURI(this.negation))
    def isBlank : SWDiscovery = manageFilter(inrae.semantic_web.internal.isBlank(this.negation))

    /* strings */
    def contains( string : String ) : SWDiscovery = manageFilter(Contains(string,this.negation))
    def strStarts( string : String ) : SWDiscovery = manageFilter(StrStarts(string,this.negation))
    def strEnds( string : String ) : SWDiscovery = manageFilter(StrEnds(string,this.negation))

    /* numeric */
    def equal( value : Literal ) : SWDiscovery = manageFilter(Equal(value,this.negation))
    def notEqual( value : Literal ) : SWDiscovery = manageFilter(NotEqual(value,this.negation))
    def inf( value : Literal ) : SWDiscovery = manageFilter(Inf(value,this.negation))
    def infEqual( value : Literal ) : SWDiscovery = manageFilter(InfEqual(value,this.negation))
    def sup( value : Literal ) : SWDiscovery = manageFilter(Sup(value,this.negation))
    def supEqual( value : Literal ) : SWDiscovery = manageFilter(SupEqual(value,this.negation))

    def not : FilterIncrement = { FilterIncrement(true) }
  }

  def filter : FilterIncrement = FilterIncrement()

  case class BindIncrement() {

    def manage(n:ExpressionNode,forward : Boolean = false) : SWDiscovery =
      focusManagement(
        Bind(n,getUniqueRef()),false)

    def subStr(startingLoc : Int,length : Int ) : SWDiscovery = manage(SubStr(startingLoc,length,getUniqueRef()))

  }

  def bind : BindIncrement = BindIncrement()

  case class ProjectionExpressionIncrement(v : String) {

    def manage(n:AggregateNode,forward : Boolean = false) : SWDiscovery =
      focusManagement(
        ProjectionExpression(QueryVariable(v),n,getUniqueRef()),false)

    def count(distinct: Boolean=false) : SWDiscovery = manage(Count(distinct,getUniqueRef()))
    def countAll(distinct: Boolean=false) : SWDiscovery = manage(CountAll(distinct,getUniqueRef()),true)
  }

  def agg_projection(`var` : String) : ProjectionExpressionIncrement = ProjectionExpressionIncrement(`var`)

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
  def root(): SWDiscovery  = SWDiscovery(config,rootNode,Some(rootNode.reference()))

  def helper : SWDiscoveryHelper = SWDiscoveryHelper(this)

  /* set the current focus on the select node */
  def focus(ref : String) : SWDiscovery = {
    trace("focus")
    if (ref == "") throw new Error("reference can not be empty !")
    val arrNode = pm.SelectNode.getNodeWithRef(ref, rootNode)
    if ( arrNode.length > 0 ) {
      SWDiscovery(config,rootNode,Some(arrNode(0).reference()))
    } else {
      throw new Error("ref unknown :"+ref)
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
        throw SWDiscoveryException("Can not add this node at the current focus["+focusNode+"]")
      }
  }

  def getUniqueRef(baseNameVar : String=""): String = {
    baseNameVar + (baseNameVar match {
      case "subject" => rootNode.getChild(SubjectOf("",URI(""))).length
      case "object" => rootNode.getChild(ObjectOf("",URI(""))).length
      case "something" => rootNode.getChild(Something("")).length
      case "linkTo" => rootNode.getChild(LinkTo("",URI(""))).length
      case "linkFrom" => rootNode.getChild(LinkFrom("",URI(""))).length
      case _ => randomUUID.toString
    }).toString
  }

  /* start a request with a variable */
  def something( ref : String = getUniqueRef("something") ) : SWDiscovery = {
    debug(" -- something -- ")
    focusManagement(Something(ref))
  }

  /* create node which focus is the subject : ?focusId <uri> ?target */
  def isSubjectOf( term : SparqlDefinition , ref : String = getUniqueRef("subject")  ) : SWDiscovery =
    checkQueryVariable(term).focusManagement(SubjectOf(ref,term))


  /* create node which focus is the subject : ?target <uri> ?focusId */
  def isObjectOf( term : SparqlDefinition , ref : String = getUniqueRef("object")  ) : SWDiscovery =
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

  def datatype( uri : URI, ref : String ) : SWDiscovery = focusManagement(DatatypeNode(focusNode,SubjectOf(ref,uri)), false)


  def set( term : SparqlDefinition ) : SWDiscovery =
    checkQueryVariable(term).focusManagement(Value(term),forward = false)

  def setList( uris : Seq[URI] ) : SWDiscovery = focusManagement(ListValues(uris),forward = false)


  def getSerializedQuery : String = SerializationBuilder.serialize(this)


  def setSerializedQuery(query : String) : SWDiscovery = SerializationBuilder.deserialize(query)


  def console() : SWDiscovery = {
    debug(" -- console -- ")
    println("USER REQUEST\n" +
      pm.SimpleConsole.get(rootNode) + "\n" +
      "FOCUS NODE:"+ focusNode +
      "\nENDPOINT:"+config.sources().map(v => println(v.url)).mkString(",") +"\n\n" +
      "\n -- SPARQL Request -- \n\n" +
      sparql())
      //"QUERY PLANNER\n"+
      //"todo....")
    this
  }

  def sparql() : String = SparqlQueryBuilder.selectQueryString(rootNode)

  def projection( ref: String ) : SWDiscovery = focusManagement(Projection(Seq(QueryVariable(ref)),getUniqueRef()))

  def projection( lRef: Seq[String] )  : SWDiscovery = focusManagement(Projection(lRef.map(QueryVariable(_)),getUniqueRef()))

  def distinct : SWDiscovery = focusManagement(Distinct(), false)

  def reduced : SWDiscovery = focusManagement(Reduced(getUniqueRef()), false)

  def limit( value : Int ) : SWDiscovery = focusManagement(Limit(value,getUniqueRef()), false)

  def offset( value : Int ) : SWDiscovery = focusManagement(Offset(value,getUniqueRef()), false)

  def orderByAsc( ref: String ) : SWDiscovery = focusManagement(OrderByAsc(Seq(QueryVariable(ref)),getUniqueRef()), false)

  def orderByAsc( lRef: Seq[String] ) : SWDiscovery = focusManagement(OrderByAsc(lRef.map(QueryVariable(_)),getUniqueRef()), false)

  def orderByDesc( ref: String ) : SWDiscovery = focusManagement(OrderByDesc(Seq(QueryVariable(ref))), false)

  def orderByDesc( lRef: Seq[String] ) : SWDiscovery = focusManagement(OrderByDesc(lRef.map(QueryVariable(_))), false)


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
  def select(lRef: Seq[String] = List(), limit : Int = 0, offset : Int = 0) : SWTransaction =
    SWTransaction(
      this.root()
        .limit(limit)
        .offset(offset)
        .projection(lRef))

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
