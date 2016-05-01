package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import akka.actor._

import scala.collection.JavaConversions._

import scala.util.{Try, Success, Failure}


import java.io.BufferedInputStream;
import java.io.FileInputStream;


import actors._

object GamesController{
    def sounds: Map[Int, String] = (for{
		configuration <- play.Play.application.configuration.getConfigList("sounds")
	} yield (configuration.getInt("number").intValue, configuration.getString("path"))).toMap
    /*
    	val sounds = Map(0  -> "E:/Projects/Mobile/simon-game/cat.mp3",
    					 1  -> "E:/Projects/Mobile/simon-game/dog.mp3",
    					 2  -> "E:/Projects/Mobile/simon-game/cow.mp3",
    					 3  -> "E:/Projects/Mobile/simon-game/pig.mp3"
    	)
    */
   
    def endGameSound = play.Play.application.configuration.getString("end-game-sound")
}


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
