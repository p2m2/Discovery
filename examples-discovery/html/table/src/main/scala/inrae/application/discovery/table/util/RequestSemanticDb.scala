package inrae.application.discovery.table.util

import inrae.semantic_web.rdf.{Literal, QueryVariable, SparqlBuilder, URI}
import inrae.semantic_web.{SW, StatementConfiguration}
import wvlet.log.Logger.rootLogger.info

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

case class RequestSemanticDb(endpoint: String, method: String = "POST_ENCODED", `type`: String = "tps") {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val config: StatementConfiguration = new StatementConfiguration()
  config.setConfigString(
    """
      {
       "sources" : [{
         "id"     : "current",
         "url"    : """" + endpoint +
      """",
         "type"   : """" + `type` +
      """",
         "method" : """" + method +
      """"
       }]}
      """.stripMargin)


  def getEntities(): Future[List[(URI, String)]] = {
    SW(config).something("instance")
      .datatype(URI("label", "rdfs"), "label")
      .isSubjectOf(URI("a"))
      .set(URI("Class", "owl"))
      .select(List("instance", "label"))
      .map(response => response("results")("bindings").arr.map(r => {
        val uri = SparqlBuilder.createUri(r("instance"))
        Try(SparqlBuilder.createLiteral(response("results")("datatypes")("label")(uri.toString)(0)).toString) match {
          case Success(v) => (uri, v)
          case Failure(_) => (uri, uri.naiveLabel())
        }
      }).toList)
  }

  def getAttributes(selectedEntity: URI): Future[List[(URI, String)]] = {
    info(" -- getAttributes --")
    info("uri:" + selectedEntity.toString())
    val query = SW(config).something("attributeProperty")
      .datatype(URI("label", "rdfs"), "label")
      .isA(URI("DatatypeProperty", "owl"))
      /* .focus("attributeProperty")
        .isSubjectOf(URI("range","rdfs"))
          .set(selectedEntity) */
      .root()
      .something("instance")
      .isA(selectedEntity)
      .isSubjectOf(QueryVariable("attributeProperty"))
      .select(List("attributeProperty", "label"))

    query.map(
      response => {
        response("results")("bindings").arr.map(r => {

          val uri = SparqlBuilder.createUri(r("attributeProperty"))
          //val uri = r("attributeProperty")("value").toString
          val label = uri.naiveLabel()

            Try(SparqlBuilder.createLiteral(response("results")("datatypes")("label")(uri.toString)(0)).toString) match {
              case Success(v) => (uri,v)
              case Failure(e) => (uri, label)
            }
        })
      }.toList)
  }

  def getValues(entity: URI, attributes: List[URI]): Future[Map[URI, Map[URI, Literal]]] = {
    info(" -- getValues --")
    var query = SW(config).something("instance")
      .isA(entity)

    attributes.foreach(attribute => {
      query = query.focus("instance").isSubjectOf(attribute, attribute.naiveLabel())
    })

    query = query.focus("instance").datatype(URI("label", "rdfs"), "label_instance")
    query = query.focus("instance").datatype(URI("https://metabohub.peakforest.org/ontology/property#version"), "test_version")

    query.select(List("instance", "label_instance", "test_version") ++ attributes.map(_.naiveLabel())).map(
      response => {
        //{
        //println(" ==============  RESULTS datatypes ===========================  ")
        //println(response("results")("datatypes").toString)
        /* Instance Label */

        /* datatypes */
        //response("results")("datatypes").obj.map( datatypeName =>{
        //  println(datatypeName)
        // })
        // }

        response("results")("bindings").arr.map(row => {
          val uriInstance = SparqlBuilder.createUri(row("instance"))

          (uriInstance -> (attributes.map(
            uri => {
              (uri -> SparqlBuilder.createLiteral(row(uri.naiveLabel())))
            }
          ) ++ {
            val labelInstance = SparqlBuilder.createLiteral(response("results")("datatypes")("label_instance")(uriInstance.localName).arr.head)
            List(uriInstance -> labelInstance)
          }).toMap)
        }).toMap
      }
    )
  }

}
