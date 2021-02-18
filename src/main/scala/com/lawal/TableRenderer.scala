package com.lawal

import net.steppschuh.markdowngenerator.table.Table


object TableRenderer {

  def renderTable(numberOfColumns: Int, storyStats: Seq[StoryStat], globalUserStat: GlobalUserStat): Option[String] = {
    if (numberOfColumns == 0 && storyStats.isEmpty) {
      return None
    }
    val tableBuilder = new Table.Builder()

    val headers = createHeaders(numberOfColumns)
    tableBuilder.addRow(headers: _*)
    storyStats.sortBy(_.rank).foreach(stat => {
      val row = createRow(stat.storyTitle, stat.userCommentCount.toList.take(numberOfColumns), headers.size, globalUserStat)
      if (row.nonEmpty) {
        tableBuilder.addRow(row: _*)
      }
    })

    val table = tableBuilder.build()
    val content = table.serialize()
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
      case 0 => "Story"
      case i => s"${ordinal(i)} Top Commenter"
    }
  }

}
