package com.lawal

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, MediaTypes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.RetrySupport
import akka.stream.scaladsl.{Sink, Source, SourceQueueWithComplete}
import akka.stream.{OverflowStrategy, QueueOfferResult}
import com.lawal.ApiModel.{CommentItem, ItemList, TopItem}
import org.slf4j.LoggerFactory

import javax.xml.ws.Response
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class QueuedHttpClient(implicit system: ActorSystem) extends JsonSupport with HackerNewsSDK {
  type HttpReqRes = (HttpRequest, Promise[HttpResponse])

  implicit val scheduler: akka.actor.Scheduler = system.scheduler

  import system.dispatcher

  val QueueSize = 1000

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val poolClientFlow = Http().cachedHostConnectionPoolHttps[Promise[HttpResponse]]("hacker-news.firebaseio.com")
  val queue: SourceQueueWithComplete[HttpReqRes] = Source.queue[HttpReqRes](QueueSize, OverflowStrategy.dropNew)
    .via(poolClientFlow)
    .to(Sink.foreach({
      case (Success(resp), promise) => promise.success(resp)
      case (Failure(e), p) => p.failure(e)
    }))
    .run()


  def queueRequest(request: HttpRequest): Future[HttpResponse] = {
    offerWithRetry(request)
  }

  private def offerRequest(request: HttpRequest): Future[HttpResponse] = {

    val responsePromise = Promise[HttpResponse]()
    queue.offer(request -> responsePromise).flatMap {
      case QueueOfferResult.Enqueued => {
        responsePromise.future
      }
      case QueueOfferResult.Dropped => {
        Future.failed(new RuntimeException("Queue overflowed. Try again later."))
      }
      case QueueOfferResult.Failure(ex) => Future.failed(ex)
      case QueueOfferResult.QueueClosed => Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
    }
  }


  private def offerWithRetry(req: HttpRequest): Future[HttpResponse] = {
    logger.debug("Retrying " + req.getUri())
    RetrySupport.retry[HttpResponse](
      attempt = () => offerRequest(req),
      attempts = 10,
      minBackoff = 1.seconds,
      maxBackoff = 2.seconds,
      randomFactor = 0.5
    )
  }


  def getTopHNItems: Future[ItemList] = {
    val request = HttpRequest(uri = "/v0/topstories.json", headers = List(Accept(MediaTypes.`application/json`)))
    queueRequest(request).flatMap(res => Unmarshal(res.entity).to[ItemList])
  }

  def fetchComment(linkId: Int): Future[CommentItem] = {
    val request = HttpRequest(uri = s"/v0/item/${linkId}.json", headers = List(Accept(MediaTypes.`application/json`)))
    queueRequest(request).flatMap(res => Unmarshal(res.entity).to[CommentItem])

  }
  def fetchTopItem(linkId: Int): Future[TopItem] = {
    val request = HttpRequest(uri = s"/v0/item/${linkId}.json", headers = List(Accept(MediaTypes.`application/json`)))
    queueRequest(request).flatMap(res => Unmarshal(res.entity).to[TopItem])
  }
}
