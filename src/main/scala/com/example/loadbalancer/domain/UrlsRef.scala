package com.example.loadbalancer.domain

import cats.effect.*

enum UrlsRef(val urls: Ref[IO, Urls]) {
  case Backends(override val urls: Ref[IO, Urls])     extends UrlsRef(urls)
  case HealthChecks(override val urls: Ref[IO, Urls]) extends UrlsRef(urls)
}
