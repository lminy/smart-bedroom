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
	Logger.info(s"Actors.scala : actors created")
}
