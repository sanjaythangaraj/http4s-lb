package com.example.loadbalancer.services

import munit.CatsEffectSuite
import cats.effect.*
import org.http4s.*
import org.http4s.client.*

import com.example.loadbalancer.domain.*
import com.example.loadbalancer.http.*

class LoadBalancerTest extends CatsEffectSuite {

  val sendAndExpectSuccess: SendAndExpect[String] = _ => IO("Success")
  val httpClientBackendResourceNotFound: HttpClient = (_, _) =>
    IO.raiseError(
      UnexpectedStatus(
        Status.NotFound,
        Method.GET,
        Uri.unsafeFromString("localhost:8081")
      )
    )

  test("All backends are inactive because Urls is empty") {
    val obtained: IO[String] =
      (
        for {
          backends <- IO.ref(Urls.empty)
          loadBalancer = LoadBalancer.from(
            UrlsRef.Backends(backends),
            _ => sendAndExpectSuccess,
            ParseUri.Impl,
            AddRequestPathToBackendUrl.Impl,
            RoundRobin.forBackends
          )
          result <- loadBalancer.orNotFound.run(Request[IO]())
        } yield result.body.compile.toVector.map(bytes => String(bytes.toArray))
      ).flatten

    assertIO(obtained, "All backends are inactive")
  }

  test("success case") {
    val obtained = (
      for {
        backends <- IO.ref(Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply)))
        loadbalancer = LoadBalancer.from(
          UrlsRef.Backends(backends),
          _ => sendAndExpectSuccess,
          ParseUri.Impl,
          AddRequestPathToBackendUrl.Impl,
          RoundRobin.forBackends
        )
        result <- loadbalancer.orNotFound.run(Request[IO]())
      } yield result.body.compile.toVector.map(bytes => String(bytes.toArray))
    ).flatten

    assertIO(obtained, "Success")

  }

  test("Resource not found (404) case") {
    val obtained = (
      for {
        backends <- IO.ref(Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply)))
        loadBalancer = LoadBalancer.from(
          UrlsRef.Backends(backends),
          _ => SendAndExpect.toBackend(httpClientBackendResourceNotFound, Request[IO]()),
          ParseUri.Impl,
          AddRequestPathToBackendUrl.Impl,
          RoundRobin.forBackends
        )
        result <- loadBalancer.orNotFound.run(Request[IO](uri = Uri.unsafeFromString("localhost:8080/items/1")))
      } yield result.body.compile.toVector.map(bytes => String(bytes.toArray))
    ).flatten

    assertIO(obtained, "resource at uri: localhost:8081/items/1 was not found")
  }

}
