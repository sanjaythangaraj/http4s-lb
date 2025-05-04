package com.example.loadbalancer.services

import cats.effect.*
import cats.implicits.*
import org.http4s.*
import org.http4s.Status.*
import org.http4s.client.*
import org.typelevel.log4cats.syntax.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import com.example.loadbalancer.http.*

import scala.concurrent.duration.*
import scala.language.postfixOps

trait SendAndExpect[A] {
  def apply(uri: Uri): IO[A]
}

object SendAndExpect {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def toBackend(httpClient: HttpClient, req: Request[IO]): SendAndExpect[String] = (uri: Uri) =>
    info"[LOAD-BALANCER] sending request to $uri" *>
      httpClient.sendAndReceive(uri, req.some).handleErrorWith {
        case UnexpectedStatus(NotFound, _, _) =>
          "resource was not found"
            .pure[IO]
            .flatTap(msg => warn"$msg")
        case _ =>
          s"server with uri: $uri is dead"
          .pure[IO]
          .flatTap(msg => warn"$msg")
      }


  def toHealthCheck(httpClient: HttpClient): SendAndExpect[ServerHealthStatus] = (uri: Uri) =>
   info"[HEALTH-CHECK] checking $uri health" *>
     httpClient.sendAndReceive(uri, none)
       .as(ServerHealthStatus.Alive)
       .flatTap(_ => info"$uri is alive")
       .timeout(5 seconds)
       .handleErrorWith(_ => warn"$uri is dead" *> ServerHealthStatus.Dead.pure[IO])
}
