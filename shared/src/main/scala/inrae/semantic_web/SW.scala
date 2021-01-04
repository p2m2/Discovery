package inrae.semantic_web

import inrae.semantic_web.event.{DiscoveryRequestEvent, DiscoveryStateRequestEvent, Publisher, Subscriber}

import java.util.UUID.randomUUID
import inrae.semantic_web.internal.Node.references
import inrae.semantic_web.internal._
import inrae.semantic_web.internal.pm.SelectNode
import inrae.semantic_web.rdf._
import inrae.semantic_web.sparql.QueryResult
import wvlet.log.Logger
import wvlet.log.Logger.rootLogger._

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

final case class DiscoveryException(private val message: String = "",
                                                 private val cause: Throwable = None.orNull) extends Exception(message,cause)

object SW {

  private val version : String = "0.0.2"

  info(" --------------------------------------------------" )
  info(" ---- version Discovery :"+ version + "          -----------" )
  info(" --------------------------------------------------" )
}

case class SW(var config: StatementConfiguration)
  extends Subscriber[DiscoveryRequestEvent,QueryManager]
{
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  /* root node */
  private val rootNode   : Root = Root()
  /* focus node */
  private var focusNode  : Node = rootNode


  this.prefix("owl",IRI("http://www.w3.org/2002/07/owl#"))
  this.prefix("rdf",IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#"))
  this.prefix("rdfs",IRI("http://www.w3.org/2000/01/rdf-schema#"))

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

  def variable(reference: String) : Option[String] = {
    debug(" -- variable -- ")
    val variableNameList = pm.SelectNode.getNodeWithRef(reference, rootNode)
      .map( v => {
        pm.SparqlGenerator.correspondenceVariablesIdentifier(rootNode)._1.getOrElse(reference,"")
      })

    if (variableNameList.filter(_ != "").length==0) {
     None
    } else {
      Some(variableNameList(0))
    }
  }

  /**
   * Return solutions as Future corresponding with the current Node request.
   * @param lRef : selected variables
   * @param limit : upper bound on the number of solutions returned
   * @param offset : solution are generated after this offset
   * @return
   */
  def select(lRef: Seq[String] = List(), limit : Int = 0, offset : Int = 0) : Future[ujson.Value] = {
    debug(" -- select -- ")
    trace("selected variables :"+lRef.toString)

    val mapId2Var =  pm.SparqlGenerator.correspondenceVariablesIdentifier(rootNode)._1

    trace("Mapping variable <-> references :\n" + mapId2Var.toString().split(",").mkString("\n"))

    val lDatatype = rootNode.lDatatypeNode.filter(ld => lRef.contains(ld.property.reference()))
    trace("list datatype : "+lDatatype.toString)

    val lSelectVariables = {
      /* select uri type ask with decoration/datatype */
      lDatatype.map(ld => {
        mapId2Var(ld.refNode)
      }) ++ {
        /* select user ask variable */
        lRef match {
          case v if v.length > 0 => v.flatMap(ref => variable(ref))
          case _ => references(focusNode).flatMap(ref => variable(ref))
        }
      }
    }.distinct

    trace("lSelectVariables :::" + lSelectVariables.toString())

    val p = Promise[ujson.Value]()

    /* manage variable name */
    val qm = QueryManager(config)
    qm.subscribe(this.asInstanceOf[Subscriber[DiscoveryRequestEvent,Publisher[DiscoveryRequestEvent]]])
    qm.queryVariables(rootNode,lSelectVariables,limit,offset)
      /* manage datatype decoration */
       .map( (qr : QueryResult) => {
         notify(DiscoveryRequestEvent(DiscoveryStateRequestEvent.DATATYPE_BUILD))
         /* create an empty set of datatypes */
         qr.json("results").update("datatypes",ujson.Obj())
         trace(qr.json)
         /* manage datatype */
         trace("  lDatatype ====> " + lDatatype.toString())

         Future.sequence(lDatatype.map(datatypeNode => {
             trace("datatype node:"+datatypeNode)

             rootNode.getRdfNode(datatypeNode.refNode) match {
               case Some(_) => {

                 /* find uris value inside results to decorate */
                 val lUris : Seq[SparqlDefinition] =
                   try {
                     qr.getValues(mapId2Var(datatypeNode.refNode))
                   } catch {
                     case _ : Throwable => {
                       List()
                     }
                   }
                 Future.sequence(QueryManager(config).process_datatypes(qr,datatypeNode,lUris))
               }
               case None => {
                 Future { }
               }
             }
           })) onComplete {
             case Success(_) => {
               notify(DiscoveryRequestEvent(DiscoveryStateRequestEvent.DATATYPE_DONE))
               qr.v2Ident(mapId2Var)
               p success qr.json
              }
             case Failure(e) => {
               p failure(e)
             }
           }
       }).recover( exception => {
          p failure(exception)
        })
    p.future
  }

  def count() : Future[Int] = {
    debug(" -- count -- ")
    val qm =QueryManager(config)
    qm.subscribe(this.asInstanceOf[Subscriber[DiscoveryRequestEvent,Publisher[DiscoveryRequestEvent]]])
    qm.countNbSolutions(rootNode)
  }

  /**
   * Give an iterable object to browse and obtain all solution performed by a select.
   * @param lRef : selected variables
   * @return iterable on select function
   */
  def selectByPage(lRef: Seq[String] = List())  : Future[(Int,Seq[LazyFutureJsonValue])] = {
    count().map(
      nsolutions => {
        val nit : Int = nsolutions / config.conf.settings.pageSize
        (nit+1,(0 to nit).map( p =>{
          val limit = config.conf.settings.pageSize
          val offset = p*config.conf.settings.pageSize
          LazyFutureJsonValue( () => select(lRef,limit,offset) )
        }))
      })
  }

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

  /**
   * Event management
   * @param pub
   * @param event
   */

  var notifyFunList = Map[String, ( String  => Unit)]()

  def notify (event: DiscoveryRequestEvent) : Unit = {
    notifyFunList.values.map( fun => fun(event.state.toString) )
  }

  def notify (pub: QueryManager, event: DiscoveryRequestEvent) : Unit = {
    notifyFunList.values.map( fun => fun(event.state.toString) )
  }

  def subscribe( id: String, userFunctionTriggered : ( String  => Unit) ): Unit = {
    notifyFunList = notifyFunList + (id ->  userFunctionTriggered)
  }

  def unsubscribe(id: String ) : Unit = {
    notifyFunList = notifyFunList - id
  }

  def abort(): Unit = {

  }
}
