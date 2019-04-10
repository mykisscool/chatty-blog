package models

import javax.inject.{Inject, Singleton}
import org.mindrot.jbcrypt.BCrypt
import play.api.db.DBApi
import anorm.{SQL, RowParser, Macro, SimpleSql, Row}

/** Case class for an authenticated user
  *
  * @param id User ID
  * @param fullname User full name
  * @param nickname User nickname
  * @param password User password
  */
case class User(id: Int,
                fullname: String,
                nickname: String,
                password: String)

/** Methods contain SQL statements that help authenticate and identify a user
  *
  * @param dbapi Database interface
  */
@Singleton
class LoginModel @Inject()(dbapi: DBApi) {

  private val db = dbapi.database("default")

  /** Validates a login attempt
    *
    * @param username Username POST variable
    * @param password Password POST variable
    * @return True for a successful attempt; False for an unsuccessful attempt
    */
  def isValidLoginAttempt(username: String, password: String): (Boolean, String, User) = {

    db.withConnection { implicit c =>

      val parser: RowParser[User] = Macro.namedParser[User]
      val q: SimpleSql[Row] = SQL(
        """
          |SELECT *
          |FROM user
          |WHERE nickname = {username}
        """.stripMargin
      ).on("username" -> username)

      try {
        val result = q.as(parser.single)

        if (BCrypt.checkpw(password, result.password)) {
          (true, s"You are logged in as ${result.fullname}.", result)
        }
        else {
          (false, "Access denied.", null)
        }
      }
      catch {
        case a: anorm.AnormException => (false, "Invalid username.", null)
        case _: Throwable => (false, "Something bad happened. Try again?", null)
      }
    }
  }
}
