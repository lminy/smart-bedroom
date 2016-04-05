///////////////////
// NOTIFICATIONS //
///////////////////

function showAlert(type, message, progressbar = false, closeCallback = null){
	return $.notify({
		message: message
	},{
		type: type,
		showProgressbar: progressbar,
		placement: {
			from: "top",
			align: "center"
		},
		onClose: closeCallback
	});
}

///////////////////
// AJAX REQUESTS //
///////////////////

function removeSong(songName, playlistName){
	var URL ="/playlists/" + playlistName + "/" + songName;
	var $song = $(this);

	$.ajax({
		url: URL,
		type: "DELETE",
		success: function(result, status){
			showAlert("success", result);
			refreshPlaylist(playlistName);
		},
		error: function(result, statut, error){showAlert("danger", result.responseText);}
	});
}



function refreshPlaylist(name){
	var $table = $("#table-" + name);
	var URL ="/playlists/" + name;

	var $parent = $table.parent();
	$table.remove();

	$.ajax({
		url: URL,
		type: "GET",
		success: function(result, status){
			$parent.append(result);
		},
		error: function(result, statut, error){showAlert("danger", result.responseText);}
	});
}

function handleFileUpload(files, playlist)
{
   for (var i = 0; i < files.length; i++)
   {
		var fd = new FormData();
		fd.append('song', files[i]);

		sendFileToServer(fd, playlist, files[i].name);
   }
}

function sendFileToServer(formData, playlist, filename)
{
	var uploadURL ="/playlists/" + playlist; //Upload URL
	var extraData ={}; //Extra Data.

	var notify = showAlert('info', "Playlist " + playlist + " : adding song " + filename, true, function(){
		//jqXHR.abort(); // TODO: Gérer le abort de telle sorte qu'il ne s'effectue qu'au clic de la souris sur la croix et pas à chaque fois que la notification se ferme.
	});

	var jqXHR=$.ajax({
			xhr: function() {
			var xhrobj = $.ajaxSettings.xhr();
			if (xhrobj.upload) {
					xhrobj.upload.addEventListener('progress', function(event) {
						var percent = 0;
						var position = event.loaded || event.position;
						var total = event.total;
						if (event.lengthComputable) {
							percent = Math.ceil(position / total * 100);
						}
						//Set progress
						notify.update({'progress': percent});
					}, false);
				}
			return xhrobj;
		},
		url: uploadURL,
		type: "POST",
		contentType:false,
		processData: false,
		cache: false,
		data: formData,
		success: function(result, statut){
			refreshPlaylist(playlist);
			notify.update({'progress': 100});
			notify.close();
			showAlert("success", result);
		},
		error: function(result, statut, error){
			notify.close();
			if(error == "Request Entity Too Large"){ // error 413
				showAlert("danger", "The song " + filename + " is too big (max 100MB)");
			}else{
				showAlert("danger", result.responseText);
			}
		}
	});
}


//////////
// MAIN //
//////////

jQuery(document).ready(function($) {

	///////////////////////////////
	// SHOWING THE DELETE BUTTON //
	///////////////////////////////

	$(document).on("mouseenter", "td", function(){
		$(this).find(".remove").show();
	}).on("mouseleave", "td", function(){
		$(this).find(".remove").hide();
	});

	////////////////////////
	// FILE PICKER UPLOAD //
	////////////////////////

	$(".drag-and-drop").click(function(){
		$(this).parent().find("input[type='file']").trigger('click');
	});

	$("input[type='file']").on("change", function(){
		$(this).parent().find("input[type='submit']").submit();
	});

	$("input[type='submit']").submit(function(){
		var files = $(this).parent().find("input[type='file']")[0].files;
		var playlist = $(this).parent().attr("action");
		handleFileUpload(files, playlist);
	});

	//////////////////////////
	// DRAG AND DROP UPLOAD //
	//////////////////////////

	var $goodMood = $("#drag-and-drop-good-mood");
	var $coolOff = $("#drag-and-drop-cool-off");
	var $alarmClock = $("#drag-and-drop-alarm-clock");

	var goodMoodDragAndDrop = new DragAndDrop($goodMood, "good-mood");
	var coolOffDragAndDrop = new DragAndDrop($coolOff, "cool-off");
	var alarmClockDragAndDrop = new DragAndDrop($alarmClock, "alarm-clock");

	function DragAndDrop(zone, playlist) {

		zone.mouseover(function() {
            var src = $(this).attr("src").match(/[^\.\_]+/) + "_over.jpg";
            $(this).attr("src", src);
			zone.css('border', '4px dotted #0B85A1');
        })
        .mouseout(function() {
            var src = $(this).attr("src").replace("_over.jpg", ".jpg");
            $(this).attr("src", src);
			zone.css('border', '4px solid transparent');
        });

		zone.on('dragenter', function (e)
		{
		    e.stopPropagation();
		    e.preventDefault();
		});
		zone.on('dragover', function (e)
		{
		    e.stopPropagation();
		    e.preventDefault();
			var src = zone.attr("src").match(/[^\.\_]+/) + "_drop.jpg";
 			zone.attr("src", src);
 			$(this).css('border', '4px solid #0B85A1');
		});
		zone.on('dragleave', function (e)
		{
		    e.stopPropagation();
		    e.preventDefault();
		});
		zone.on('drop', function (e)
		{
		    e.preventDefault();
		    var files = e.originalEvent.dataTransfer.files;

			var src = zone.attr("src").replace("_over.jpg", ".jpg");
            zone.attr("src", src);
			zone.css('border', '4px solid transparent');

		    //We need to send dropped files to Server
		    handleFileUpload(files, playlist);
		});

		$(document).on('dragenter', function (e)
		{
		    e.stopPropagation();
		    e.preventDefault();
		});
		$(document).on('dragover', function (e)
		{
			e.stopPropagation();
			e.preventDefault();
			var src = zone.attr("src").match(/[^\.\_]+/) + "_over.jpg";
			zone.attr("src", src);
			zone.css('border', '4px dotted #0B85A1');
		});
		$(document).on('drop', function (e)
		{
		    e.stopPropagation();
		    e.preventDefault();

			var src = zone.attr("src").replace("_over.jpg", ".jpg");
			zone.attr("src", src);

			zone.css('border', '4px solid transparent');

		});
		$(document).on('dragleave', function (e)
		{
			e.stopPropagation();
			e.preventDefault();

			var src = zone.attr("src").replace("_over.jpg", ".jpg");
			zone.attr("src", src);

			zone.css('border', '4px solid transparent');
		});
	}

	console.log("app.js loaded");
});
