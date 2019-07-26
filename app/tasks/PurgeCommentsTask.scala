package tasks

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.actor.ActorSystem
import play.api.Logger
import javax.inject.{Inject, Singleton}
import models.{BlogCommentModel => Comment}

/** Actor/task responsible for cleaning up user comments
  * https://www.bradcypert.com/scheduling-background-jobs-in-play-with-scala/
  *
  * @param actorSystem Hierarchical group of actors which share common configuration
  * @param blogPostCommentModel Model class for blog post comments
  * @param ec Thread pool
  */
@Singleton
class PurgeCommentsTask @Inject()(actorSystem: ActorSystem,
                                  blogPostCommentModel: Comment)(implicit ec: ExecutionContext) {

  private val logger: Logger = Logger(getClass)

  actorSystem.scheduler.schedule(initialDelay = 0.microseconds, interval = 8.hours) {
    blogPostCommentModel.purgeUserComments()
    logger.info("Comments cleaned up.")
  }
}
