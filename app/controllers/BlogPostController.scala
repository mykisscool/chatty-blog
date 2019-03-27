package controllers

import javax.inject._
import play.api.mvc.{AbstractController, ControllerComponents}
import models.{BlogPostModel => Post, BlogCommentModel => Comment}

/** Blog post page controller
  *
  * @param cc Simply meant to bundle together components typically used in a controller
  * @param blogPostModel Model class for blog post(s)
  */
@Singleton
class BlogPostController @Inject()(cc: ControllerComponents, blogPostModel: Post, blogPostCommentModel: Comment) extends AbstractController(cc) {

  def getPost(slug: String) = Action {

    val blogPost = blogPostModel.getPost(slug)
    val title = blogPost.title

    // Get comments
    val parentComments = blogPostCommentModel.getCommentsToPost(blogPost.id)

    // @TODO Comments to comments

    Ok(views.html.post(title, blogPost, parentComments))
  }
}
