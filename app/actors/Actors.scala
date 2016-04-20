package actors

import akka.actor._
import javax.inject._
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import actors._

@Singleton
class Actors @Inject() (system: ActorSystem, appLifecycle: ApplicationLifecycle) {

	// This code is called when the application starts.
	val player = system.actorOf(PlayerActor.props, "player")
	val interfaceKit = system.actorOf(InterfaceKitActor.props, "interfaceKit")
	val simon = system.actorOf(SimonGameActor.props, "simonGame")

	Logger.info(s"Actors.scala : actors created")
}
