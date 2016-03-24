package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import scala.collection.JavaConversions._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class WidgetsController @Inject() extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def getTemp = Action {


    val test = play.Play.application.configuration.getConfigList("playlists").foldLeft(""){ (acc, stream) =>
        acc + s" ${stream.getString("name")} is ${stream.getString("path")}"
    }

    val playlists: Map[String, String] = (for{
        configuration <- play.Play.application.configuration.getConfigList("playlists")
    } yield (configuration.getString("name"), configuration.getString("path"))).toMap

    Ok("It's 25°C in Namur + config : " + play.Play.application.configuration.getString("foo") + " ...  " + test)
  }

}
