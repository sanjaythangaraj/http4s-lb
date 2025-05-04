package com.example.loadbalancer.services

import cats.*
import cats.effect.*
import scala.util.Try

import com.example.loadbalancer.domain.*

trait RoundRobin[F[_]] {
  def apply(ref: UrlsRef): IO[F[Url]]
}

object RoundRobin {
  type BackendRoundRobin      = RoundRobin[Option]
  type HealthChecksRoundRobin = RoundRobin[Id]

  def forBackends: BackendRoundRobin = (ref: UrlsRef) =>
    ref.urls
      .getAndUpdate(next)
      .map(_.currentOpt)

  def forHealthChecks: HealthChecksRoundRobin = (ref: UrlsRef) =>
    ref.urls
      .getAndUpdate(next)
      .map(_.currentUnsafe)

  private val next: Urls => Urls = (urls: Urls) =>
    Try {
      Urls(urls.values.tail :+ urls.values.head)
    }.getOrElse(Urls.empty)
}
