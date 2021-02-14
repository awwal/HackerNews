package com.lawal

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.lawal.ApiModel.{CommentItem, HNItem, TopItem}
import spray.json.DefaultJsonProtocol

object ApiModel {
  type ItemList = List[Int]
  case class HNItem(title: String, by: Option[String], kids: Option[ItemList], `type`: String, score: Int)

  case class CommentItem(by: Option[String], kids: Option[ItemList])

  case class TopItem(title: String, kids: Option[ItemList], `type`: String, score: Int)

  case class HNStory(title: String,rank:Int, commentIds: List[Int])

  object HNStory {
    def apply(item: TopItem, rank: Int): HNStory = {
      HNStory(item.title, rank, if (item.kids.isEmpty) Nil else item.kids.get)
    }
  }
}
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol  {
  implicit val itemFormat = jsonFormat5(HNItem)
  implicit val topItemFormat = jsonFormat4(TopItem)
  implicit  val commentItemFormat = jsonFormat2(CommentItem)
}
