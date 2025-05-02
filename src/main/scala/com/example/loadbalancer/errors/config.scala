package com.example.loadbalancer.errors

object config {

  type InvalidConfig = InvalidConfig.type

  case object InvalidConfig extends Throwable {
    override def getMessage: String =
      "Invalid post or host, please fix Config"
  }
}
