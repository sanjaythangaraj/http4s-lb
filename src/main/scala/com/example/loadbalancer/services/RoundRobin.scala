package com.example.loadbalancer.services

import cats.*
import cats.effect.*

import com.example.loadbalancer.domain.*

trait RoundRobin[F[_]] {
  def apply(ref: UrlsRef): IO[F[Url]]
}

object RoundRobin {
  type BackendRoundRobin      = RoundRobin[Option]
  type HealthChecksRoundRobin = RoundRobin[Id]

  def forBackends: BackendRoundRobin = (ref: UrlsRef) =>
    ref.urls
      .getAndUpdate(_.next)
      .map(_.currentOpt)

  def forHealthChecks: HealthChecksRoundRobin = (ref: UrlsRef) =>
    ref.urls
      .getAndUpdate(_.next)
      .map(_.currentUnsafe)
  
}
