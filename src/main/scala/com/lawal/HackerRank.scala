package com.lawal


import akka.actor.ActorSystem
import com.lawal.ApiModel.{HNStory, ItemList}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object HackerRank extends App with JsonSupport {


  implicit val system: ActorSystem = ActorSystem("HackerNews")

  import system.dispatcher

  val COMMENT_LIMIT = 5
  val TOP_STORIES_COUNT = 3
  private val pooledHttpClient = new PooledHttpClient()
  val storyList: Future[List[HNStory]] = getTopStories(TOP_STORIES_COUNT)

  val allStoryUserComments: Future[List[StoryStat]] = storyList.flatMap(xs => {
    val items: List[Future[StoryStat]] = for (x <- xs) yield new StoryStatCollector(x, COMMENT_LIMIT).fetch()
    val sf: Future[List[StoryStat]] = Future.sequence(items)
    sf
  })


  allStoryUserComments onComplete {
    case util.Success(storyStats) =>
      val globalUserStat = new GlobalUserStat()
      storyStats.foreach(s => globalUserStat.addUserComments(s.userCommentCount))
      TableRenderer.print(COMMENT_LIMIT, storyStats, globalUserStat)
       system.terminate()
    case Failure(exception) =>
      system.terminate()
  }


  def getTopStories(limit: Int): Future[List[HNStory]] = {
    val responseFuture: Future[ItemList] = pooledHttpClient.getTopHNItems
    val storyList: Future[List[HNStory]] = responseFuture.flatMap(itemList => {
      fetchAllLinks(itemList, limit)
    })
    storyList
  }


  def fetchAllLinks(itemList: ItemList, limit: Int): Future[List[HNStory]] = {
    println(itemList.size)
    val items = for (item <- itemList.take(limit)) yield pooledHttpClient.fetchTopItem(item)
    Future.sequence(items)
      .map(list => {
        //val stories = list.filter(it => it.`type`.equalsIgnoreCase("story"))
        //val top50 = stories.sortBy(_.score)(Ordering[Int].reverse).take(limit)
        val top50 = list.zipWithIndex
          .map { case (el, idx) => HNStory(el, idx) }
        println("Done fetching top stories")
        top50
      })

  }

//  private
//  def printResult[T](fs: Future[List[T]]): Unit = {
//    fs onComplete {
//      case Success(xs) => xs.foreach(println)
//    }
//  }
}

