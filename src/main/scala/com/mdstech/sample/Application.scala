package com.mdstech.sample

import java.io.File

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.{Http, server}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._

import scala.io.StdIn

object Application {
  def main(args: Array[String]): Unit = {

//    import com.typesafe.config.Config
//    import com.typesafe.config.ConfigFactory
//    val parsedConfig = ConfigFactory.parseResources(getClass.getClassLoader, "application.conf.old")
//    println(parsedConfig.isEmpty)
//    val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + "2556" + "\n" + "akka.remote.artery.canonical.port=" + "2556")
//      .withFallback(ConfigFactory.load(parsedConfig).getConfig("Master"))

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
