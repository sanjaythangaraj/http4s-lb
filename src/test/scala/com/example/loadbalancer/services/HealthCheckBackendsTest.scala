package com.example.loadbalancer.services

import cats.effect.*
import munit.CatsEffectSuite
import scala.concurrent.duration.*

import com.example.loadbalancer.domain.*
import com.example.loadbalancer.http.HttpClient

class HealthCheckBackendsTest extends CatsEffectSuite {
  test("add backend url to the backends as soon as health check returns true") {
    val httpClient: HttpClient = (_, _) => IO.pure("Hello")
    val obtained = for {
      backendsUrls    <- IO.ref(Urls(Vector(Url("localhost:8082"))))
      healthCheckUrls <- IO.ref(Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply)))
      result <- HealthCheckBackends.checkHealthAndUpdateBackends(
        UrlsRef.HealthChecks(healthCheckUrls),
        UrlsRef.Backends(backendsUrls),
        ParseUri.Impl,
        UpdateBackendsAndGet.Impl,
        RoundRobin.forHealthChecks,
        SendAndExpect.toHealthCheck(httpClient)
      )
    } yield result

    assertIO(obtained, Urls(Vector("localhost:8082", "localhost:8081").map(Url.apply)))
  }

  test("remove backend url from the backends as soon as health check returns failure") {
    val httpClient: HttpClient = (_, _) => IO.sleep(6.seconds).as("")
    val obtained = for {
      backendsUrls <- IO.ref(Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply)))
      healthCheckUrls <- IO.ref(Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply)))
      result <- HealthCheckBackends.checkHealthAndUpdateBackends(
        UrlsRef.HealthChecks(healthCheckUrls),
        UrlsRef.Backends(backendsUrls),
        ParseUri.Impl,
        UpdateBackendsAndGet.Impl,
        RoundRobin.forHealthChecks,
        SendAndExpect.toHealthCheck(httpClient)
      )
    } yield result

    assertIO(obtained, Urls(Vector("localhost:8082").map(Url.apply)))
  }
}
