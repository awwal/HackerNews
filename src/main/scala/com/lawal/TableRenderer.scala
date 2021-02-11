package com.lawal

import de.vandermeer.asciitable.AsciiTable

object TableRenderer {

  def print(commentColumnSize: Int, storyStats: List[StoryStat], globalUserStat: GlobalUserStat): Unit = {
    val at = new AsciiTable
    at.addRule()
    val headers: Seq[String] = createHeaders(commentColumnSize)
    at.addRow(headers: _*)
    at.addRule()
    storyStats.sortBy(_.story.rank).foreach(stat => {
      val row: Seq[String] = createRow(stat.story.title, stat.userCommentCount, headers.size, globalUserStat)
      at.addRow(row: _*)
      at.addRule()
    })
    println(at.render(80 * 2))
  }

  def createRow(title: String, userCommentCount: List[UserCommentCount], rowSize: Int,
                globalUserStat: GlobalUserStat): Seq[String] = {


    val comments = userCommentCount.take(rowSize).map(uc => {
      val total = globalUserStat.getUserCount(uc.userId)
      s"${uc.userId}: ${uc.count} for story, ${total} total"
    })
    val row = title :: comments
    val fill = rowSize - row.size
    row ++ Array.fill(fill)("")
  }

  private def ordinal(i: Int): String = {
    lazy val suffixes = Array[String]("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
    i % 100 match {
      case 11 | 12 | 13 => i + "th"
      case _ =>
        i + suffixes(i % 10)
    }
  }

  private def createHeaders(limit: Int): Seq[String] = {
    (0 to limit).map {
      i => if (i == 0) "Story" else s" ${ordinal(i)} Top Commenter"
    }
  }

}
