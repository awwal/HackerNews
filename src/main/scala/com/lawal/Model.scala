package com.lawal

case class UserCommentCount(userId: String, count: Int)
case class StoryStat(storyTitle: String, rank :Int , userCommentCount: Seq[UserCommentCount])


