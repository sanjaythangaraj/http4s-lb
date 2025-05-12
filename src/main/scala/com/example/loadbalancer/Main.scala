package com.example.loadbalancer

import cats.effect.{IO, IOApp}
import cats.implicits.*
import com.comcast.ip4s.*
import com.example.loadbalancer.domain.*
import com.example.loadbalancer.errors.config.InvalidConfig
import com.example.loadbalancer.http.*
import com.example.loadbalancer.services.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*
import pureconfig.ConfigSource

object Main extends IOApp.Simple {

  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] =
    for {
      config <- IO(ConfigSource.default.loadOrThrow[Config])
      backendUrls = config.backends
      backends     <- IO.ref(backendUrls)
      healthChecks <- IO.ref(backendUrls)
      hostAndPort  <- IO.fromEither(hostAndPort(config.host, config.port))
      (host, port) = hostAndPort
      _ <- info"Starting server on $host:$port"
      _ <- HttpServer.start(
        UrlsRef.Backends(backends),
        UrlsRef.HealthChecks(healthChecks),
        port,
        host,
        config.healthCheckInterval,
        ParseUri.Impl,
        UpdateBackendsAndGet.Impl,
        RoundRobin.forBackends,
        RoundRobin.forHealthChecks
      )
    } yield ()

  private def hostAndPort(
      host: String,
      port: Int
  ): Either[InvalidConfig, (Host, Port)] =
    (
      Host.fromString(host),
      Port.fromInt(port)
    ).tupled
      .toRight(InvalidConfig)
}
