package com.mdstech.sample

import java.util.concurrent.ConcurrentLinkedDeque

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContextExecutor

case class Person(name: String, age: Int)
object ServiceJsonProtoocol extends DefaultJsonProtocol {
  implicit val personProtocol = jsonFormat2(Person)
}

trait PersonService {
  implicit val system:ActorSystem
  implicit val materializer:ActorMaterializer
//  implicit val executor:ExecutionContextExecutor

  val list = new ConcurrentLinkedDeque[Person]()

  import ServiceJsonProtoocol._
  import scala.collection.JavaConverters._

  val route =
    path("person") {
      post {
        entity(as[Person]) {
          person => complete {
            list.add(person)
            StatusCodes.Created
          }
        }
      } ~
      get {
        complete {
          list.asScala
        }
      }
    }
}

object Application extends App with PersonService {

  override implicit val system = ActorSystem()
//  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  val routes =
    get {
      pathSingleSlash {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body>Hello world!</body></html>"))
      }
    } ~
      route

  Http().bindAndHandle(routes, config.getString("akka.http.server.host"), config.getInt("akka.http.server.port"))
}
