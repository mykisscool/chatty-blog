package controllers

import javax.inject._
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.Configuration
import models.{BlogPostModel => Post}

/** Home Controller (homepage cycling through blog posts)
  *
  * @param cc
  * @param blogPostModel
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, blogPostModel: Post, configuration: Configuration) extends AbstractController(cc) {

  def index(page: Int) = Action {

    val title = "Newest Posts"
    val blogPosts = blogPostModel.getPosts(page)

    // Determine simple previous/next pagination
    val blogCount: Int = blogPostModel.getAllPostCount
    val postsPerPage: Int = configuration.get[Int]("my.postsPerPage")

    val prevLink: Boolean = if (page == 1) false else true
    val nextLink: Boolean = if ((blogCount.toDouble / page.toDouble) <= postsPerPage) false else true

    Ok(views.html.index(title, page, blogPosts, prevLink, nextLink))
  }
}
