package actors

// Librairy to play MP3
import javazoom.jl.player.Player

// Scala
import scala.collection.mutable.Queue
import scala.util.Random

// Java
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File

// Akka
import akka.actor._


object PlayerActor {
    def props = Props[PlayerActor]

    abstract class Message

    case class Play(playable: Playable) extends Message
    case class Stop() extends Message
    case class Pause() extends Message
    case class Resume() extends Message
}

class PlayerActor extends Actor {
    import PlayerActor._

    def stopped: Receive = {
        case Play(playable: Playable) => {
            PausablePlayer.play(playable)
            context become playing
        }
    }

    def playing: Receive = {
        case Stop() => {
            PausablePlayer.stop()
            context become stopped
        }

        case Pause() => {
            PausablePlayer.stop()
            context become paused
        }
        case Play(playable: Playable) => {
            PausablePlayer.stop()
            println("Stop & Playing...")
            PausablePlayer.play(playable)
        }
    }

    def paused: Receive = {
        case Resume() => {
            PausablePlayer.resume()
            context become playing
        }

        case Stop() => {
            PausablePlayer.stop()
            context become stopped
        }

        case Play(playable: Playable) => {
            PausablePlayer.stop()
            println("Stop & Playing...")
            PausablePlayer.play(playable)
        }
    }

    def receive = stopped // Start out as stopped
}

object PausablePlayer {

    var player: javazoom.jl.player.Player = _
    var thread: Thread = _
    var halt = false
    var remainingSongs: Queue[Song] = _

    def play(playable: Playable){
        playable match {
            case song: Song         => remainingSongs = Queue(song)
            case playlist: Playlist => remainingSongs = Queue(Random.shuffle(playlist.songs): _*)
        }

        player =  new Player(new BufferedInputStream(new FileInputStream(remainingSongs.dequeue.filename)))
        runThread()
    }

    def playInternal() {
        halt = false

        println("Playing...")

        // Play first song
        var end = false
        while (!end && !halt) {
            end = !player.play(1)
        }

        // Play remaining songs if any
        while(!halt && !remainingSongs.isEmpty) {
            player =  new Player(new BufferedInputStream(new FileInputStream(remainingSongs.dequeue.filename)))
            var end = false
            while (!end && !halt) {
                end = !player.play(1)
            }
        }
    }

    def stop() { // Or Pause don't do player.stop()
        halt = true
        thread.join()
    }

    def resume() {
        runThread()
    }

    private def runThread(){
        val run = new Runnable() {
            override def run() {
                playInternal()
            }
        }
        thread = new Thread(run)
        thread.setDaemon(true)
        thread.setPriority(Thread.MAX_PRIORITY)
        thread.start()
    }

}

abstract class Playable

class Song(name: String, path: String) extends Playable {
    def filename = path

    override def toString() = s"name: $name path: $path"
}

// Companion object
object Song {
    def apply(name: String, path: String) = new Song(name: String, path: String)
}

class Playlist(name: String) extends Playable {
    val paths = Map("good-mood" -> "E:/Projects/Mobile/good-mood/",
                    "cool-off"  -> "E:/Projects/Mobile/cool-off/",
                    "alarm-clock" -> "E:/Projects/Mobile/alarm-clock/")

    private def getListOfFiles(dir: String): List[File] = {
        val d = new File(dir)
        if (d.exists && d.isDirectory) {
            d.listFiles.filter(_.isFile).toList
        } else {
            List[File]()
        }
    }

    def songs: List[Song] = getListOfFiles(paths(name)).filter(_.getName().endsWith(".mp3")).map(
        file => new Song(file.getName(), file.getAbsolutePath())
    )
}

// Companion object
object Playlist {
    def apply(name:String) = new Playlist(name)
}
