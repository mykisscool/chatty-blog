package controllers

import javax.inject._
import play.api.mvc._
import models.{LoginModel => Login}

/** Login Controller
  *
  * @param cc Simply meant to bundle together components typically used in a controller
  * @param loginModel Model class for authenticating users to application
  */
@Singleton
class LoginController @Inject()(cc: ControllerComponents, loginModel: Login) extends AbstractController(cc) {

  private val homeRoute = routes.HomeController.index()
  private val loginRoute = routes.LoginController.loginForm()

  /** Displays the login form
    *
    * @return Login form
    */
  def loginForm = Action { implicit request: Request[AnyContent] =>

    val title = "Login"
    val formSubmitUrl = routes.LoginController.processLoginAttempt()

    Ok(views.html.login(title, formSubmitUrl))
  }

  /** Either you see the login form with some login errors OR you see the index page as an authenticated user
    *
    * @return Login form page or index page
    */
  def processLoginAttempt = Action { implicit request: Request[AnyContent] =>

    val username = request.body.asFormUrlEncoded.get("user").head
    val password = request.body.asFormUrlEncoded.get("pass").head
    val (validAttempt, reason, userIfAny) = loginModel.isValidLoginAttempt(username, password)

    if (validAttempt) {
      Redirect(homeRoute)
        .withSession("userid" -> userIfAny.id.toString, "name" -> userIfAny.fullname, "handle" -> userIfAny.nickname)
        .flashing("info" -> reason)
    }
    else {
      Redirect(loginRoute).flashing("error" -> reason)
    }
  }

  /** Logs out a user
    *
    * @return The index page with a cleared session
    */
  def processLogoutAttempt = Action { implicit request: Request[AnyContent] =>
    Redirect(homeRoute).withNewSession.flashing("info" -> "You have logged out.")
  }
}
