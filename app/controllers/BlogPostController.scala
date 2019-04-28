package controllers

import javax.inject._
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.api.libs.json._
import models.{BlogCommentModel => Comment, BlogPostModel => Post}

/** Blog post page controller- displays a blog post
  *
  * @param cc Simply meant to bundle together components typically used in a controller
  * @param blogPostModel Model class for blog posts
  * @param blogPostCommentModel Model class for blog post comments
  * @param userAction Checks to make sure the request is performed by an authenticated user
  */
@Singleton
class BlogPostController @Inject()(cc: ControllerComponents,
                                   blogPostModel: Post,
                                   blogPostCommentModel: Comment,
                                   userAction: UserAction) extends AbstractController(cc) {

  /** Display a blog post
    *
    * @param slug URI segment that uniquely identifies a blog post
    * @return The blog post view
    */
  def getPost(slug: String) = Action { implicit request: Request[AnyContent] =>

    val blogPost = blogPostModel.getPost(slug)
    val title = blogPost.title
    val comments = blogPostCommentModel.getCommentsToPost(blogPost.id)

    Ok(views.html.post(title, blogPost, comments))
  }

  /** Save a blog post comment to the database
    *
    * @param postid Post ID
    * @param commentid Comment ID
    * @param comment Comment made
    * @return JSON indicating either a successful or an unsuccessful reply
    */
  def addComment(postid: Int, commentid: Int, comment: String) = userAction { implicit request: Request[AnyContent] =>
    val userid = request.session.get("userid")

    // @TODO Add record to database, check for bad words, send appropriate response back to FE
    Ok(Json.obj("status" -> "OK", "message" -> "OK"))
  }
}
