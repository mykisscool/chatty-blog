package controllers

import java.net.URI
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.Configuration
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request, RequestHeader, WebSocket}
import play.api.libs.json._
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Source}
import akka.stream.Materializer
import models.{BlogCommentModel => Comment, BlogPostModel => Post}

/** Blog post page controller- displays a blog post
  *
  * @param cc Simply meant to bundle together components typically used in a controller
  * @param blogPostModel Model class for blog posts
  * @param blogPostCommentModel Model class for blog post comments
  * @param userAction Checks to make sure the request is performed by an authenticated user
  * @param materializer The Materializer is a factory for stream execution engines, it is the thing that makes streams run
  * @param ec Thread pool
  */
@Singleton
class BlogPostController @Inject()(cc: ControllerComponents,
                                   blogPostModel: Post,
                                   blogPostCommentModel: Comment,
                                   userAction: UserAction,
                                   configuration: Configuration)
                                   (implicit materializer: Materializer,
                                             ec: ExecutionContext) extends AbstractController(cc) {

  private type webSocketMessage = String

  private val (chatSink, chatSource) = {

    /** A Source is an operator with exactly one output, emitting data elements whenever downstream operators are ready
      * to receive them. */
    val source = MergeHub.source[webSocketMessage]
      .log("source")
      .recoverWithRetries(-1, {
        case _: Exception => Source.empty
      })

    /** A Sink is an operator with exactly one input, requesting and accepting data elements, possibly slowing down the
      * upstream producer of elements */
    val sink = BroadcastHub.sink[webSocketMessage]
    source.toMat(sink)(Keep.both).run()
  }

  /** A Flow is an operator which has exactly one input and output, which connects its upstream and downstream by
    * transforming the data elements flowing through it. */
  private val userFlow: Flow[webSocketMessage, webSocketMessage, _] = {
    Flow.fromSinkAndSource(chatSink, chatSource).log("userFlow")
  }

  /** Checks that the WebSocket comes from the same origin. This is necessary to protect
    * against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
    * https://github.com/playframework/play-samples/tree/2.8.x/play-scala-chatroom-example
    *
    * @param rh The HTTP request header
    * @return True of false if the WbSocket comes from the same origin
    */
  private def sameOriginCheck(implicit rh: RequestHeader): Boolean = {
    rh.headers.get("Origin") match {
      case Some(originValue) if originMatches(originValue) => true
      case Some(badOrigin) => false
      case None => false
    }
  }

  /** Returns true if the value of the Origin header contains an acceptable value.
    * https://github.com/playframework/play-samples/tree/2.8.x/play-scala-chatroom-example
    *
    * @param origin Origin request header
    * @return True if the Origin header contains an acceptable value
    */
  private def originMatches(origin: String): Boolean = {
    try {
      val url = new URI(origin)
      url.getHost == "localhost" &&
        (url.getPort match { case 9000 | 19001 => true; case _ => false })
    }
    catch {
      case e: Exception => false
    }
  }

  /** Establish WebSocket connection/route
    * https://github.com/playframework/play-samples/tree/2.8.x/play-scala-chatroom-example
    *
    * @return A WebSocket handler
    */
  def chat(): WebSocket = {
    WebSocket.acceptOrResult[webSocketMessage, webSocketMessage] {
      case rh if sameOriginCheck(rh) =>
        Future.successful(userFlow).map {
          flow => Right(flow)
        }.recover {
          case e: Exception => Left(InternalServerError("Cannot create WebSocket."))
        }

      case rejected =>
        Future.successful {
          Left(Forbidden("Forbidden."))
        }
    }
  }

  /** Display a blog post
    *
    * @param slug URI segment that uniquely identifies a blog post
    * @return The blog post view
    */
  def getPost(slug: String) = Action { implicit request: Request[AnyContent] =>

    val blogPost = blogPostModel.getPost(slug)
    val title = blogPost.title
    val comments = blogPostCommentModel.getCommentsToPost(blogPost.id)
    val webSocketUrl = routes.BlogPostController.chat().webSocketURL(configuration.get[Boolean]("my.secureWebSocket"))

    Ok(views.html.post(title, blogPost, comments, webSocketUrl))
  }

  /** Save a blog post comment to the database
    *
    * @param postid Post ID
    * @param commentid Comment ID
    * @param comment Comment made
    * @return JSON indicating either a successful or an unsuccessful reply
    */
  def addComment(postid: Int, commentid: Int, comment: String) = userAction { implicit request: Request[AnyContent] =>

    if (!request.headers.get("X-Requested-With").contains("XMLHttpRequest")){
      // @TODO Validate Ajax only (fringe case; potentially unnecessary)
    }

    val userid = request.session.get("userid").get.toInt
    val (newcomment, newcommentid) = blogPostCommentModel.addComment(postid, userid, commentid, comment)
    val (title, slug) = blogPostModel.getPostDetails(postid)

    Ok(Json.obj(
      "postid" -> postid,
      "userid" -> userid,
      "name" -> request.session.get("name").get,
      "handle" -> request.session.get("handle").get,
      "comment" -> newcomment,
      "commentid" -> newcommentid,
      "title" -> title,
      "slug" -> slug
    ))
  }
}
