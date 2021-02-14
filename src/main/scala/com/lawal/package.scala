package com

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.concurrent.Promise

package object lawal {
  type HttpReqRes = (HttpRequest, Promise[HttpResponse])
}
