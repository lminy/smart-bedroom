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

import controllers.GamesController._

class InterfaceKitActor extends Actor{
    import InterfaceKitActor._

    val player = context.actorSelection("../player")

    var inputs = Array(false,false,false,false,false,false,false,false)

    private val ifk = new InterfaceKitPhidget()
    private var ifkConnected = false

    println("creating interfaceKit...")

    ifk.addAttachListener(new AttachListener() {

        def attached(ae: AttachEvent) {
            println("attachment of " + ae)
            ifkConnected = true
        }
    })
    ifk.addDetachListener(new DetachListener() {

        def detached(ae: DetachEvent) {
            println("detachment of " + ae)
            ifkConnected = false
        }
    })
    ifk.addErrorListener(new ErrorListener() {

        def error(ee: ErrorEvent) {
            println(ee.getSource)
        }
    })
    ifk.addInputChangeListener(new InputChangeListener() {

        def inputChanged(oe: InputChangeEvent) {
            //println(s"input ${oe.getIndex} : ${oe.getState}")
            inputs(oe.getIndex) = true
            if(oe.getState == false && sounds.contains(oe.getIndex)){
                player ! PlayerActor.Play(Song(sounds(oe.getIndex), sounds(oe.getIndex)))
            }
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

    println("Connecting to ifk...")
    try{
        ifk.openAny()
        ifk.waitForAttachment(10000)
        println("connected to ifk!")
    }catch{
        case ex:PhidgetException => println("No interfaceKit connected...")
    }


    def changeColor(i:Int){
        println("Message reçu: "+i)
        ifk.setOutputState(5, false)
        ifk.setOutputState(6, false)
        ifk.setOutputState(7, false)
        ifk.setOutputState(i, true)
        println("Lumière "+i+" selectionée")
    }

    def turnOffAll(){
        for(index <- 0 to 7){
            ifk.setOutputState(index, false)
        }
        //println("All leds turned off")
    }

    def turnOn(index: Int){
        ifk.setOutputState(index, true)
        //println(s"Led $index turned on")
    }

    def turnOff(index: Int){
        ifk.setOutputState(index, false)
        //println(s"Led $index turned off")
    }

    def resetInputs(){
        inputs = inputs.map(_=>false)
    }
    def isThereInput = inputs.foldLeft(false){_||_}
    def indexLastInput = inputs.indexOf(true)

    // Wait max 5 seconds
    def waitInput(){
        println("Waiting input")
        resetInputs()
        var i = 1
        while(i <= 5*5 && !isThereInput){
            Thread.sleep(200)
            i = i+1
        }
        if(isThereInput){
            //println(s"button $indexLastInput pushed")
            sender ! Some(indexLastInput)
        }else{
            //println(s"no button pushed...")
            sender ! None
        }
    }

    def receive = {
        /*case 5 => changeColor(5)
        case 6 => changeColor(6)
        case 7 => changeColor(7)*/
        case TurnOffAll() => turnOffAll()
        case TurnOn(index) => turnOn(index)
        case TurnOff(index) => turnOff(index)
        case WaitInput() => waitInput()
        case _ => println("Nothing to do")
    }
}

object InterfaceKitActor {
    def props = Props[InterfaceKitActor]

    abstract class Message

    case class TurnOffAll() extends Message
    case class TurnOn(index: Int) extends Message
    case class TurnOff(index: Int) extends Message
    case class WaitInput() extends Message
/*
    val master = ActorSystem("SmartHome")
    val interfaceKit = master.actorOf(InterfaceKitActor.props, name = "interfaceKit")

    def main(args: Array[String]):Unit = {
        Thread.sleep(2000)
        println("Start Main")
        interfaceKit ! TurnOffAll()
        interfaceKit ! TurnOn(0)
        println("End Main")
    }*/
}
