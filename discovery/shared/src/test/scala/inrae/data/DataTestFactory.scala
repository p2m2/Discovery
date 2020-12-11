package inrae.data


import fr.hmil.roshttp.HttpRequest
import fr.hmil.roshttp.Method.POST
import fr.hmil.roshttp.exceptions.HttpException
import fr.hmil.roshttp.response.SimpleHttpResponse
import inrae.semantic_web.StatementConfiguration
import wvlet.log.Logger.rootLogger.{debug, error, info}

import java.io.IOException
import scala.util.{Failure, Success}

final case class DataTestFactoryException(private val message: String = "",
                                                 private val cause: Throwable = None.orNull) extends Exception(message,cause)

object DataTestFactory {
  import monix.execution.Scheduler.Implicits.global

  val port = "8890"
  val container_name = "virtuoso"


  val url_endpoint = "http://localhost:"+port+"/sparql"


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

  def graph(classname: String) = "graph:test:discovery:" + classname.replace("$","")

  /* insert into graph */
  def insert(data : String ,
                   classname: String,
                   url_endpoint : String=url_endpoint) = {

    put(s"""
        INSERT {
            GRAPH <${graph(classname)}>
              {
                ${data}
              }
          }
        """.stripMargin,url_endpoint).onComplete({
          case Success(_) => {
            debug(s"${graph(classname)} is loaded !")
          }
          case Failure(_) => throw new Error(s"Can not load graph :${graph(classname)}")
        })
  }

  def delete(classname: String,
                   url_endpoint : String=url_endpoint) = {

    put(s"DROP SILENT GRAPH <${graph(classname)}>",url_endpoint)

  }



  def getConfig() : StatementConfiguration = {
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
            "logLevel" : "info",
            "sizeBatchProcessing" : 100
          }
         }
        """.stripMargin)
  }

  def getDbpediaConfig() : StatementConfiguration = {
    StatementConfiguration().setConfigString(
      s"""
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
            """.stripMargin.stripMargin)
  }
//   "driver" : "inrae.semantic_web.driver.JenaRequestDriver",
}
