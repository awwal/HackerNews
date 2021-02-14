package com.lawal

import com.lawal.ApiModel.{CommentItem, HNStory, ItemList, TopItem}
import org.scalatest.flatspec.AsyncFlatSpec

import scala.concurrent.Future
import scala.language.postfixOps

class StoryStatAggregatorSpec extends AsyncFlatSpec {


  it should "should return all Comments for a story" in {

    val maxComment = Int.MaxValue
    val linkIds = (1 to 10).toList
    val story = HNStory("test", 1, linkIds);
    val stat = new StoryStatAggregator(story, maxComment, MockHackerNewsSDK)
    val res: Future[StoryStat] = stat.fetch()
    res.map(storyStat => assert(storyStat.userCommentCount.size == 12))

  }

  object MockHackerNewsSDK extends HackerNewsSDK {
    override def getTopHNItems: Future[ItemList] = {
      Future(List[Int]())
    }

    override def fetchComment(linkId: Int): Future[ApiModel.CommentItem] = Future {
      val user = Some(s"user$linkId")
      if (linkId == 1) {
        CommentItem(user, Some(List(11, 12)))
      }
      else {
        CommentItem(user, Some(List[Int]()))
      }
    }

    override def fetchTopItem(linkId: Int): Future[ApiModel.TopItem] = Future {
      TopItem("", None, "", 1)
    }
  }

}
