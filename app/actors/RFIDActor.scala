package actors

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import com.phidgets.PhidgetException
import com.phidgets.RFIDPhidget
import com.phidgets.event.AttachEvent
import com.phidgets.event.AttachListener
import com.phidgets.event.DetachEvent
import com.phidgets.event.DetachListener
import com.phidgets.event.ErrorEvent
import com.phidgets.event.ErrorListener
import com.phidgets.event.InputChangeEvent
import com.phidgets.event.InputChangeListener
import com.phidgets.event.OutputChangeEvent
import com.phidgets.event.OutputChangeListener
import com.phidgets.event.SensorChangeEvent
import com.phidgets.event.SensorChangeListener
import com.phidgets.event.TagGainEvent
import com.phidgets.event.TagGainListener
import com.phidgets.event.TagLossEvent
import com.phidgets.event.TagLossListener
import play.api.Logger

import scala.concurrent.duration._

import play.api.libs.concurrent.Execution.Implicits._

import controllers.GamesController._

object RFIDActor {
    def props = Props[RFIDActor]

    abstract class Message
    /*
    case class Play(playable: Playable) extends Message
    case class Stop() extends Message
    case class Pause() extends Message
    case class Resume() extends Message*/
}

class RFIDActor() extends Actor {
    import InterfaceKitActor._

    val rfidPh:RFIDPhidget = new RFIDPhidget()

    val player = context.actorSelection("../player")
    val interfaceKit = context.actorSelection("../interfaceKit")

    //Valeur permettant de savoir si le rfid est connecter et OK
    var temprfid = 0

    rfidPh.addAttachListener(new AttachListener() {
        def attached(ae: AttachEvent) {
            temprfid = 0
            try {
                ae.getSource.asInstanceOf[RFIDPhidget].setAntennaOn(true)
                ae.getSource.asInstanceOf[RFIDPhidget].setLEDOn(true)
            } catch {
                case ex: PhidgetException => Logger.debug("exception")
            }
            Logger.debug("attachment of " + ae)
        }
    })
    rfidPh.addDetachListener(new DetachListener() {

        def detached(ae: DetachEvent) {
            Logger.debug("detachment of " + ae)
        }
    })
    rfidPh.addErrorListener(new ErrorListener() {

        def error(ee: ErrorEvent) {

        }
    })
    rfidPh.addTagGainListener(new TagGainListener() {

        def tagGained(oe: TagGainEvent) {
            Logger.debug(oe.getValue)
            changeTag(oe.getValue)
            rfidPh.setLEDOn(false)
        }
    })
    rfidPh.addTagLossListener(new TagLossListener() {

        def tagLost(oe: TagLossEvent) {
            rfidPh.setLEDOn(true)
        }
    })
    rfidPh.addOutputChangeListener(new OutputChangeListener() {

        def outputChanged(oe: OutputChangeEvent) {
            //Logger.debug("Error")
        }
    })

    def changeTag(id: String) {

        player ! PlayerActor.Play(Sound(endGameSound))

        // All balls
        Logger.debug("Snooze")
        player ! PlayerActor.PauseAlarm()
        //context.system.scheduler.scheduleOnce(10 seconds, player, PlayerActor.Resume())
        context.system.scheduler.scheduleOnce(10 seconds) {
            player ! PlayerActor.Resume()
        }

        if (id.equalsIgnoreCase("5c005e6423")) {
            interfaceKit ! TurnOffAll()
            interfaceKit ! TurnOn(5)
        }
        else if (id.equalsIgnoreCase("5c005e3598")) {
            interfaceKit ! TurnOffAll()
            interfaceKit ! TurnOn(6)
        }
        else if (id.equalsIgnoreCase("5c005c8cb4")) {
            interfaceKit ! TurnOffAll()
            interfaceKit ! TurnOn(7)
        }
        else if (id.equalsIgnoreCase("700082406f")) {
            interfaceKit ! TurnOffAll()
        }
    }

    while (true) {
        try {
            Thread.sleep(500)
            if (temprfid == 0) {
                rfidPh.openAny()
                rfidPh.waitForAttachment(1000)
            }
        } catch {
            case e: PhidgetException => Logger.debug("No rfid connected")
            case e: InterruptedException => e.printStackTrace()
        }
        //Premier passage dans la boucle
        temprfid = 1
    }

    //L'acteur RFID n'est censé recevoir aucun message, car ce n'est pas un actuateur
    def receive = {
        case _ => Logger.debug("Message to RFID !")
    }
}
