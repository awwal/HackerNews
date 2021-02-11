package com.lawal

import com.lawal.ApiModel.HNStory
case class UserCommentCount(userId: String, count: Int)
case class StoryStat(story: HNStory, userCommentCount: List[UserCommentCount])


