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
    case class StopAlarm() extends Message
    case class PauseAlarm() extends Message
    case class Pause() extends Message
    case class Resume() extends Message
}

class PlayerActor extends Actor {
    import PlayerActor._

    val player = new PausablePlayer()

    def stopped: Receive = {

        case Play(sound: Sound) => { // Un son peut se lancer en même temps qu'une musique :)
            val p = new PausablePlayer()
            p.play(sound)
        }

        case Play(playable: Playable) => {
            player.play(playable)
            context become playing
        }
    }

    def playing: Receive = {
        case Stop() => {
            player.stop()
            context become stopped
        }

        case StopAlarm() => {
            player.getPlayable match {
                case Playlist(name) if name == "alarm-clock" => {
                    player.stop()
                    context become stopped
                }
                case _ => {/* IGNORE STOP */}
            }
        }

        case PauseAlarm() => {
            player.getPlayable match {
                case Playlist(name) if name == "alarm-clock" => {
                    player.pause()
                    context become paused
                }
                case _ => {/* IGNORE STOP */}
            }
        }

        case Pause() => {
            player.pause()
            context become paused
        }

        case Play(sound: Sound) => { // Un son peut se lancer en même temps qu'une musique :)
            val p = new PausablePlayer()
            p.play(sound)
        }

        case Play(playable: Playable) => {
            player.stop()
            //println("Stop & Playing...")
            player.play(playable)
        }
    }

    def paused: Receive = {
        case Resume() => {
            player.resume()
            context become playing
        }

        case Stop() => {
            player.stop()
            context become stopped
        }

        case StopAlarm() => {
            player.getPlayable match {
                case Playlist(name) if name == "alarm-clock" => {
                    player.stop()
                    context become stopped
                }
                case _ => {/* IGNORE STOP */}
            }
        }

        case Play(sound: Sound) => { // Un son peut se lancer en même temps qu'une musique :)
            val p = new PausablePlayer()
            p.play(sound)
        }

        case Play(playable: Playable) => {
            player.stop()
            //println("Stop & Playing...")
            player.play(playable)
        }
    }

    def receive = stopped // Start out as stopped
}

class PausablePlayer {

    var player: javazoom.jl.player.Player = _
    var thread: Thread = _
    var halt = false
    var remainingSongs: Queue[Song] = _
    var playable:Playable = _

    def getPlayable = playable

    def play(playable: Playable){
        this.playable = playable
        playable match {
            case song: Song         => remainingSongs = Queue(song)
            case Sound(path)        => remainingSongs = Queue(Song(path, path))
            case playlist: Playlist => remainingSongs = Queue(Random.shuffle(playlist.songs): _*)
        }

        player =  new Player(new BufferedInputStream(new FileInputStream(remainingSongs.dequeue.filename)))
        runThread()
    }

    def playInternal() {
        halt = false

        //println("Playing...")

        // Play first song
        var end = false
        while (!end && !halt) {
            end = !player.play(1)
        }

        // Play remaining songs if any
        while(!halt && !remainingSongs.isEmpty) {
            player.close()
            player =  new Player(new BufferedInputStream(new FileInputStream(remainingSongs.dequeue.filename)))
            var end = false
            while (!end && !halt) {
                end = !player.play(1)
            }
        }
        if(!halt) player.close() // Only if end of queue but not when pause
    }

    def stop() {
        halt = true
        thread.join()
        player.close()
    }

    def pause() {
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

case class Song(name: String, path: String) extends Playable {
    def filename = path
}

case class Sound(path: String) extends Playable {
    def filename = path
}

case class Playlist(name: String) extends Playable {
    import controllers.PlaylistsController._

    private def getListOfFiles(dir: String): List[File] = {
        val d = new File(dir)
        if (d.exists && d.isDirectory) {
            d.listFiles.filter(_.isFile).toList
        } else {
            List[File]()
        }
    }

    def songs: List[Song] = getListOfFiles(playlists(name)).filter(_.getName().endsWith(".mp3")).map(
        file => new Song(file.getName(), file.getAbsolutePath())
    )
}
