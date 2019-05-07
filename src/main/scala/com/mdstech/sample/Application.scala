package com.mdstech.sample

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.{Http, server}
import akka.stream.ActorMaterializer

import scala.io.StdIn

object Application {
  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("SampleActorSystem")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    def innerRoute(id: Int): server.Route =
      get {
        complete {
          "Received GET request for order " + id
        }
      } ~
        put {
          complete {
            "Received PUT request for order " + id
          }
        }


    val route =
      get {
        pathSingleSlash {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,"<html><body>Hello world!</body></html>"))
        } ~
          path("ping") {
            complete("PONG!")
          } ~
          path("crash") {
            sys.error("BOOM!")
          } ~
          path( "test") {
            complete("done")
          } ~
          path("order" / IntNumber) {
            id => innerRoute(id)
          }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done
  }
}
