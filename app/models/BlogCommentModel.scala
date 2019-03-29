package models

import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import play.api.db._
import play.api.Configuration
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
  */
@Singleton
class BlogCommentModel @Inject()(dbapi: DBApi, configuration: Configuration) {

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
}
