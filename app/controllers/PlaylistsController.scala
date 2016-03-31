package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import java.io.File
import scala.collection.JavaConversions._

import scala.util.{Try, Success, Failure}


/**
* This controller creates an `Action` to handle HTTP requests to the
* application's home page.
*/
@Singleton
class PlaylistsController @Inject() extends Controller {
/*
    val playlists = Map("good-mood" -> "E:/Projects/Mobile/good-mood",
                        "cool-off"  -> "E:/Projects/Mobile/cool-off",
                        "alarm-clock" -> "E:/Projects/Mobile/alarm-clock")
*/
    def playlists: Map[String, String] = (for{
        configuration <- play.Play.application.configuration.getConfigList("playlists")
    } yield (configuration.getString("name"), configuration.getString("path"))).toMap

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

    /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
    def index = Action {
/*
        val songs = for(
            (playlist, path) <- playlists
            file <- getListOfFiles(path)
            if(file.getName().endsWith(".mp3"))
        ) yield (file.getName ->
*/
        Ok(views.html.playlists(listSongs))
    }

    def getPlaylist(name: String) = Action {
        if(playlists.keys contains name){
            Ok(views.html.playlist(listSongs,name))
        }else
            NotFound("The playlist you requested doesn't exist")

    }

    def normalize(filename: String):String = filename.replaceAll("[^a-zA-Z0-9\\s\\._-]+", "")

    def addSong(playlist: String) = Action(parse.multipartFormData) { request =>
        Ok("test")
        /*
        if (request.body.isMaxSizeExceeded()){
            NotAcceptable(s"Error while adding ${song.filename} : The size of the file is too big! (max 100MB)")
        }else{*/
            request.body.file("song").map { song =>
                def moveSong(): Result = {
                    song.ref.moveTo(new File(s"E:/Projects/Mobile/$playlist/${normalize(song.filename)}"))
                    Created(s"Playlist $playlist : ${normalize(song.filename)} added")
                }
                //if(playlists contains playlist) moveSong() else NotFound("The playlist you requested doesn't exist")
                if(!(playlists contains playlist)){
                    NotFound("The playlist you requested doesn't exist")
                }else if (song.contentType.getOrElse("") != "audio/mp3")
                {
                    NotAcceptable(s"Error while adding ${song.filename} : This is not an MP3 song! Contet '${song.contentType.getOrElse("")}' '${song.ref.file.length()}'")
                }else if (song.ref.file.length() > maxDiskBuffer){
                    NotAcceptable(s"Error while adding ${song.filename} : The size of the file is too big! (max $maxDiskBufferHumanReadable)")
                }
                else{
                    moveSong()
                }
            }.getOrElse {
                NotAcceptable("Missing song")
            }
            /*).recover{
                case MaxLengthLimitAttained(ex) => InternalServerError("The size of the song is too big")
            }*/
        //}
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


        /*
        playlists.get(playlist) match {
            case Some(path) => {
                getListOfFiles(path).find(file => file.getName().equals(songName)) match{
                    case Some(song) => {
                        song.delete() match {
                            case true => Ok("song removed")
                            case false => InternalServerError("Error while removing the song")
                        }
                    }
                    case None => NotAcceptable("The song doesn't exist")
                }
                //.getOrElse{NotAcceptable("Song doesn't exist")}



                val songs = for {
                    file <- getListOfFiles(path)
                    if file.getName().endsWith(".mp3")
                } yield _.getName()


            }
            case None => NotAcceptable("The playlist doesn't exist")
        }
        */
    }
}
