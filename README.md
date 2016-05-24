Smart bedroom
=============

The smart bedroom projet by JM, XM, TD & LM.

Controllers
===========

- HomeController.scala:

  You can use it to interact with the bedroom

- GamesController.scala:

  You can use it to start games

- PlaylistsController.scala:

  You can use it to manage playlists

How to run it
=============

To run this application in developpment mode, just run `activator ~run`.

To run this application in production mode, packaged the file with `activator dist` then for linux just run with `webapp-1.0-SNAPSHOT/bin/webapp -Dconfig.file=webapp-1.0-SNAPSHOT/conf/production.conf`.
