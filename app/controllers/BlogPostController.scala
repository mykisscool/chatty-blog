package controllers

import javax.inject._
import play.api.mvc.{AbstractController, ControllerComponents}
import models.{BlogPostModel => Post, BlogCommentModel => Comment}

/** Blog post page controller- displays a blog post
  *
  * @param cc Simply meant to bundle together components typically used in a controller
  * @param blogPostModel Model class for blog posts
  * @param blogPostCommentModel Model class for blog post comments
  */
@Singleton
class BlogPostController @Inject()(cc: ControllerComponents, blogPostModel: Post, blogPostCommentModel: Comment) extends AbstractController(cc) {

  def getPost(slug: String) = Action {

    val blogPost = blogPostModel.getPost(slug)
    val title = blogPost.title
    val comments = blogPostCommentModel.getCommentsToPost(blogPost.id)

    Ok(views.html.post(title, blogPost, comments))
  }
}
