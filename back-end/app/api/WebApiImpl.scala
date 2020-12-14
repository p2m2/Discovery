package api

import shared.api.WebApi
import play.api.Configuration
import wvlet.log.LogSupport
import scala.controllers.AutowireContext
import scala.concurrent.{ExecutionContext, Future}
import shared.models.{ClientUser, IncorrectCredentials, LoginUserError, UnknownUserError, User, UserExist}

class WebApiImpl(context: AutowireContext, configuration: Configuration) extends WebApi with LogSupport {
  import ExecutionContext.Implicits.global

  def getLoggedUser(): Future[Option[ClientUser]] = {
    Future {
      Some(ClientUser("bidon@test.fr", "bidonsession"))
    }
  }

  def signIn(email: String, password: String): Future[Either[LoginUserError, ClientUser]] = {
    Future {
      Right(ClientUser("bidon@test.fr", "bidonsession"))
    }
  }

  def signUp(email: String, password: String): Future[Either[LoginUserError, User]] = {
    Future {
      Right(User("1","bidon@test.fr", "bidon","bidonsession"))
    }
  }

  def signOut(): Future[Unit] = {
    Future {

    }
  }

}

/*
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson._
import mongodb.{MongoConnection, MongoDatabases}
import org.bson.types.ObjectId
import org.mongodb.scala.MongoWriteException
import play.api.Configuration
import play.api.libs.json.Json.obj
import play.api.mvc.Cookie
import shared.api.WebApi
import shared.models.{ClientUser, IncorrectCredentials, LoginUserError, UnknownUserError, User, UserExist}
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.controllers.AutowireContext


class WebApiImpl(context: AutowireContext, configuration: Configuration) extends WebApi with LogSupport {
  // use any appropriate context
  import ExecutionContext.Implicits.global

  private final val cookieSession = context.playRequest.cookies.get(Defs.PLAY_SESSION).getOrElse(Cookie("noCookieSet", "noCookieSet")).value

  case class User(email: String, password: String, sessionId: String)

  implicit def userWriter: BSONDocumentWriter[User] = Macros.writer[User]
  // use personWriter
  def createUser(user: User, personCollection : Future[BSONCollection]): Future[Unit] =
    personCollection.flatMap(_.insert.one(user).map(_ => {}))

  def updateUser(user: User, personCollection : Future[BSONCollection]): Future[Int] = {
    val selector = document(
      "email" -> user.email,
      "password" -> user.password,
      "sessionId" -> user.sessionId
    )

    // Update the matching person
    personCollection.flatMap(_.update.one(selector, user).map(_.n))
  }

  implicit def personReader: BSONDocumentReader[User] = Macros.reader[User]

  def getLoggedUser(): Future[Option[ClientUser]] = {
    lazy val collection : Future[BSONCollection] = new MongoConnection(configuration).database.map(_.collection(MongoDatabases.USERS_DB))
    for(
        list <- collection.map( _.find(BSONDocument("sessionId" ->cookieSession)) )
      ) yield {
        if(list.length != 1) None
        else list.headOption.map(_.clientUser)
      }
  }

  def signIn(email: String, password: String): Future[Either[LoginUserError, ClientUser]] = {
    lazy val emailLowerCase = email.toLowerCase()
    info(s"email $emailLowerCase $password")
    lazy val collection : Future[BSONCollection] = new MongoConnection(configuration).database.map(_.collection(MongoDatabases.USERS_DB))
    for {
      list <- collection.find(obj("email" -> emailLowerCase, "password" -> password)).list()
      _ <- collection.updateOne(obj("email" -> emailLowerCase, "password" -> password), obj("$set" -> obj("sessionId" -> cookieSession))).map(
        _ => collection.updateMany(obj("sessionId" -> cookieSession, "email" -> obj("$not"-> obj("$eq" -> emailLowerCase))), obj("$set" -> obj("sessionId" -> "loggedOut")))
      )
    } yield {
      if(list.length > 1) Left(UnknownUserError("User in DB is ambiguous"))
      else if(list.isEmpty) Left(IncorrectCredentials())
      else Right(list.head.clientUser)
    }
  }

  def signUp(email: String, password: String): Future[Either[LoginUserError, User]] = {
    lazy val emailLowerCase = email.toLowerCase()
    info(s"signup $emailLowerCase")
    lazy val collection: BSONCollection = new MongoConnection(configuration).database.getCollection(MongoDatabases.USERS_DB)
    val userToInsert = User(new ObjectId().toString, emailLowerCase, password, cookieSession)
      for {
        _ <- collection.insertOne(userToInsert) recoverWith {case t: MongoWriteException => Future.failed(UserExist(email))}
        _ <- collection.updateMany(obj("sessionId" -> cookieSession, "email" -> obj("$not" -> obj("$eq" -> emailLowerCase))), obj("$set" -> obj("sessionId" -> "loggedOut")))
        user <- collection.find(obj("email" -> emailLowerCase)).list()
      } yield {
        if (user.length < 1) Left(UnknownUserError("User was not added, please try again later"))
        else if (user.length == 1) Right(user.head)
        else Left(UnknownUserError("Some error occurred, please try again later"))
      }

    } recoverWith {
    case t: UserExist => Future.successful(Left(UserExist(email)))
    case t: Throwable => Future.successful(Left(UnknownUserError(t.getMessage)))
  }

  def signOut(): Future[Unit] = {
    info(s"signOut")
    lazy val collection: MongoCollection[User] = new MongoConnection(configuration).database.getCollection(MongoDatabases.USERS_DB)
    for{
      logoutSessions <- collection.updateMany(obj("sessionId" -> cookieSession), obj("$set" -> obj("sessionId" -> "loggedOut")))
    } yield {
      info(s"COUNT: ${logoutSessions.getMatchedCount}")
    }
  }


}
*/