package com.lawal

import com.lawal.ApiModel.{CommentItem, ItemList, TopItem}

import scala.concurrent.Future

trait HackerNewsSDK {
  def getTopHNItems: Future[ItemList];
  def fetchComment(linkId: Int): Future[CommentItem];
  def fetchTopItem(linkId: Int): Future[TopItem];
}
