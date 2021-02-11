package com.lawal

import akka.actor.ActorSystem
import com.lawal.ApiModel.{CommentItem, HNStory}
import org.slf4j.LoggerFactory

import scala.collection.View
import scala.concurrent.Future

class StoryStatCollector(story: HNStory, commentLimit: Int)(implicit val system: ActorSystem) {

  import system.dispatcher
//  private val logger = LoggerFactory.getLogger(this.getClass)
  private val pooledHttpClient = new PooledHttpClient()

  def fetch(): Future[StoryStat] = {

//    logger.info(s"Fetching comment for story ${story.title} rank ${story.rank}")
    val items: List[Future[List[CommentItem]]] = for (commentId <- story.commentIds) yield walkCommentLink(commentId)

    val f: Future[List[CommentItem]] = Future.sequence(items).map(list => list.flatten)
    val fl: Future[List[String]] = f.map(list => list.flatMap(it => it.by))
    val eventualStoryStat: Future[StoryStat] = fl.map(userlist => getUserStats(story, userlist))
    eventualStoryStat

  }

  private
  def getUserStats(story: HNStory, userList: List[String]): StoryStat = {
    val userToCommentCount: View[(String, Int)] = userList.groupBy(identity).view.mapValues(_.size);
    val top=  userToCommentCount.toList.sortBy(_._2)(Ordering[Int].reverse)
      .take(commentLimit)
      .map(t => UserCommentCount(t._1, t._2))
    StoryStat(story, top);
  }


  //  @tailrec
  private
  def walkCommentLink(commentId: Int): Future[List[CommentItem]] = {
//    logger.debug(s"Currently on comment $commentId in story ${story.title}")
    pooledHttpClient.fetchComment(commentId) flatMap ((comment: CommentItem) => {
      if (comment.kids.isDefined) {
        val items: List[Future[List[CommentItem]]] = for (commentId <- comment.kids.get) yield walkCommentLink(commentId)
        val f: Future[List[CommentItem]] = Future.sequence(items).map(list => list.flatten)
        mergeFutureLists(f, Future(List(comment)))
      }
      else {
        val f: Future[List[CommentItem]] = Future(List(comment))
        f
      }
    })
  }

  private
  def mergeFutureLists[X](xs: Future[List[X]], ys: Future[List[X]]): Future[List[X]] = {
    for {
      x <- xs
      y <- ys
    } yield (x ::: y)
  }
}
