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

import scala.concurrent.duration._

import play.api.libs.concurrent.Execution.Implicits._

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
                case ex: PhidgetException => println("exception")
            }
            println("attachment of " + ae)
        }
    })
    rfidPh.addDetachListener(new DetachListener() {

        def detached(ae: DetachEvent) {
            println("detachment of " + ae)
        }
    })
    rfidPh.addErrorListener(new ErrorListener() {

        def error(ee: ErrorEvent) {

        }
    })
    rfidPh.addTagGainListener(new TagGainListener() {

        def tagGained(oe: TagGainEvent) {
            println(oe.getValue)
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
            println(oe)
        }
    })

    def changeTag(id: String) {
        if (id.equalsIgnoreCase("4a003749b0")) {
            interfaceKit ! TurnOffAll()
            interfaceKit ! TurnOn(0)
        }
        else if (id.equalsIgnoreCase("4a003726cb")) {
            interfaceKit ! TurnOffAll()
            interfaceKit ! TurnOn(1)
        }
        else if (id.equalsIgnoreCase("2800b87ac5")) {
            interfaceKit ! TurnOffAll()
            interfaceKit ! TurnOn(2)
        }
        else if (id.equalsIgnoreCase("5c005e3598")) {
            println("Snooze")
            player ! PlayerActor.PauseAlarm()
            //context.system.scheduler.scheduleOnce(10 seconds, player, PlayerActor.Resume())
            context.system.scheduler.scheduleOnce(10 seconds) {
                player ! PlayerActor.Resume()
            }
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
                //RID connecté
                //TODO
            }
        } catch {
            case e: PhidgetException => println("No rfid connected")
            case e: InterruptedException => e.printStackTrace()
        }
        //Premier passage dans la boucle
        temprfid = 1
    }

    //L'acteur RFID n'est censé recevoir aucun message, car ce n'est pas un actuateur
    def receive = {
        case _ => println("Message to RFID !")
    }
}
