package models

import scala.io.Source
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import play.api.db._
import play.api.Configuration
import play.api.Environment
import anorm.{RowParser, SQL, ~}
import anorm.SqlParser.{date, int, str}

/** Case class for a blog post comment
  *
  * @param comment_id The ID of the blog post comment
  * @param commented_on_comment_id The ID of the blog post comment commented on (or NULL)
  * @param fullname Comment author name
  * @param nickname Comment author nickname
  * @param comment The comment
  * @param created_at When the comment was made
  */
case class Comment(comment_id: Int,
                   commented_on_comment_id: Int,
                   fullname: String,
                   nickname: String,
                   comment: String,
                   created_at: String)

/** Methods contain SQL statements that retrieve blog posts comments
  *
  * @param dbapi Database interface
  * @param configuration application.conf configuration
  * @param env Captures concerns relating to the classloader and the filesystem for the application
  */
@Singleton
class BlogCommentModel @Inject()(dbapi: DBApi, configuration: Configuration, env: Environment) {

  private val db = dbapi.database("default")

  /** Formats the published date
    *
    * @param commented Published date
    * @return The difference between the current timestamp and when the comment was made
    */
  private def prettyDate(commented: Date): String = {

    val dbHoursOffset: Int = configuration.get[Int]("my.dbHoursOffset")
    val rightNow: Date = new Date
    val hoursInMs: Long = 60 * 60 * 1000
    val adjustedCommentDateTime =  new Date(commented.getTime + hoursInMs * dbHoursOffset)

    val diffInSeconds = TimeUnit.SECONDS.convert(rightNow.getTime - adjustedCommentDateTime.getTime, TimeUnit.MILLISECONDS)
    val diffInMinutes = TimeUnit.MINUTES.convert(rightNow.getTime - adjustedCommentDateTime.getTime, TimeUnit.MILLISECONDS)
    val diffInHours = TimeUnit.HOURS.convert(rightNow.getTime - adjustedCommentDateTime.getTime, TimeUnit.MILLISECONDS)
    val diffInDays = TimeUnit.DAYS.convert(rightNow.getTime - adjustedCommentDateTime.getTime, TimeUnit.MILLISECONDS)

    if (diffInSeconds < 60) {
      s"$diffInSeconds second" + (if (diffInSeconds > 1) "s" else "") + " ago"
    }
    else if (diffInMinutes < 60) {
      s"$diffInMinutes minute" + (if (diffInMinutes > 1) "s" else "") + " ago"
    }
    else if (diffInHours < 24) {
      s"$diffInHours hour" + (if (diffInHours > 1) "s" else "") + " ago"
    }
    else if (diffInDays < 30)
      s"$diffInDays day" + (if (diffInDays > 1) "s" else "") + " ago"
    else {
      "A very long time ago"
    }
  }

  /** Parser for comments
    *
    * @return A comment
    */
  private def parser: RowParser[Comment] = {
    int("comment_id") ~
    int("commented_on_comment_id") ~
    str("fullname") ~
    str("nickname") ~
    str("comment") ~
    date("created_at") map {

      case comment_id ~ commented_on_comment_id ~ fullname ~ nickname ~ comment ~ created_at =>
        Comment(comment_id, commented_on_comment_id, fullname, nickname, comment, prettyDate(created_at))
    }
  }

  /** Replaces potential bad words in a comment with asterisks
    * Source of bad words: https://github.com/RobertJGabriel/Google-profanity-words
    *
    * @param comment The comment made to the blog post/comment
    * @return The same comment with bad words scrubbed out and supplemented with asterisks
    */
  private def removeBadWords(comment: String): String = {
    val badWordsFile = env.getClass.getResource("/resources/badwords.txt").getPath
    Source.fromFile(badWordsFile).getLines.mkString("\\b(", "|", ")\\b").r.replaceAllIn(comment, "****")
  }

  /** Returns comments made to a blog post
    *
    * @param id Blog post ID
    * @return A list of all comments made to a blog post
    */
  def getCommentsToPost(id: Int): List[Comment] = {

    db.withConnection { implicit c =>

      val q = SQL(
        """
          |SELECT c.id AS comment_id,
          |       IFNULL(comment_id, 0) AS commented_on_comment_id,
          |       fullname,
          |       nickname,
          |       comment,
          |       c.created_at
          |FROM comment c
          |INNER JOIN user u ON c.user_id = u.id
          |WHERE post_id = {id}
          |ORDER BY commented_on_comment_id, created_at
        """.stripMargin
      ).on("id" -> id)

      q.as(parser.*)
    }
  }

  /** Adds a comment to the database
    *
    * @param postid The ID of the blog post
    * @param userid The ID of the user who commented on the blog post
    * @param commentid The ID of the comment (if a comment was made to another comment as opposed to directly commenting on the post)
    * @param comment The comment made to the blog post/comment
    * @return The new, unique ID of the comment
    */
  def addComment(postid: Int, userid: Int, commentid: Int, comment: String): Option[Long] = {

    val id: Option[Long] = db.withConnection { implicit c =>

      val q = SQL(
        """
          |INSERT INTO comment (post_id, user_id, comment_id, comment)
          |values ({postid}, {userid}, {commentid}, {comment})
        """.stripMargin
      ).on("postid" -> postid, "userid" -> userid, "commentid" -> commentid, "comment" -> removeBadWords(comment))

      q.executeInsert()
    }

    id
  }
}
