package controllers

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import javax.inject._
import play.api._
import play.api.mvc._

import akka.actor._

import scala.util.{Try, Success, Failure}


//Concurency imports
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

import actors._

object BedroomController {
}

@Singleton
class BedroomController @Inject() (system: ActorSystem) extends Controller {

    import BedroomController._

    val interfaceKit = system.actorSelection("user/interfaceKit")
    val servo = system.actorSelection("user/servo")

    ///////////////////
    // HTTP HANDLERS //
    ///////////////////

    def presence = Action {
        implicit val timeout = Timeout(5 seconds)
        val future = interfaceKit ? InterfaceKitActor.Presence()
        val presence = Try(Await.result(future, timeout.duration).asInstanceOf[Boolean])
        if(presence.getOrElse(false)){
            Ok("true")
        }else{
            Ok("false")
        }
    }

    def start {
        import sys.process._
        "sudo service motion start" !
    }

    def stop {
        import sys.process._
        "sudo service motion stop" !
    }

    def webcam = Action {
        start
        Future{
            Thread.sleep(5*60*1000)
            stop
        }
        Ok("OK!")
    }

    def curtains = Action {
        import actors.ServoMoteurActor._
        servo ! Close()
        Ok("OK!")
    }

} // END bedroom Controller
