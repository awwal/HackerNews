package com.lawal

import com.lawal.ApiModel.{CommentItem, HNStory}
import org.slf4j.LoggerFactory

import scala.collection.View
import scala.concurrent.{ExecutionContext, Future}

class StoryStatAggregator(story: HNStory, commentLimit: Int, clientSDK: HackerNewsSDK)(implicit executionContext: ExecutionContext) {


  private val logger = LoggerFactory.getLogger(this.getClass)

  def fetch(): Future[StoryStat] = {

    logger.info(s"Fetching comment for story [${story.title}] rank ${story.rank}")
    val fCommentList: List[Future[List[CommentItem]]] = for (commentId <- story.commentIds) yield walkCommentLink(commentId)
    val allComments = Future.sequence(fCommentList).map(list => list.flatten)
    val commenters = allComments.map(list => list.flatMap(it => it.by))
    val eventualStoryStat: Future[StoryStat] = commenters.map(userlist => toUserStat(story, userlist))
    eventualStoryStat

  }

  private def toUserStat(story: HNStory, userList: List[String]): StoryStat = {
    val userToCommentCount: View[(String, Int)] = userList.groupBy(identity).view.mapValues(_.size);
    val top = userToCommentCount.toList.sortBy(_._2)(Ordering[Int].reverse)
      .take(commentLimit)
      .map(t => UserCommentCount(t._1, t._2))
    StoryStat(story.title, story.rank, top)
  }


  //@tailrec --todo
  private
  def walkCommentLink(commentId: Int): Future[List[CommentItem]] = {
    logger.debug(s"Currently on comment $commentId in story [${story.title}]")
    clientSDK.fetchComment(commentId).flatMap { comment =>
      val f: Future[List[CommentItem]] = Future(List(comment))
      comment.kids match {
        case Some(kids) if kids.nonEmpty =>
          val childFutures: List[Future[List[CommentItem]]] = for (commentId <- comment.kids.get) yield walkCommentLink(commentId)
          val fs = Future.sequence(childFutures).map(list => list.flatten)
          mergeFutureLists(fs, f)
        case None =>
          f
      }
    }
  }

  private def mergeFutureLists[X](xs: Future[List[X]], ys: Future[List[X]]): Future[List[X]] = {
    for {
      x <- xs
      y <- ys
    } yield (x ::: y)
  }
}
