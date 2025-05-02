package com.example.loadbalancer.services

import munit.FunSuite
import org.http4s.*

class AddRequestPathToBackendUrlTest extends FunSuite {

  val impl       = AddRequestPathToBackendUrl.Impl
  val backendUrl = "http://localhost:8082"

  test("add '/items/1 to backendurl'") {
    val obtained = impl(backendUrl, Request(uri = Uri.unsafeFromString("localhost:8080/items/1")))
    val expected = "http://localhost:8082/items/1"

    assertEquals(obtained, expected)
  }

  test("since request doesn't have path just return backendUrl") {
    val obtained = impl(backendUrl, Request(uri = Uri.unsafeFromString("localhost:8080")))
    val expected = "http://localhost:8082"
  }

}
