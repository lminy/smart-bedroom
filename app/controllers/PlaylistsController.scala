package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import akka.actor._

import java.io.File
import scala.collection.JavaConversions._

import scala.util.{Try, Success, Failure}


import java.io.BufferedInputStream;
import java.io.FileInputStream;

import javazoom.jl.player.Player;

//Concurency imports
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

import actors._

import play.api.Logger

object PlaylistsController{

    ///////////////
    // FUNCTIONS //
    ///////////////

    def playlists: Map[String, String] = (for{
        configuration <- play.Play.application.configuration.getConfigList("playlists")
    } yield (configuration.getString("name"), configuration.getString("path"))).toMap

/*
    val playlists = Map("good-mood" -> "E:/Projects/Mobile/good-mood",
                        "cool-off"  -> "E:/Projects/Mobile/cool-off",
                        "alarm-clock" -> "E:/Projects/Mobile/alarm-clock")
*/

    def maxDiskBufferHumanReadable: String = play.Play.application.configuration.getString("play.http.parser.maxDiskBuffer")

    /**
     * REQUIRES: maxDiskBufferHumanReadable is a size in megabytes
     */
    def maxDiskBuffer: Int = ("^\\d*".r findFirstIn maxDiskBufferHumanReadable).getOrElse("0").toInt * 1024 * 1024

    def getListOfFiles(dir: String):List[File] = {
        val d = new File(dir)
        if (d.exists && d.isDirectory) {
            d.listFiles.filter(_.isFile).toList
        } else {
            List[File]()
        }
    }

    def listSongs: Map[String,List[String]] = playlists.map{
        case (playlist, path) => (playlist -> (getListOfFiles(path).filter(_.getName().endsWith(".mp3"))).map(_.getName()))
    }

    def normalize(filename: String):String = filename.replaceAll("[^a-zA-Z0-9\\s\\._-]+", "")
}

@Singleton
class PlaylistsController @Inject() (system: ActorSystem) extends Controller {

    import PlaylistsController._
    import actors.ServoMoteurActor._

    val player = system.actorSelection("user/player")
    val interfaceKit = system.actorSelection("user/interfaceKit")
    val servo = system.actorSelection("user/servo")

    ///////////////////
    // HTTP HANDLERS //
    ///////////////////

    def index = Action {
        Ok(views.html.playlists(listSongs))
    }

    def getPlaylist(name: String) = Action {
        if(playlists.keys contains name){
            Ok(views.html.playlist(listSongs,name))
        }else{
            NotFound("The playlist you requested doesn't exist")
        }
    }

    def addSong(playlist: String) = Action(parse.multipartFormData) { request =>
        request.body.file("song").map { song =>
            val filename = normalize(song.filename);

            def moveSong(): Result = {
                song.ref.moveTo(new File(s"${playlists(playlist)}/$filename"))
                Created(s"Playlist $playlist : $filename added")
            }
            if(!(playlists contains playlist)){
                NotFound("The playlist you requested doesn't exist")
            }else if (song.contentType.getOrElse("") != "audio/mp3"){
                NotAcceptable(s"Error while adding $filename : This is not an MP3 song!")
            }else if (song.ref.file.length() > maxDiskBuffer){
                NotAcceptable(s"Error while adding $filename : The size of the file is too big! (max $maxDiskBufferHumanReadable)")
            }else{
                moveSong()
            }
        }.getOrElse {
            NotAcceptable("Missing song")
        }
    }

    def removeSong(playlist: String, songName: String) = Action {
        (for{
            path <- playlists.get(playlist)
            song <- getListOfFiles(path).find(file => file.getName().equals(songName))
        } yield{
            song.delete() match {
                case true => Ok(s"$songName removed")
                case false => InternalServerError("Error while removing the song")
            }
        }).getOrElse {
            NotFound("The song or the playlist don't exist")
        }
    }

    def getTemperature: Float = {
        import java.io._
        import java.net._

        val serverSocket = new DatagramSocket(5678) // Création du socket UDP
        val receiveData = Array.ofDim[Byte](1024)
        val receivePacket = new DatagramPacket(receiveData, receiveData.length)

        serverSocket.receive(receivePacket) // Récupération du paquet
        val sentence = new String(receivePacket.getData()) // Récupération des données du paquet
        serverSocket.close()

        Logger.debug(s"String temperature : $sentence")
        sentence.toFloat // Conversion du texte (temperature) en Float
    }

    def startPlaylist(name: String) = Action {
        import actors.InterfaceKitActor._
        Logger.debug("startPlaylist")

        if(name == "alarm-clock"){
            Logger.debug("Alarm-clock")
            Future {
                // Récupérer la température
                Logger.debug("Alarm-clock avant getTemperature")
                var temperature = getTemperature //getTemperatureFromZolertia
                while(temperature < 30){
                    interfaceKit ! TurnOn(4)
                    Logger.debug("Allumer chauffage")
                    Thread.sleep(20*1000)
                    temperature = getTemperature
                }
                interfaceKit ! TurnOff(4)
                Logger.debug("Eteindre chauffage")
            }
            servo ! Open()
        }
        player ! PlayerActor.Play(Playlist(name))
        //interfaceKit ! 21
        Ok("OK!")
    }

    def pausePlaylist(name: String) = Action {
        player ! PlayerActor.Pause()
        Ok("OK!")
    }

    def resumePlaylist(name: String) = Action {
        player ! PlayerActor.Resume()
        Ok("OK!")
    }

    def stopPlaylist(name: String) = Action {
        player ! PlayerActor.Stop()
        Ok("OK!")
    }

} // END Playlist Controller
