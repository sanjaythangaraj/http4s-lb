package com.example.loadbalancer.services

import org.http4s.*
import cats.implicits.*
import munit.FunSuite

import com.example.loadbalancer.errors.parsing.*

class ParseUriTest extends FunSuite {
  val parseUri = ParseUri.Impl

  test("try parsing valid URI and return Right(Uri(...))") {
    val uri      = "0.0.0.0/8080"
    val obtained = parseUri(uri)
    val expected = Uri.unsafeFromString(uri).asRight[InvalidUri]

    assertEquals(obtained, expected)
  }

  test("try parsing invalid URI and return Left(InvalidUri(...))") {
    val uri      = "invalid uri"
    val obtained = parseUri(uri)
    val expected = InvalidUri(uri).asLeft[Uri]

    assertEquals(obtained, expected)
  }
}
