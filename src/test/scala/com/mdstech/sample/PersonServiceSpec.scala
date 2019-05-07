package com.mdstech.sample

import akka.http.scaladsl.model._
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PersonServiceSpec extends WordSpec with Matchers with ScalatestRouteTest with PersonService {
  "Person API" should {
    "Posting to /person should add the person" in {
      val jsonRequest = ByteString(
        """
          |{
          | "name":"test",
          | "age" : 25
          |}
        """.stripMargin)

      val postRequest = HttpRequest(HttpMethods.POST, uri = "/person", entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

      postRequest ~> route ~> check {
        status.isSuccess() shouldEqual true
      }
    }
  }
}
