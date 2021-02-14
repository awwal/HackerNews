package com.lawal

import de.vandermeer.asciitable.AsciiTable

object TableRenderer {

  def renderTable(numberOfColumns: Int, storyStats: Seq[StoryStat], globalUserStat: GlobalUserStat, tableWidth: Int): Option[String] = {
    if (numberOfColumns == 0 && storyStats.isEmpty) {
      return None
    }
    val at = new AsciiTable
    at.addRule()
    val headers = createHeaders(numberOfColumns)
    at.addRow(headers: _*)
    at.addRule()
    storyStats.sortBy(_.rank).foreach(stat => {
      val row = createRow(stat.storyTitle, stat.userCommentCount.toList.take(numberOfColumns), headers.size, globalUserStat)
      at.addRow(row: _*)
      at.addRule()
    })

    val content = at.render(tableWidth)
    Some(content)
  }

  def createRow(title: String, userCommentCount: List[UserCommentCount], rowSize: Int,
                globalUserStat: GlobalUserStat): Seq[String] = {


    val comments = userCommentCount.take(rowSize).map(uc => {
      val total = globalUserStat.getUserCount(uc.userId)
      s"${uc.userId}: ${uc.count} for story, ${total} total"
    })
    val row = title :: comments
    val left = rowSize - row.size
    row ++ Array.fill(left)("")
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
