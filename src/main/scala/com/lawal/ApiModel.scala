package com.lawal

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.lawal.ApiModel.{CommentItem, HNItem, TopItem}
import spray.json.{DefaultJsonProtocol, JsNull, JsObject, JsValue, RootJsonFormat}

object ApiModel {
  type ItemList = List[Int]

  case class HNItem(title: String, by: Option[String], kids: Option[ItemList], `type`: String, score: Int)

  case class CommentItem(by: Option[String], kids: Option[ItemList])

  case class TopItem(title: String, kids: Option[ItemList], `type`: String, score: Int)

  case class HNStory(title: String, rank: Int, commentIds: List[Int])

  object HNStory {
    def apply(item: TopItem, rank: Int): HNStory = {
      HNStory(item.title, rank, if (item.kids.isEmpty) Nil else item.kids.get)
    }
  }

}

object TopItemJsonProtocol extends DefaultJsonProtocol {

  implicit object OptionItemJson extends RootJsonFormat[Option[TopItem]] {
    def write(c: Option[TopItem]): JsValue = c match {
      case Some(t) => jsonFormat4(TopItem).write(t)
      case None => JsNull
    }

    def read(value: JsValue): Option[TopItem] = value match {
      case JsObject(_) =>
        val item = jsonFormat4(TopItem).read(value)
        Some(item)
      case _ => None
    }
  }

}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val itemFormat = jsonFormat5(HNItem)
  implicit val commentItemFormat = jsonFormat2(CommentItem)
}
