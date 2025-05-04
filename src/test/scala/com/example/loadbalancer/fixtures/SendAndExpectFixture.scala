package com.example.loadbalancer.fixtures

import cats.effect.*

import com.example.loadbalancer.services.*

trait SendAndExpectFixture {
  val BackendSuccessTest: SendAndExpect[String] = _ => IO("Success")
}
