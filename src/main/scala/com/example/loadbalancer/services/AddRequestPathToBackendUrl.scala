package com.example.loadbalancer.services

import cats.effect.*
import org.http4s.*

trait AddRequestPathToBackendUrl {
  def apply(backendUrl: String, request: Request[IO]): String
}

object AddRequestPathToBackendUrl {
  object Impl extends AddRequestPathToBackendUrl {

    override def apply(backendUrl: String, request: Request[IO]): String =
      val requestPath = request.uri.path.renderString
        .dropWhile(_ != '/')

      backendUrl concat requestPath
  }
}
