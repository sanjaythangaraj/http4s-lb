package com.example.loadbalancer.http

import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import com.example.loadbalancer.domain.*
import com.example.loadbalancer.services.*
import com.example.loadbalancer.services.RoundRobin.HealthChecksRoundRobin

object HttpServer {
  def start(
      backends: UrlsRef.Backends,
      healthChecks: UrlsRef.HealthChecks,
      port: Port,
      host: Host,
      healthCheckInterval: HealthCheckInterval,
      parseUri: ParseUri,
      updateBackendsAndGet: UpdateBackendsAndGet,
      backendsRoundRobin: RoundRobin.BackendRoundRobin,
      healthChecksRoundRobin: HealthChecksRoundRobin
  ): IO[Unit] = (
    for {
      client <- EmberClientBuilder.default[IO].build
      httpClient = HttpClient.of(client)
      loadBalancer = LoadBalancer
        .from(
          backends,
          SendAndExpect.toBackend(httpClient, _),
          parseUri,
          AddRequestPathToBackendUrl.Impl,
          backendsRoundRobin
        )
        .orNotFound
      httpApp = Logger.httpApp(logHeaders = false, logBody = true)(loadBalancer)
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(host)
        .withPort(port)
        .withHttpApp(httpApp)
        .build
      _ <- HealthCheckBackends
        .periodically(
          healthChecks,
          backends,
          parseUri,
          updateBackendsAndGet,
          healthChecksRoundRobin,
          SendAndExpect.toHealthCheck(httpClient),
          healthCheckInterval
        )
        .toResource
    } yield ()
  ).useForever
}
