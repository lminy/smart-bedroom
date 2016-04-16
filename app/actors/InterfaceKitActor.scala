package actors

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.phidgets.PhidgetException
import com.phidgets.InterfaceKitPhidget
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

import actors._

object InterfaceKitActor {
    def props = Props[InterfaceKitActor]

    abstract class Message
    /*
    case class Play(playable: Playable) extends Message
    case class Stop() extends Message
    case class Pause() extends Message
    case class Resume() extends Message*/
}

class InterfaceKitActor extends Actor{

    val player = context.actorSelection("../player")

    private val ifk = new InterfaceKitPhidget()

    println("creating interfaceKit...")

    ifk.addAttachListener(new AttachListener() {

        def attached(ae: AttachEvent) {
            println("attachment of " + ae)
        }
    })
    ifk.addDetachListener(new DetachListener() {

        def detached(ae: DetachEvent) {
            println("detachment of " + ae)
        }
    })
    ifk.addErrorListener(new ErrorListener() {

        def error(ee: ErrorEvent) {
            println(ee.getSource)
        }
    })
    ifk.addInputChangeListener(new InputChangeListener() {

        def inputChanged(oe: InputChangeEvent) {
            //println(oe)
        }
    })
    ifk.addOutputChangeListener(new OutputChangeListener() {

        def outputChanged(oe: OutputChangeEvent) {
            //println(oe)
            //println("Event output")
        }
    })
    ifk.addSensorChangeListener(new SensorChangeListener() {
        var playlist = 0
        def sensorChanged(oe: SensorChangeEvent) {
            val value = oe.getValue
            println("Value " + value)
            if (oe.getIndex == 7){
                if (value == 0){
                    player ! PlayerActor.StopAlarm()
                }
            }else if (oe.getIndex == 6){
                if (value > 0 && value < 450 && playlist != 1){
                    player ! "playlist1"
                    playlist = 1
                }
                else if (value >= 450 && value < 900 && playlist != 2){
                    player ! "playlist2"
                    playlist = 2
                }
                else if (value >= 900 && playlist != 0){
                    player ! "stop"
                    playlist = 0
                }
            }
            Thread.sleep(200)
        }
    })

    ifk.openAny()
    ifk.waitForAttachment(10000)


    def changeColor(i:Int){
        println("Message reçu: "+i)
        ifk.setOutputState(5, false)
        ifk.setOutputState(6, false)
        ifk.setOutputState(7, false)
        ifk.setOutputState(i, true)
        println("Lumière "+i+" selectionée")
    }

    def receive = {
        case 5 => changeColor(5)
        case 6 => changeColor(6)
        case 7 => changeColor(7)
        case _ => println("Nothing to do")
    }
}
