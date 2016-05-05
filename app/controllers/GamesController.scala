package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import akka.actor._

import scala.collection.JavaConversions._

import scala.util.{Try, Success, Failure}


import java.io.BufferedInputStream
import java.io.FileInputStream

// Json
import play.api.libs.json._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.io.Source
import java.io.File

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

    val scoresPath = "app/controllers/scores.json"

    def readScores: List[Score] = Json.parse(Source.fromFile(scoresPath).getLines.mkString).as[List[Score]]

    def writeScores(scores: List[Score]) {
        import java.io._
        val pw = new PrintWriter(new File(scoresPath))
        pw.write(Json.prettyPrint(Json.toJson(scores)))
        pw.close
    }

    def addScore(score: Score) {
        val f = new File(scoresPath)
        if (f.exists) {
            writeScores(score :: readScores)
        } else {
            writeScores(List(score))
        }
        lastScore = score
    }

    case class Score(name: String, points: Int, date: String)

    implicit val scoreReads: Reads[Score] = (
        (JsPath \ "name").read[String] and
        (JsPath \ "points").read[Int] and
        (JsPath \ "date").read[String]
    )(Score.apply _)

    implicit val scoreWrites: Writes[Score] = (
        (JsPath \ "name").write[String] and
        (JsPath \ "points").write[Int] and
        (JsPath \ "date").write[String]
    )(unlift(Score.unapply))

    var lastScore:Score = _

}


@Singleton
class GamesController @Inject() (system: ActorSystem) extends Controller {
    import GamesController._

    val player = system.actorSelection("user/player")
    val simon = system.actorSelection("user/simonGame")

    ///////////////////
    // HTTP HANDLERS //
    ///////////////////

    def startSimonGame(name: String) = Action {
        lastScore = null
        simon ! SimonGameActor.Play(name)
        Ok("OK!")
    }

    /*
     * Return the Top 10 scores
     */
    def getScoreboard = Action {
        Ok(Json.toJson(readScores.sorted(Ordering.by { s: Score => s.points }).reverse.take(10)))
    }

    def getLastScore = Action {
        if(lastScore == null){
            Ok("nothing")
        }else{
            val score = lastScore.points //Json.toJson(lastScore)
            lastScore = null
            Ok(score.toString) //Json.toJson(score))
        }
    }

} // END Playlist Controller
