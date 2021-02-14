package com.lawal


import akka.actor.ActorSystem
import com.lawal.ApiModel.{HNStory, ItemList}
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.Failure

object HackerNews extends App with JsonSupport {
  private val logger = LoggerFactory.getLogger(this.getClass)
  implicit val system: ActorSystem = ActorSystem("HackerNews")

  import system.dispatcher

  val TOP_STORIES_COUNT = 30
  val COMMENT_LIMIT = 10
  val httpClient = new QueuedHttpClient()
  val storyList: Future[List[HNStory]] = getTopStories(TOP_STORIES_COUNT)

  val allStoryUserComments: Future[List[StoryStat]] = storyList.flatMap(xs => {
    val stats: List[Future[StoryStat]] = for (x <- xs) yield new StoryStatAggregator(x, COMMENT_LIMIT, httpClient).fetch()
    Future.sequence(stats)
  })


  allStoryUserComments onComplete {
    case util.Success(storyStats) =>
      val globalUserStat = new GlobalUserStat()
      storyStats.foreach(s => globalUserStat.addUserComments(s.userCommentCount))

      val content = TableRenderer.renderTable(COMMENT_LIMIT, storyStats, globalUserStat, 80 * 2)
      if (content.isDefined) {
        println(content.get)
      }
      system.terminate()
    case Failure(exception) =>
      logger.info("Ending with error", exception)
      system.terminate()
  }


  def getTopStories(limit: Int): Future[List[HNStory]] = {
    val itemList = httpClient.getTopHNItems
    val storyList: Future[List[HNStory]] = itemList.flatMap(itemList => {
      logger.debug("Total top item size =" + itemList.size)
      fetchAllLinks(itemList, limit)
    })
    storyList
  }


  def fetchAllLinks(itemList: ItemList, limit: Int): Future[List[HNStory]] = {
    val items = for (item <- itemList.take(limit)) yield httpClient.fetchTopItem(item)
    Future.sequence(items)
      .map(list => {
        //val stories = list.filter(it => it.`type`.equalsIgnoreCase("story"))
        //val top50 = stories.sortBy(_.score)(Ordering[Int].reverse).take(limit)
        val top50 = list.zipWithIndex
          .map { case (el, idx) => HNStory(el, idx) }
        logger.info(s"Done fetching top $limit  stories")
        top50
      })

  }

}

