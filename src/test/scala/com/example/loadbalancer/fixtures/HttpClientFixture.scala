package com.example.loadbalancer.fixtures

import cats.effect.*
import org.http4s.*
import org.http4s.client.*
import scala.concurrent.duration.*

import com.example.loadbalancer.http.*


trait HttpClientFixture {

  val httpClientHello: HttpClient = (_, _) => IO.pure("Hello")
  val httpClientRuntimeException: HttpClient = (_, _) => IO.raiseError(new RuntimeException("Server is dead"))
  val httpClientTimeoutFailure: HttpClient = (_, _) => IO.sleep(6.seconds).as("")
  val httpClientBackendResourceNotFound: HttpClient = (_, _) =>
    IO.raiseError {
      UnexpectedStatus(
        org.http4s.Status.NotFound,
        org.http4s.Method.GET,
        Uri.unsafeFromString("localhost:8081"),
      )
    }
}
