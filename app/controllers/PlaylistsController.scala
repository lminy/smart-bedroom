package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import java.io.File
import scala.collection.JavaConversions._

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

    def getListOfFiles(dir: String):List[File] = {
        val d = new File(dir)
        if (d.exists && d.isDirectory) {
            d.listFiles.filter(_.isFile).toList
        } else {
            List[File]()
        }
    }

    /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
    def index = Action {
        val songs: Map[String,List[String]] = playlists.map{
            case (playlist, path) => (playlist -> (getListOfFiles(path).filter(_.getName().endsWith(".mp3"))).map(_.getName()))
        }
/*
        val songs = for(
            (playlist, path) <- playlists
            file <- getListOfFiles(path)
            if(file.getName().endsWith(".mp3"))
        ) yield (file.getName ->
*/
        Ok(views.html.playlists(songs))
    }

    def addSong(playlist: String) = Action(parse.multipartFormData) { request =>
        request.body.file("song").map { song =>
            def moveSong(): Result = {
                song.ref.moveTo(new File(s"E:/Projects/Mobile/$playlist/${song.filename}"))
                Created("song added")
            }
            playlist match {
                case "good-mood" => moveSong()
                case "cool-off" => moveSong()
                case "alarm-clock" => moveSong()
                case _ => NotFound("The playlist you requested doesn't exist")
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
                case true => Ok("Song removed")
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
