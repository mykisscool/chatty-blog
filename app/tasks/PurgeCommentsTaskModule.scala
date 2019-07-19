package tasks

import play.api.inject._

/** When using Guice Dependency Injection, you will need to create and enable a module to load the tasks as eager singletons
  * https://www.playframework.com/documentation/2.7.x/ScheduledTasks#Starting-tasks-when-your-app-starts
  */
class PurgeCommentsTaskModule extends SimpleModule(bind[PurgeCommentsTask].toSelf.eagerly())
