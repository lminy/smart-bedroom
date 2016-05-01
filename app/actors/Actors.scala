package actors

import akka.actor._
import javax.inject._
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import actors._

import com.phidgets.RFIDPhidget

@Singleton
class Actors @Inject() (system: ActorSystem, appLifecycle: ApplicationLifecycle) {
	val rfidPh:RFIDPhidget = new RFIDPhidget()

	// This code is called when the application starts.
	val player = system.actorOf(PlayerActor.props, "player")
	val interfaceKit = system.actorOf(InterfaceKitActor.props, "interfaceKit")
	val rfid = system.actorOf(RFIDActor.props, "rfid")
	val simon = system.actorOf(SimonGameActor.props, "simonGame")

	Logger.info(s"Actors.scala : actors created")
}
