package actors

import com.phidgets._
import com.phidgets.event._
import akka.actor._

object ServoMoteurActor {
    def props = Props[ServoMoteurActor]

    abstract class Message
    
    case class Close() extends Message
    case class Open() extends Message
}

class ServoMoteurActor() extends Actor{
  import ServoMoteurActor._
  
  val interfaceKit = context.actorSelection("../interfaceKit")
  
  //Valeur permettant de savoir si le servo moteur est connecter et OK
  var tempservo = 0

  var servo: AdvancedServoPhidget = null
  println(Phidget.getLibraryVersion)
  servo = new AdvancedServoPhidget()
  
  servo.addAttachListener(new AttachListener() {

    def attached(ae: AttachEvent) {
      tempservo = 0
      println("attachment of " + ae)
    }
  })
  servo.addDetachListener(new DetachListener() {

    def detached(ae: DetachEvent) {
      println("detachment of " + ae)
    }
  })
  servo.addErrorListener(new ErrorListener() {

    def error(ee: ErrorEvent) {
      println("error event for " + ee)
    }
  })
  servo.addServoPositionChangeListener(new ServoPositionChangeListener() {

    def servoPositionChanged(oe: ServoPositionChangeEvent) {
      //println(oe)
    }
  })
  
  try {
      Thread.sleep(500)
      if (tempservo == 0) {
          servo.openAny()
          servo.waitForAttachment(10000)
      }
  } catch {
      case e: PhidgetException => println("No rfid connected")
      case e: InterruptedException => e.printStackTrace()
  }
  //Premier passage dans la boucle
  //tempservo = 1
  
  servo.setSpeedRampingOn(0, true)
  servo.setAcceleration(0, 20.0)
  servo.setVelocityLimit(0, 20.0)
  
  def openMove = {
    servo.setEngaged(0, false)
    servo.setSpeedRampingOn(0, true)
    servo.setAcceleration(0, 20.0)
    servo.setVelocityLimit(0, 20.0)
    servo.setPosition(0, 50)
    servo.setEngaged(0, true)
    Thread.sleep(500)
  }
  
  def closeMove = {
    servo.setEngaged(0, false)
    servo.setSpeedRampingOn(0, true)
    servo.setAcceleration(0, 20.0)
    servo.setVelocityLimit(0, 20.0)
    servo.setPosition(0, 200)
    servo.setEngaged(0, true)
    Thread.sleep(500)
  }
  
  /*def closeServo = {
    servo.close()
    servo = null
    println("Object ServoMoteur closed")
  }*/
  
  def receive = {
    case Open() => openMove
    case Close() => closeMove
    case _ => println("ServoMoteur: Nothing to do")
  }
}
