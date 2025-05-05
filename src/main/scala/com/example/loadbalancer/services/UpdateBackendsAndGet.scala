package com.example.loadbalancer.services

import cats.effect.*
import com.example.loadbalancer.domain.*
import com.example.loadbalancer.http.*

trait UpdateBackendsAndGet {
  def apply(backends: UrlsRef.Backends, url: Url, status: ServerHealthStatus): IO[Urls]
}

object UpdateBackendsAndGet {
  object Impl extends UpdateBackendsAndGet {

    override def apply(backends: UrlsRef.Backends, url: Url, status: ServerHealthStatus): IO[Urls] =
      backends.urls.updateAndGet {urls =>
        status match {
          case ServerHealthStatus.Alive => urls.add(url)
          case ServerHealthStatus.Dead => urls.remove(url)
        }
      }
  }
}
