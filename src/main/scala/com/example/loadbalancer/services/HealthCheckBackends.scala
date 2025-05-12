package com.example.loadbalancer.services

import cats.effect.*
import scala.concurrent.duration.*

import com.example.loadbalancer.domain.*
import com.example.loadbalancer.http.ServerHealthStatus
import com.example.loadbalancer.services.RoundRobin.HealthChecksRoundRobin

object HealthCheckBackends {

  def periodically(
      healthChecksUrlsRef: UrlsRef.HealthChecks,
      backendsUrlsRef: UrlsRef.Backends,
      parseUri: ParseUri,
      updateBackendsAndGet: UpdateBackendsAndGet,
      healthChecksRoundRobin: HealthChecksRoundRobin,
      sendAndExpectStatus: SendAndExpect[ServerHealthStatus],
      healthCheckInterval: HealthCheckInterval
  ): IO[Unit] = checkHealthAndUpdateBackends(
    healthChecksUrlsRef,
    backendsUrlsRef,
    parseUri,
    updateBackendsAndGet,
    healthChecksRoundRobin,
    sendAndExpectStatus
  ).flatMap(_ => IO.sleep(healthCheckInterval.value.seconds)).foreverM

  private[services] def checkHealthAndUpdateBackends(
      healthChecksUrlsRef: UrlsRef.HealthChecks,
      backendsUrlsRef: UrlsRef.Backends,
      parseUri: ParseUri,
      updateBackendsAndGet: UpdateBackendsAndGet,
      healthChecksRoundRobin: HealthChecksRoundRobin,
      sendAndExpectStatus: SendAndExpect[ServerHealthStatus]
  ): IO[Urls] =
    for {
      url     <- healthChecksRoundRobin(healthChecksUrlsRef)
      uri     <- IO.fromEither(parseUri(url.value))
      status  <- sendAndExpectStatus(uri)
      updated <- updateBackendsAndGet(backendsUrlsRef, url, status)
    } yield updated
}
