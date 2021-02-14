package com.lawal

import org.scalatest.flatspec.AnyFlatSpec

import java.util.UUID
import scala.collection.mutable.Stack

class GlobalUserStatSpec extends AnyFlatSpec {

  "A empty stat" should "return 0 for missing user id" in {
    val globalUserStat = new GlobalUserStat()
    assert(globalUserStat.getUserCount(UUID.randomUUID().toString) === 0)
  }

  "An non-empty stat" should "return correct user count" in {
    val globalUserStat = new GlobalUserStat()
    val userId = UUID.randomUUID().toString

    globalUserStat.addUserComments(Seq(
      UserCommentCount(userId, 1),
      UserCommentCount(userId, 2)
    ))
    assert(globalUserStat.getUserCount(userId) === 3)
    assert(globalUserStat.getUserCount(UUID.randomUUID().toString) === 0)
  }
}