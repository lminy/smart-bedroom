package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import akka.actor._


import scala.util.{Try, Success, Failure}


import java.io.BufferedInputStream;
import java.io.FileInputStream;


import actors._

@Singleton
class GamesController @Inject() (system: ActorSystem) extends Controller {

    val player = system.actorSelection("user/player")
    val simon = system.actorSelection("user/simonGame")

    ///////////////
    // FUNCTIONS //
    ///////////////



    ///////////////////
    // HTTP HANDLERS //
    ///////////////////

    def startSimonGame = Action {
        simon ! SimonGameActor.Play()
        Ok("OK!")
    }

} // END Playlist Controller
