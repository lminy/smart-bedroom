name := """webapp"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0-RC1" % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"


fork in run := true

// Added for debian package - LM

/*
lazy val root = (project in file(".")).enablePlugins(PlayScala, DebianPlugin)

maintainer in Linux := "Laurent Miny <laurent.miny@student.unamur.be>"

packageSummary in Linux := "Smart Bedroom"

packageDescription := "Smart Bedroom is a prototype"

*/
