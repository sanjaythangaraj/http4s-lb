package com.example.loadbalancer.services

import munit.CatsEffectSuite
import cats.effect.*

import com.example.loadbalancer.domain.*

class RoundRobinTest extends CatsEffectSuite {

  test("forBackends [Some, one url]") {
    val roundRobin = RoundRobin.forBackends
    val assertion = for {
      ref <- IO.ref(Urls(Vector(Url("localhost:8082"))))
      urlsRef = UrlsRef.Backends(ref)
      assertion1 <- roundRobin(urlsRef)
        .map(_.exists(_.value == "localhost:8082"))
      assertion2 <- roundRobin(urlsRef)
        .map(_.exists(_.value == "localhost:8082"))
    } yield assertion1 && assertion2

    assertIOBoolean(assertion)
  }

  test("forBackends [Some, multiple urls]") {
    val roundRobin = RoundRobin.forBackends
    val assertion = for {
      ref <- IO.ref(Urls(Vector(Url("localhost:8081"), Url("localhost:8082"))))
      urlsRef = UrlsRef.Backends(ref)
      assertion1 <- roundRobin(urlsRef)
        .map(_.exists(_.value == "localhost:8081"))
      assertion2 <- ref.get
        .map(_.values.map(_.value) == Vector("localhost:8082", "localhost:8081"))
      assertion3 <- roundRobin(urlsRef)
        .map(_.exists(_.value == "localhost:8082"))
      assertion4 <- ref.get
        .map(_.values.map(_.value) == Vector("localhost:8081", "localhost:8082"))
    } yield List(assertion1, assertion2, assertion3, assertion4).reduce(_ && _)

    assertIOBoolean(assertion)

  }

  test("forBackends [None]") {
    val roundRobin = RoundRobin.forBackends
    val assertion = for {
      ref    <- IO.ref(Urls.empty)
      result <- roundRobin(UrlsRef.Backends(ref))
    } yield result.isEmpty

    assertIOBoolean(assertion)

  }

  test("forHealthChecks [Some, one url]") {
    val roundRobin = RoundRobin.forHealthChecks
    val assertion = for {
      ref <- IO.ref(Urls(Vector(Url("localhost:8081"))))
      urlsRef = UrlsRef.HealthChecks(ref)
      assertion1 <- roundRobin(urlsRef).map(_.value == "localhost:8081")
      assertion2 <- roundRobin(urlsRef).map(_.value == "localhost:8081")
    } yield assertion1 & assertion2

    assertIOBoolean(assertion)
  }

  test("forHealthChecks [Some, multiple urls]") {
    val roundRobin = RoundRobin.forHealthChecks
    val assertion = for {
      ref <- IO.ref(Urls(Vector(Url("localhost:8081"), Url("localhost:8082"))))
      urlsRef = UrlsRef.HealthChecks(ref)
      assertion1 <- roundRobin(urlsRef)
        .map(_.value == "localhost:8081")
      assertion2 <- ref.get
        .map(_.values.map(_.value) == Vector("localhost:8082", "localhost:8081"))
      assertion3 <- roundRobin(urlsRef)
        .map(_.value == "localhost:8082")
      assertion4 <- ref.get
        .map(_.values.map(_.value) == Vector("localhost:8081", "localhost:8082"))
    } yield List(assertion1, assertion2, assertion3, assertion4).reduce(_ && _)

    assertIOBoolean(assertion)
  }

  test("forHealthChecks [Exception, empty urls]") {
    val roundRobin = RoundRobin.forHealthChecks
    val assertion = for {
      ref <- IO.ref(Urls.empty)
      result <- roundRobin(UrlsRef.HealthChecks(ref))
        .as(false)
        .handleError {
          case _: NoSuchElementException => true
          case _                         => false
        }
    } yield result

    assertIOBoolean(assertion)
  }

  test("forBackends [Some, with stateful Ref updates]") {
    val roundRobin = RoundRobin.forBackends
    val assertion = for {
      ref <- IO.ref(Urls(Vector(Url("localhost:8081"), Url("localhost:8082"))))
      urlsRef = UrlsRef.Backends(ref)
      assertion1 <- roundRobin(urlsRef)
        .map(_.exists(_.value == "localhost:8081"))
      _ <- ref.update { urls =>
        Urls(urls.values :+ Url("localhost:8083"))
      }
      assertion2 <- roundRobin(urlsRef)
        .map(_.exists(_.value == "localhost:8082"))
      assertion3 <- ref.get.map { urls =>
        urls.values.map(_.value) == Vector("localhost:8081", "localhost:8083", "localhost:8082")
      }
      assertion4 <- roundRobin(urlsRef)
        .map(_.exists(_.value == "localhost:8081"))
      assertion5 <- roundRobin(urlsRef)
        .map(_.exists(_.value == "localhost:8083"))
    } yield List(assertion1, assertion2, assertion3, assertion4, assertion5).reduce(_ && _)

    assertIOBoolean(assertion)
  }

}
