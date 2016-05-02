// scalac -classpath .;*.jar SimonGame.scala
// scala -classpath .;*.jar SimonGame

package actors

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.util.Random
import scala.util.{Try, Success, Failure}
import scala.language.postfixOps
import java.io._

//Concurency imports
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._

import scala.language.postfixOps

case class Button(color: Int)

import controllers.GamesController._

import collection.JavaConversions._

// Interaction - VIEW
class ButtonsManager(player: ActorSelection, interfaceKit: ActorSelection){

    def colors: List[Int] = sounds.keys.toList

	def askButton(): Button = {

		//val color = scala.io.StdIn.readLine()
 		implicit val timeout = Timeout(5 seconds)
		val future = interfaceKit ? InterfaceKitActor.WaitInput()
    	val result = Try(Await.result(future, timeout.duration).asInstanceOf[Option[Int]])
    	println(s"Result input : $result")

		//if(sounds contains color) playSound(color)
		Button(result.getOrElse(None).getOrElse(-1))
	}

	def showSequence(sequence: List[Button]){
		Thread.sleep(1000)
		for(Button(color) <- sequence){
			println(color)
			playSound(color)
		}
	}

	def playSound(color: Int) {
		player ! PlayerActor.Play(Song(color.toString, sounds(color)))
		interfaceKit ! InterfaceKitActor.TurnOffAll()
		interfaceKit ! InterfaceKitActor.TurnOn(color)
		Thread.sleep(1000)
		interfaceKit ! InterfaceKitActor.TurnOff(color)
	}
}

class SimonGame(buttonsManager: ButtonsManager) {
	val buttons:List[Button] = buttonsManager.colors.map(Button(_))

	val infiniteSequence: Stream[Button] = buttonsGen

	def start():Int = play(1) // Start the game

	//Retourne le score
	private def play(size: Int): Int = {
		val sequence = infiniteSequence.take(size).toList
		buttonsManager.showSequence(sequence)
		completeSequence(sequence) match {
			case true => play(size + 1)
			case false => size - 1
		}
	}

	private def completeSequence(sequence: List[Button]): Boolean = sequence match {
		case Nil => true
		case x::xs if buttonsManager.askButton() == x => {
			//Thread.sleep(1000)
			completeSequence(xs)
		}
		case _ => false
	}

	private def buttonsGen: Stream[Button] = {
		val rnd = new scala.util.Random
		buttons(rnd.nextInt(buttons length)) #:: buttonsGen
	}
}

class SimonGameActor extends Actor {
	val player = context.actorSelection("../player")
	val interfaceKit = context.actorSelection("../interfaceKit")

	val buttonsManager = new ButtonsManager(player, interfaceKit)

	def receive = {
		case SimonGameActor.Play(name: String) => {
			val game = new SimonGame(buttonsManager)
			val score = game.start()
			player ! PlayerActor.Play(Song("Game over", endGameSound))
			println(s"Game Over :(         Score: $score")
            // Store the score
            import controllers.GamesController._
            import java.util.Date
            import java.text.SimpleDateFormat
            val dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            val date = new Date()
            addScore(Score(name, score, dateFormat.format(date)))
		}
	}
}

object SimonGameActor {
	def props = Props[SimonGameActor]

	abstract class Message

	case class Play(name: String) extends Message
}

object SimonGame{
	def main(args: Array[String]){
		/*
		val game = new SimonGame()
		val score = game.start()
		ButtonsManager.playSound("coins.mp3")
		println(s"Result : $score")
		*/
	}
}
