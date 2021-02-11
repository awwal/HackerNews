package com.lawal

import scala.collection.mutable

class GlobalUserStat {
  private val m = mutable.Map[String, Int]();

  def addUserComments(commentCounts: Seq[UserCommentCount]): Unit = {
    commentCounts.foreach(addUserComment)
  }


  private def addUserComment(userCommentCount: UserCommentCount): Unit = {
    m.updateWith(userCommentCount.userId)({
      case Some(count) => Some(count + userCommentCount.count)
      case None => Some(userCommentCount.count)
    })
  }

  def getUserCount(userId: String): Int = {
    m.getOrElse(userId, 0);
  }
}
