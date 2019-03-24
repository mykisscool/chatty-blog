package controllers

import javax.inject._
import play.api.mvc.{AbstractController, ControllerComponents}
import models.{BlogPostModel => Post}

/** Blog post page controller
  *
  * @param cc Simply meant to bundle together components typically used in a controller
  * @param blogPostModel Model class for blog post(s)
  */
@Singleton
class BlogPostController @Inject()(cc: ControllerComponents, blogPostModel: Post) extends AbstractController(cc) {

  def getPost(slug: String) = Action {

    val blogPost = blogPostModel.getPost(slug)
    val title = blogPost.title

    Ok(views.html.post(title, blogPost))
  }
}
