package com.lawal

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class TableRendererSpec extends AnyFlatSpec {

  private val COLUMN_DELIMITER = '|'


  "Empty values for renderer" should "return Option.None" in {
    val globalUserStat = new GlobalUserStat()
    val content = TableRenderer.renderTable(0, List(), globalUserStat)
    assert(content.isEmpty)

  }

  "TableRenderer with multiple rows" should " render rows with correct values" in {
    val globalUserStat = new GlobalUserStat()

    val story1 = StoryStat("story1",1,(1 to 4).map( id => {
      UserCommentCount(s"user$id", id)
    }))

    val story2 = StoryStat("story2",2,(1 to 4).map( id => {
      UserCommentCount(s"user$id", id)
    }))


    globalUserStat.addUserComments(story1.userCommentCount)
    globalUserStat.addUserComments(story2.userCommentCount)

    val optContent = TableRenderer.renderTable(4, List(story1,story2), globalUserStat).map(wrapInLineSeparator)
    assert(optContent.isDefined)
    val content = optContent.get
    println( content)
    val expected =
      """
        || Story  |  1st Top Commenter          |  2nd Top Commenter          |  3rd Top Commenter          |  4th Top Commenter          |
        || ------ | --------------------------- | --------------------------- | --------------------------- | --------------------------- |
        || story1 | user1: 1 for story, 2 total | user2: 2 for story, 4 total | user3: 3 for story, 6 total | user4: 4 for story, 8 total |
        || story2 | user1: 1 for story, 2 total | user2: 2 for story, 4 total | user3: 3 for story, 6 total | user4: 4 for story, 8 total |
        |""".stripMargin

    val columnCount = numberOfColumns(expected)
    columnCount shouldBe 4
    content shouldBe (expected)
  }


  "No comment story" should "render empty columns but show title" in {


    val story1 = StoryStat("story1",1,(1 to 4).map( id => {
      UserCommentCount(s"user$id", id)
    }))

    val story2 = StoryStat("story2",2,(1 to 4).map( id => {
      UserCommentCount(s"user$id", id)
    }))

    val story3 = StoryStat("story3",2,List())

    val globalUserStat = new GlobalUserStat()
    globalUserStat.addUserComments(story1.userCommentCount)
    globalUserStat.addUserComments(story2.userCommentCount)

    val optContent = TableRenderer.renderTable(3, List(story1,story2,story3), globalUserStat).map(wrapInLineSeparator)
    assert(optContent.isDefined)

    val content = optContent.get
    println( content)
    val expected =
      """
        || Story  |  1st Top Commenter          |  2nd Top Commenter          |  3rd Top Commenter          |
        || ------ | --------------------------- | --------------------------- | --------------------------- |
        || story1 | user1: 1 for story, 2 total | user2: 2 for story, 4 total | user3: 3 for story, 6 total |
        || story2 | user1: 1 for story, 2 total | user2: 2 for story, 4 total | user3: 3 for story, 6 total |
        || story3 |                             |                             |                             |
        |""".stripMargin

    content shouldBe (expected)
  }

  "Empty List" should "render only headers" in {
    val globalUserStat = new GlobalUserStat()

    val optContent = TableRenderer.renderTable(3, List(), globalUserStat)
    assert(optContent.isDefined)
    val content = optContent.get
    println(content)
   content.split(System.lineSeparator()).length shouldBe 1

  }

  def wrapInLineSeparator(str: String): String = System.lineSeparator() + str + System.lineSeparator()

  def numberOfColumns(str: String): Int = {
    val line = str.split(System.lineSeparator())(2)
    line.count(_ == COLUMN_DELIMITER) - 2 // account for start & end
  }
}
