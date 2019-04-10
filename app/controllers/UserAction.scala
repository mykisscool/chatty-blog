package controllers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._
import play.api.mvc.Results._

/** Implement our own authentication action transformer that determines the user from the original request
  *
  * https://www.playframework.com/documentation/2.7.x/ScalaActionsComposition#Authentication
  * https://github.com/alvinj/PlayFrameworkLoginAuthenticationExample
  *
  * @param parser HTTP Request body parser
  * @param ec Thread pool
  */
class UserAction @Inject()(parser: BodyParsers.Default)(implicit ec: ExecutionContext) extends ActionBuilderImpl(parser) {

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {

    val userid = request.session.get("userid")

    userid match {
      case None => Future.successful(Forbidden(views.html.errors("You must login first.")))
      case Some(u) => val res: Future[Result] = block(request); res
    }
  }
}
