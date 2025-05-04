package com.example.loadbalancer.services

import cats.effect.*
import org.http4s.*
import munit.CatsEffectSuite
import com.example.loadbalancer.fixtures.{HttpClientFixture, SendAndExpectFixture}
import com.example.loadbalancer.http.*

class SendAndExpectTest extends CatsEffectSuite with HttpClientFixture with SendAndExpectFixture {

  val localhost8080 = "localhost:8080"
  val backend: Uri  = Uri.unsafeFromString(localhost8080)
  val emptyRequest  = Request[IO]()

  test("toBackend [Success]") {
    val sendAndExpect = SendAndExpect.toBackend(httpClientHello, emptyRequest)
    val obtained = sendAndExpect(backend)

    assertIO(obtained, "Hello")
  }

  test("toBackend [Failure]") {
    val sendAndExpect = SendAndExpect.toBackend(httpClientRuntimeException, emptyRequest)
    val obtained = sendAndExpect(backend)

    assertIO(obtained, s"server with uri: $localhost8080 is dead")
  }

  test("toHealthCheck [Alive]") {
    val sendAndExpect = SendAndExpect.toHealthCheck(httpClientHello)
    val obtained = sendAndExpect(backend)

    assertIO(obtained, ServerHealthStatus.Alive)
  }

  test("toHealthCheck [Dead due to timeout]") {
    val sendAndExpect = SendAndExpect.toHealthCheck(httpClientTimeoutFailure)
    val obtained = sendAndExpect(backend)

    assertIO(obtained, ServerHealthStatus.Dead)
  }

  test("toHealthCheck [Dead due to exception]") {
    val sendAndExpect = SendAndExpect.toHealthCheck(httpClientRuntimeException)
    val obtained = sendAndExpect(backend)

    assertIO(obtained, ServerHealthStatus.Dead)
  }

}
