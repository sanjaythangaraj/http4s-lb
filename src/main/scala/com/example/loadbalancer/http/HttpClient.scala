package com.example.loadbalancer.http

import org.http4s.*
import org.http4s.client.*
import cats.effect.*

trait HttpClient {
  def sendAndReceive(uri: Uri, requestOpt: Option[Request[IO]]): IO[String]
}

object HttpClient {
  def of(client: Client[IO]): HttpClient = (uri: Uri, requestOpt: Option[Request[IO]]) =>
    requestOpt match
      case Some(request) => client.expect[String](request.withUri(uri))
      case None => client.expect[String](uri)
}
