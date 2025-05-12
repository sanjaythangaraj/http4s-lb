package com.example.loadbalancer.services

import cats.effect.*
import com.example.loadbalancer.domain.UrlsRef
import org.http4s.*
import org.http4s.dsl.*

object LoadBalancer {
  def from(
      backends: UrlsRef.Backends,
      sendAndExpectResponse: Request[IO] => SendAndExpect[String],
      parseUri: ParseUri,
      addRequestPathToBackendUrl: AddRequestPathToBackendUrl,
      backendsRoundRobin: RoundRobin.BackendRoundRobin
  ): HttpRoutes[IO] = {
    val dsl = Http4sDsl[IO]
    import dsl._

    HttpRoutes.of[IO] { request =>
      backendsRoundRobin(backends).flatMap {
        _.fold(Ok("All backends are inactive")) { backendUrl =>
          val url = addRequestPathToBackendUrl(backendUrl.value, request)
          for {
            uri <- IO.fromEither(parseUri(url))
            response <- sendAndExpectResponse(request)(uri)
            result <- Ok(response)
          } yield result
        }
      }
    }
  }
}
