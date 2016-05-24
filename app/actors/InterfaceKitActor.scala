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
import play.api.Logger

import actors._

import controllers.GamesController._

class InterfaceKitActor extends Actor{
    import InterfaceKitActor._

    val player = context.actorSelection("../player")

    // INPUTS :
    // 0 : initial initial state
    // 1 : Bouton pressed
    // 2 : Bouton released
    var inputs = Array(0,0,0,0,0,0,0,0)

    private val ifk = new InterfaceKitPhidget()
    private var ifkConnected = false

    Logger.debug("creating interfaceKit...")

    ifk.addAttachListener(new AttachListener() {

        def attached(ae: AttachEvent) {
            Logger.debug("attachment of " + ae)
            ifkConnected = true
        }
    })
    ifk.addDetachListener(new DetachListener() {

        def detached(ae: DetachEvent) {
            Logger.debug("detachment of " + ae)
            ifkConnected = false
        }
    })
    ifk.addErrorListener(new ErrorListener() {

        def error(ee: ErrorEvent) {
            //Logger.debug("Error listener")
        }
    })
    ifk.addInputChangeListener(new InputChangeListener() {

        def inputChanged(oe: InputChangeEvent) {
            if(inputs(oe.getIndex) == 0) {
                inputs(oe.getIndex) = 1
            } else if(inputs(oe.getIndex) == 1) {
                inputs(oe.getIndex) = 2
            }
            Logger.debug(s"input ${oe.getIndex} : ${oe.getState} & state : ${inputs(oe.getIndex)}")
            if(oe.getState == false && sounds.contains(oe.getIndex)){
                player ! PlayerActor.Play(Song(sounds(oe.getIndex), sounds(oe.getIndex)))
            }
        }
    })
    ifk.addOutputChangeListener(new OutputChangeListener() {

        def outputChanged(oe: OutputChangeEvent) {
            //Logger.debug(oe)
            //Logger.debug("Event output")
        }
    })
    ifk.addSensorChangeListener(new SensorChangeListener() {
        var playlist = 0
        def sensorChanged(oe: SensorChangeEvent) {
            val value = oe.getValue
            Logger.debug("Value " + value)
            if (oe.getIndex == 7){ // Force sensor
                if (value == 0){
                    player ! PlayerActor.StopAlarm()
                }
            }else if (oe.getIndex == 6){ // Slider sensor
                if (value > 0 && value < 450 && playlist != 1){
                    player ! PlayerActor.Play(Playlist("good-mood"))
                    playlist = 1
                }
                else if (value >= 450 && value < 900 && playlist != 2){
                    player ! PlayerActor.Play(Playlist("cool-off"))
                    playlist = 2
                }
                else if (value >= 900 && playlist != 0){
                    player ! PlayerActor.Stop()
                    playlist = 0
                }
            }
            Thread.sleep(200)
        }
    })

    Logger.debug("Connecting to ifk...")
    try{
        ifk.openAny()
        ifk.waitForAttachment(10000)
        Logger.debug("connected to ifk!")
    }catch{
        case ex:PhidgetException => Logger.debug("No interfaceKit connected...")
    }

    def turnOffAll(){
        for(index <- 0 to 7){
            ifk.setOutputState(index, false)
        }
        //Logger.debug("All leds turned off")
    }

    def turnOn(index: Int){
        ifk.setOutputState(index, true)
        //Logger.debug(s"Led $index turned on")
    }

    def turnOff(index: Int){
        ifk.setOutputState(index, false)
        //Logger.debug(s"Led $index turned off")
    }

    def resetInputs(){
        inputs = inputs.map(_=>0)
    }
    def isThereButtonReleased = (inputs.indexOf(2) != -1)
    def indexButtonReleased = inputs.indexOf(2)

    // Wait max 5 seconds
    def waitInput(){
        Logger.debug("Waiting input")
        resetInputs()
        var i = 1
        while(i <= 5*5 && !isThereButtonReleased){
            Thread.sleep(100)
            i = i+1
        }
        if(isThereButtonReleased){
            //Logger.debug(s"button $indexLastInput pushed")
            sender ! Some(indexButtonReleased)
        }else{
            //Logger.debug(s"no button pushed...")
            sender ! None
        }
    }

    def presence(){
        if(ifk.getSensorValue(0) > 700){
            sender ! true
        }else{
            sender ! false
        }
    }

    def receive = {
        case TurnOffAll() => turnOffAll()
        case TurnOn(index) => turnOn(index)
        case TurnOff(index) => turnOff(index)
        case WaitInput() => waitInput()
        case Presence() => presence()
        case _ => Logger.debug("Nothing to do")
    }
}

object InterfaceKitActor {
    def props = Props[InterfaceKitActor]

    abstract class Message

    case class TurnOffAll() extends Message
    case class TurnOn(index: Int) extends Message
    case class TurnOff(index: Int) extends Message
    case class WaitInput() extends Message
    case class Presence() extends Message

}
