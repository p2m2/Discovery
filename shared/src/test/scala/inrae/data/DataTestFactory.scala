package inrae.data

import fr.hmil.roshttp.HttpRequest
import fr.hmil.roshttp.Method.POST
import fr.hmil.roshttp.exceptions.HttpException
import fr.hmil.roshttp.response.SimpleHttpResponse
import inrae.semantic_web.StatementConfiguration
import wvlet.log.Logger.rootLogger.error

import java.io.IOException
import scala.concurrent.Future

final case class DataTestFactoryException(private val message: String = "",
                                          private val cause: Throwable = None.orNull) extends Exception(message,cause)

object DataTestFactory  {
  import monix.execution.Scheduler.Implicits.global

  val url_endpoint = "http://localhost:8890/sparql"

  val default_http_driver = "inrae.semantic_web.driver.RosHTTPDriver"

  def put(stringQuery : String, url_endpoint : String) = {

    HttpRequest(url_endpoint)
      //  .withHeader("Authorization", "Basic " + Base64.getEncoder.encodeToString("dba:dba".getBytes))
      .withMethod(POST)
      .withQueryParameter("query",stringQuery)
      .send()
      .recover {
        case HttpException(e: SimpleHttpResponse) =>
          // Here we may have some detailed application-level insight about the error
          error("There was an issue with your request." +
            " Here is what the application server says: " )
        case e: IOException =>
          error(s"${url_endpoint} is not reachable. ")
      }
  }

  def graph1(classname: String) = "graph:test:discovery:virtuoso1:" + classname.replace("$","")
  def graph2(classname: String) = "graph:test:discovery:virtuoso2:" + classname.replace("$","")

  private def insert(data : String,
                     graph: String,
                     url_endpoint : String=url_endpoint) : Future[Any] = {
    put(s"""
        PREFIX owl: <http://www.w3.org/2002/07/owl#>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

        INSERT {
            GRAPH <${graph}>
              {
                ${data}
              }
          }
        """.stripMargin,url_endpoint)
      .map( _ => { println(s" ------------- ${graph} is loaded ! -------------------- ") })
      .recover( _ =>  { throw new Error(s"Can not load graph :${graph}") } )
  }

  def insert_virtuoso1(data : String,
                       classname: String,
                       url_endpoint : String=url_endpoint) : Future[Any] = insert(data,graph1(classname),url_endpoint)

  def insert_virtuoso2(data : String,
                       classname: String,
                       url_endpoint : String=url_endpoint): Future[Any]= insert(data,graph2(classname),url_endpoint)

  private def delete(graph: String,
                     url_endpoint : String=url_endpoint) : Future[Any] = {
    put(s"DROP SILENT GRAPH <${graph}>",url_endpoint)
      .map( _ => { println(s" ------------- ${graph} is deleted ! -------------------- ") })

  }

  def delete_virtuoso1(classname: String,
                       url_endpoint : String=url_endpoint) = delete(graph1(classname),url_endpoint)

  def delete_virtuoso2(classname: String,
                       url_endpoint : String=url_endpoint) = delete(graph2(classname),url_endpoint)


  def getConfigVirtuoso1() : StatementConfiguration = {
    StatementConfiguration().setConfigString(
      s"""
        {
         "sources" : [{
           "id"       : "local",
           "url"      : "${DataTestFactory.url_endpoint}",
           "type"     : "tps",
           "method"   : "POST",
           "mimetype" : "json"
         }],
         "settings" : {
            "driver" : "${default_http_driver}",
            "logLevel" : "off",
            "sizeBatchProcessing" : 100
          }
         }
        """.stripMargin)
  }

  def getConfigVirtuoso2() : StatementConfiguration = {
    StatementConfiguration().setConfigString(
      s"""
        {
         "sources" : [{
           "id"       : "local",
           "url"      : "${DataTestFactory.url_endpoint}",
           "type"     : "tps",
           "method"   : "POST",
           "mimetype" : "json"
         }],
         "settings" : {
            "driver" : "${default_http_driver}",
            "logLevel" : "off",
            "sizeBatchProcessing" : 100
          }
         }
        """.stripMargin)
  }

  val dbpedia_config_string = """
            {
             "sources" : [{
               "id"  : "dbpedia",
               "url" : "https://dbpedia.org/sparql",
               "type" : "tps",
               "method" : "POST"
             }],
            "settings" : {
              "driver" : "inrae.semantic_web.driver.JenaRequestDriver",
              "logLevel" : "trace",
              "sizeBatchProcessing" : 100,
              "cache" : false
             }
            }
            """.stripMargin.stripMargin

  def getDbpediaConfig() : StatementConfiguration = {
    StatementConfiguration().setConfigString(dbpedia_config_string)
  }
  //   "driver" : "inrae.semantic_web.driver.JenaRequestDriver",
}
