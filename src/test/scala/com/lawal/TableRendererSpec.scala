package com.lawal

import com.lawal.ApiModel.HNStory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import java.util.UUID

class TableRendererSpec extends AnyFlatSpec {

  private val COLUMN_DELIMITER = '│'

  "Empty values for renderer" should "return Option.None" in {
    val globalUserStat = new GlobalUserStat()
    val content = TableRenderer.renderTable(0, List(), globalUserStat, 10)
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

    val optContent = TableRenderer.renderTable(4, List(story1,story2), globalUserStat, 100).map(wrapInLineSeparator)
    assert(optContent.isDefined)
    val content = optContent.get
    println( content)
    val expected =
      """
        |┌───────────────────┬───────────────────┬───────────────────┬───────────────────┬──────────────────┐
        |│Story              │1st Top Commenter  │2nd Top Commenter  │3rd Top Commenter  │4th Top Commenter │
        |├───────────────────┼───────────────────┼───────────────────┼───────────────────┼──────────────────┤
        |│story1             │user1: 1 for story,│user2: 2 for story,│user3: 3 for story,│user4:    4    for│
        |│                   │2 total            │4 total            │6 total            │story, 8 total    │
        |├───────────────────┼───────────────────┼───────────────────┼───────────────────┼──────────────────┤
        |│story2             │user1: 1 for story,│user2: 2 for story,│user3: 3 for story,│user4:    4    for│
        |│                   │2 total            │4 total            │6 total            │story, 8 total    │
        |└───────────────────┴───────────────────┴───────────────────┴───────────────────┴──────────────────┘
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
//    globalUserStat.addUserComments(story3.userCommentCount)

    val optContent = TableRenderer.renderTable(3, List(story1,story2,story3), globalUserStat, 80).map(wrapInLineSeparator)
    assert(optContent.isDefined)

    val content = optContent.get
    println( content)
    val expected =
      """
        |┌───────────────────┬───────────────────┬───────────────────┬──────────────────┐
        |│Story              │1st Top Commenter  │2nd Top Commenter  │3rd Top Commenter │
        |├───────────────────┼───────────────────┼───────────────────┼──────────────────┤
        |│story1             │user1: 1 for story,│user2: 2 for story,│user3:    3    for│
        |│                   │2 total            │4 total            │story, 6 total    │
        |├───────────────────┼───────────────────┼───────────────────┼──────────────────┤
        |│story2             │user1: 1 for story,│user2: 2 for story,│user3:    3    for│
        |│                   │2 total            │4 total            │story, 6 total    │
        |├───────────────────┼───────────────────┼───────────────────┼──────────────────┤
        |│story3             │                   │                   │                  │
        |└───────────────────┴───────────────────┴───────────────────┴──────────────────┘
        |""".stripMargin

    content shouldBe (expected)
  }

  "Empty List" should "render only headers" in {
    val globalUserStat = new GlobalUserStat()

    val optContent = TableRenderer.renderTable(3, List(), globalUserStat, 60).map(wrapInLineSeparator)
    assert(optContent.isDefined)
    val content = optContent.get
    val expected =
      """
        |┌──────────────┬──────────────┬──────────────┬─────────────┐
        |│Story         │1st        Top│2nd        Top│3rd       Top│
        |│              │Commenter     │Commenter     │Commenter    │
        |└──────────────┴──────────────┴──────────────┴─────────────┘
        |""".stripMargin

    val columnCount = numberOfColumns(expected)
    columnCount shouldBe 3
    content shouldBe (expected)
  }

  def wrapInLineSeparator(str: String): String = System.lineSeparator() + str + System.lineSeparator()

  def numberOfColumns(str: String): Int = {
    val line = str.split(System.lineSeparator())(2)
    line.count(_ == COLUMN_DELIMITER) - 2 // account for start & end
  }
}
