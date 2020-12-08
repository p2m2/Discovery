package application.menucomp

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router.RouterCtl
import router.Pages
import inrae.semantic_web.StatementConfiguration
import inrae.semantic_web.SW
import inrae.semantic_web.rdf.{IRI, URI}

import scala.concurrent.{Future}

object AmazingPageComp {
  import scala.concurrent.ExecutionContext.Implicits.global

  val config: StatementConfiguration = new StatementConfiguration()
  config.setConfigString(
    """
      |{
      | "sources" : [{
      |   "id"  : "bbip",
      |   "url" : "https://bbip.askomics.org/virtuoso/sparql",
      |   "typ" : "tps",
      |   "method" : "GET",
      |   "mimetype" : "json"
      | }]}
      |""".stripMargin)
  val query = SW(config)
  val res : Future[String] = query
    .prefix("owl", IRI("http://www.w3.org/2002/07/owl#"))
    .prefix("asko", IRI("http://askomics.org/internal/"))
    .something("entity")
    .isA(URI("Class", "owl"))
    .filter.not.contains("http://www.w3.org/2002/07/owl#")
    .debug()
    //.sparql_console()
    .select(List("entity"))
    .map(json => { println(json("results").toString());json("results").toString() } )
    /*
    .onComplete {
      case Success(result) => {
        println("========================= RESULTS ===============================")
        println(result)
      }
      case Failure(exception) => println(exception)
    }*/

  case class Props(router: RouterCtl[Pages])
  case class State()

  final class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {

          <.div(
            "Amazing Page ! ===> !!" + res.value.get
          )
    }
  }

  val component = ScalaComponent.builder[Props]("AmazingPageComp")
    .initialState( State( ) )
    .renderBackend[Backend]
    .build

    def apply(router: RouterCtl[Pages]) = component(Props(router))
}