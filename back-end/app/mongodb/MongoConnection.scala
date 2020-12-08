package mongodb

/*
import reactivemongo.api.{AsyncDriver, DB, MongoConnection}
import play.api.Configuration

import scala.concurrent.{ ExecutionContext, Future }

class MongoConnection(configuration: Configuration, dbName: String = "appDatabase") {

  // use any appropriate context
  import ExecutionContext.Implicits.global

  val mongoUri = s"${configuration.get[String]("mongodb.uri")}"
  val parsedUri = MongoConnection.fromString(mongoUri)

  def mongoClient: AsyncDriver = {
    val driver = AsyncDriver()
    driver
  }

  def database: Future[DB] = {
    // Database and collections: Get references
    val futureConnection = parsedUri.flatMap(mongoClient().connect(_))
    futureConnection.flatMap(_.database(dbName))
    //TODO : How manage codecRegistry
    //mongoClient.getDatabase(dbName).withCodecRegistry(CodecRegistry.codecRegistry)
  }

}

 */
