package controllers

import javax.inject._
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import models.{LoginModel => Login}

/** Login Controller
  *
  * @param cc Simply meant to bundle together components typically used in a controller
  * @param loginModel Model class for authenticating users to application
  */
@Singleton
class LoginController @Inject()(cc: ControllerComponents, loginModel: Login) extends AbstractController(cc) {

  /** Displays the login form
    *
    * @return Login form
    */
  def loginForm = Action { implicit request: Request[AnyContent] =>

    val title = "Login"
    val formSubmitUrl = routes.LoginController.processLoginAttempt()

    Ok(views.html.login(title, formSubmitUrl)(request))
  }

  /** Either you see the login form with some login errors OR you see the index page as an authenticated user
    *
    * @return Login form page or index page
    */
  def processLoginAttempt = Action { implicit request: Request[AnyContent] =>

    val homeRoute = routes.HomeController.index()
    val loginRoute = routes.LoginController.loginForm()
    val username = request.body.asFormUrlEncoded.get("user").head
    val password = request.body.asFormUrlEncoded.get("pass").head
    val (validAttempt, reason) = loginModel.isValidLoginAttempt(username, password)

    if (validAttempt) {
      Redirect(homeRoute)
    }
    else {
      Redirect(loginRoute).flashing("error" -> reason)
    }
  }
}
