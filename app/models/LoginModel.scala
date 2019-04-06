package models

import javax.inject.{Inject, Singleton}
import org.mindrot.jbcrypt.BCrypt
import play.api.db.DBApi
import anorm.{SQL, SqlParser}

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
  def isValidLoginAttempt(username: String, password: String): (Boolean, String) = {

    db.withConnection { implicit c =>
      val q = SQL(
        """
          |SELECT password
          |FROM user
          |WHERE nickname = {username}
        """.stripMargin
      ).on("username" -> username)

      try {
        val hashedPassword = q.as(SqlParser.scalar[String].single)

        if (BCrypt.checkpw(password, hashedPassword)) {
          (true, "You are now logged in.")
        }
        else {
          (false, "Access denied.")
        }
      }
      catch {
        case a: anorm.AnormException => (false, "Invalid username.")
        case _: Throwable => (false, "Something bad happened. Try again?")
      }
    }
  }
}
