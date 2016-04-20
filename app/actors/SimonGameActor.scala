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

// Interaction - VIEW
class ButtonsManager(player: ActorSelection, interfaceKit: ActorSelection){

/*
val sounds = Map("red"    -> "E:/Projects/Mobile/simon-game/cat.mp3",
				 "blue"   -> "E:/Projects/Mobile/simon-game/dog.mp3",
				 "yellow" -> "E:/Projects/Mobile/simon-game/cow.mp3",
				 "green"  -> "E:/Projects/Mobile/simon-game/pig.mp3"
)

 */

	val sounds = Map(0    -> "E:/Projects/Mobile/simon-game/cat.mp3",
					 1   -> "E:/Projects/Mobile/simon-game/dog.mp3",
					 2 -> "E:/Projects/Mobile/simon-game/cow.mp3",
					 3  -> "E:/Projects/Mobile/simon-game/pig.mp3"
	)
/*
	val color = Map(0 -> "green",
					1 -> "red",
					2 -> "yellow",
					3-> "blue"
	)
*/
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
		for(Button(color) <- sequence){
			println(color)
			playSound(color)
		}
	}

	def playSound(color: Int) {
	/*
		val input = new BufferedInputStream(new FileInputStream(new File(path)))
		val player = new Player(input)
		player.play()*/

		player ! PlayerActor.Play(Song(color.toString, sounds(color)))
		interfaceKit ! InterfaceKitActor.TurnOffAll()
		interfaceKit ! InterfaceKitActor.TurnOn(color)
		Thread.sleep(1000)
		interfaceKit ! InterfaceKitActor.TurnOff(color)
	}
}

class SimonGameActor extends Actor {

	val player = context.actorSelection("../player")
	val interfaceKit = context.actorSelection("../interfaceKit")

	val ButtonsManager = new ButtonsManager(player, interfaceKit)

	val buttons:List[Button] = ButtonsManager.colors.map(Button(_))

	val infiniteSequence: Stream[Button] = buttonsGen

	def start():Int = play(1) // Start the game

	//Retourne le score
	private def play(size: Int): Int = {
		val sequence = infiniteSequence.take(size).toList
		ButtonsManager.showSequence(sequence)
		completeSequence(sequence) match {
			case true => play(size + 1)
			case false => size - 1
		}
	}

	private def completeSequence(sequence: List[Button]): Boolean = sequence match {
		case Nil => true
		case x::xs if ButtonsManager.askButton() == x => {
			Thread.sleep(1000)
			completeSequence(xs)
		}
		case _ => false
	}

	private def buttonsGen: Stream[Button] = {
		val rnd = new scala.util.Random
		buttons(rnd.nextInt(buttons length)) #:: buttonsGen
	}

	def receive = {
		case SimonGameActor.Play() => {
			val score = start()
			player ! PlayerActor.Play(Song("Game over", "E:/Projects/Mobile/simon-game/coins.mp3"))
			println(s"Game Over :(         Score: $score")
		}
	}

}

object SimonGameActor {
	def props = Props[SimonGameActor]

	abstract class Message

	case class Play() extends Message
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
