


function removeSong(songName, playlistName){
	console.log("removeSong()");
	var URL ="/playlists/" + playlistName + "/" + songName;
	var $song = $(this);

	$.ajax({
		url: URL,
		type: "DELETE",
		success: function(result, status){
			console.log("begin_remove");
			showAlert("success", result);
			//var $table = $song.parents(".table");
			//alert($table);
			refreshPlaylist(playlistName);
			console.log("end_remove");
		},
		error: function(result, statut, error){showAlert("danger", result.responseText);}
	});
}

function showAlert(type, message){

	var notify = $.notify({
		message: message
	},{
		type: type,
		placement: {
			from: "top",
			align: "center"
		}
	});

	//notify.update({'progress': 50});
	//notify.update({'type': 'success', 'message': '<strong>Success</strong> Your page has been saved!', 'progress': 25});

	/*
	$("#alert > .alert").alert('close');
	$("#alert").html("<div class='alert alert-" + type + "' role='alert'>\
						" + message + "\
						<button type='button' class='close' data-dismiss='alert' aria-label='Close'>\
							<span aria-hidden='true'>&times;</span>\
						</button>\
					</div>");*/
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

jQuery(document).ready(function($) {

	//$.notify("Hello World");
/*
	$.notify({
		title: '<strong>Heads up!</strong>',
		message: 'You can use any of bootstraps other alert styles as well by default.'
	},{
		type: 'success',
		placement: {
			from: "top",
			align: "center"
		}
	},);
/*
	$.notify('<strong>Saving</strong> Do not close this page...', {
	   type: 'success',
	   allow_dismiss: false,
	   showProgressbar: true
   });*/

	$(document).on("mouseenter", "td", function(){
		$(this).find(".remove").show();
	}).on("mouseleave", "td", function(){
		$(this).find(".remove").hide();
	});



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
			//console.log("zone.dragenter");
		    e.stopPropagation();
		    e.preventDefault();
			/*
			var src = zone.attr("src").match(/[^\.\\_]+/) + "_over.jpg";
			zone.attr("src", src);
			$(this).css('border', '4px solid #0B85A1');*/
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
			//console.log("zone.dragleave");
		    e.stopPropagation();
		    e.preventDefault();
			//$(this).css('border', '4px dotted #0B85A1');
		});
		zone.on('drop', function (e)
		{
			//console.log("zone.drop");
		    e.preventDefault();
		    var files = e.originalEvent.dataTransfer.files;

			var src = zone.attr("src").replace("_over.jpg", ".jpg");
            zone.attr("src", src);
			zone.css('border', '4px solid transparent');

		    //We need to send dropped files to Server
		    handleFileUpload(files,zone);
		});





		$(document).on('dragenter', function (e)
		{
			//console.log("doc.dragenter");
			/*
			var src = zone.attr("src").match(/[^\.\_]+/) + "_over.jpg";
			zone.attr("src", src);

			zone.css('border', '4px dotted #0B85A1');*/
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
			//console.log("doc.dragleave");
			e.stopPropagation();
			e.preventDefault();

			var src = zone.attr("src").replace("_over.jpg", ".jpg");
			zone.attr("src", src);

			zone.css('border', '4px solid transparent');
		});


		var rowCount=0;
		function createStatusbar(zone)
		{
		     rowCount++;
		     var row="odd";
		     if(rowCount %2 ==0) row ="even";
		     this.statusbar = $("<div class='statusbar "+row+"'></div>");
		     this.filename = $("<div class='filename'></div>").appendTo(this.statusbar);
		     this.progressBar = $("<div class='progress progress-striped active'>  <div class='progress-bar' style='width: 45%'></div></div>").appendTo(this.statusbar);
		     this.abort = $("<span class='glyphicon glyphicon-remove' aria-hidden='true'></span>").appendTo(this.statusbar);
		     zone.after(this.statusbar);

		    this.setFileNameSize = function(name)
		    {
		        this.filename.html(name);
		    }
		    this.setProgress = function(progress)
		    {
		        var progressBarWidth =progress*this.progressBar.width()/ 100;
		        this.progressBar.find('div').animate({ width: progressBarWidth }, 10).html(progress + "% ");
		        if(parseInt(progress) >= 100)
		        {
		            this.abort.hide();
		        }
		    }
		    this.setAbort = function(jqxhr)
		    {
		        var sb = this.statusbar;
		        this.abort.click(function()
		        {
		            jqxhr.abort();
		            sb.hide();
		        });
		    }
		}

		function createProgressAlert(type, message){

			return $.notify({
				message: message
			},{
				type: type,
				showProgressbar: true,
				placement: {
					from: "top",
					align: "center"
				}
			});
		}



		function handleFileUpload(files,zone)
		{
		   for (var i = 0; i < files.length; i++)
		   {
		        var fd = new FormData();
		        fd.append('song', files[i]);

		        var status/* = new createStatusbar(zone); //Using this we can set progress.
		        status.setFileNameSize(files[i].name);
*/
				var notify = createProgressAlert('warning',"Playlist " + playlist + " : adding song " + files[i].name);

		        sendFileToServer(fd,status, notify);

		   }
		}



		function sendFileToServer(formData, status, notify)
		{
		    var uploadURL ="/playlists/" + playlist; //Upload URL
		    var extraData ={}; //Extra Data.

			//notify.update({'progress': 50});

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
		                        //status.setProgress(percent);
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
					//showAlert("success", result);
					refreshPlaylist(playlist);
					//status.setProgress(100);
					notify.update({'type': 'success', 'message': result, 'progress': 100});
		            //$("#status1").append("File upload Done<br>");
		        },
				error: function(result, statut, error){
					//showAlert("danger", result.responseText);
					notify.update({'type': 'danger', 'delay':0, 'showProgressbar':false, 'message': result.responseText, 'progress': 0});
				}
		    });

			notify.update('test', {onClose: function(){
				alert('abort');
				jqXHR.abort();
			}});

		    //status.setAbort(jqXHR);
		}
	}


	console.log("End App.js");
});
