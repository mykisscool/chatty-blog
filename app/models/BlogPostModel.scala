package models

import java.util.Date
import java.text.SimpleDateFormat
import javax.inject.{Inject, Singleton}
import play.api.db._
import play.api.Configuration
import anorm.{Row, RowParser, SQL, SimpleSql, SqlParser, ~}
import anorm.SqlParser.{date, int, str}

/** Case class for a blog post
  *
  * @param id Post ID
  * @param fullname Author name
  * @param nickname Author nickname
  * @param title Post title
  * @param slug Post URL segment
  * @param image Post featured image
  * @param content Post contents
  * @param created_at When the post was published
  */
case class Post(id: Int,
                fullname: String,
                nickname: String,
                title: String,
                slug: String,
                image: String,
                content: String,
                created_at: String)

/** Methods contain SQL statements that retrieve blog posts
  *
  * @param dbapi Database interface
  * @param configuration application.conf configuration
  */
@Singleton
class BlogPostModel @Inject()(dbapi: DBApi, configuration: Configuration) {

  private val db = dbapi.database("default")

  /** Helper method for pagination; returns total number of blog posts
    *
    * @return Number of published blog posts
    */
  def getAllPostCount: Int = {
    db.withConnection { implicit c =>
      SQL("SELECT COUNT(*) FROM post").as(SqlParser.scalar[Int].single)
    }
  }

  /** Truncates an entire blog post to 30 words to act as a snippet
    *
    * @param content Blog post
    * @return Blog snippet
    */
  private def truncateContent(content: String): String =
    content.split(" ").map(_.trim).toList.take(30).mkString(" ").replaceAll(",$", "")

  /** Formats the published date
    *
    * @param published Published date
    * @return Pretty-formatted published date
    */
  private def prettyDate(published: Date): String = new SimpleDateFormat("MMM dd").format(published)

  /** Parser for post table rows
    *
    * @param isPost Whether or not to truncate the content (snippet) for post loop
    * @return A post
    */
  private def parser(isPost: Boolean = false): RowParser[Post] = {
    int("id") ~
    str("fullname") ~
    str("nickname") ~
    str("title") ~
    str("slug") ~
    str("image") ~
    str("content") ~
    date("created_at") map {

    case id ~ fullname ~ nickname ~ title ~ slug ~ image ~ content ~ created_at =>
      Post(id, fullname, nickname, title, slug, image, if (isPost) content else truncateContent(content), prettyDate(created_at))
    }
  }

  /** Returns a list of blog posts for a specific page
    *
    * @param page Page number of blog posts
    * @return A list of blog posts
    */
  def getPosts(page: Int): List[Post] = {

    db.withConnection { implicit c =>

      val postsPerPage: Int = configuration.get[Int]("my.postsPerPage")

      val q = SQL(
        """
          |SELECT p.id,
          |       fullname,
          |       nickname,
          |       title,
          |       slug,
          |       image,
          |       content,
          |       created_at
          |FROM post p
          |INNER JOIN user u ON p.user_id = u.id
          |ORDER BY created_at DESC, p.id DESC
          |LIMIT {start}, {end}
        """.stripMargin
      ).on("start" -> (page -1) * postsPerPage, "end" -> postsPerPage)

      q.as(parser().*)
    }
  }

  /** Returns a blog post
    *
    * @param slug Blog post URL segment
    * @return THE blog post
    */
  def getPost(slug: String): Post = {

    db.withConnection { implicit c =>

      val q: SimpleSql[Row] = SQL(
        """
          |SELECT p.id,
          |       fullname,
          |       nickname,
          |       title,
          |       slug,
          |       image,
          |       content,
          |       created_at
          |FROM post p
          |INNER JOIN user u ON p.user_id = u.id
          |WHERE slug = {slug}
        """.stripMargin
      ).on("slug" -> slug)

      q.as(parser(true).single)
    }
  }
}
