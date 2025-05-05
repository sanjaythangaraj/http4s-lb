package com.example.loadbalancer.services

import cats.effect.*
import munit.CatsEffectSuite
import com.example.loadbalancer.domain.*
import com.example.loadbalancer.http.*

class UpdateBackendsAndGetTest extends CatsEffectSuite {
  val updateBackendsAndGet = UpdateBackendsAndGet.Impl
  val initialUrls: Vector[Url] = Vector("localhost:8081", "localhost:8082").map(Url.apply)

  test("add the passed url to the backends when the server status is alive") {
    val obtained: IO[Urls] = for {
      urls <- IO.ref(Urls(initialUrls))
      updated <- updateBackendsAndGet(UrlsRef.Backends(urls), Url("localhost:8083"), ServerHealthStatus.Alive)
    } yield updated

    assertIO(obtained, Urls(initialUrls :+ Url("localhost:8083")))
  }

  test("add the passed url to the backends when the server status is dead") {
    val obtained = for {
      urls <- IO.ref(Urls(initialUrls :+ Url("localhost:8083")))
      updated <- updateBackendsAndGet(UrlsRef.Backends(urls), Url("localhost:8083"), ServerHealthStatus.Dead)
    } yield updated

    assertIO(obtained, Urls(initialUrls))
  }
}
