package com.example.loadbalancer.services

import org.http4s.*
import cats.implicits.*

import com.example.loadbalancer.errors.parsing.*

trait ParseUri {
  def apply(uri: String): Either[InvalidUri, Uri]
}

object ParseUri {
  object Impl extends ParseUri {

    override def apply(uri: String): Either[InvalidUri, Uri] =
      Uri
        .fromString(uri)
        .leftMap(_ => InvalidUri(uri))
  }
}
