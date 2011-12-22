var column_map = {'ID' : 0, 'TEAM_A' : 1, 'TEAM_B' : 2, 'WINS' : 3, 'STATUS' : 4, 'TIME' : 5, 'CONTROL' : 6};
var lastheard = -1;
var lastheard_update = -1;
$(function() {
    $("#seed_selector").prop("selectedIndex", 0);
    $("#maps_checkbox").attr("checked", false);
    var runTable = $('#run_table').dataTable({
        "bJQueryUI": true,
        "sPaginationType": "full_numbers",
        "aoColumnDefs": [ 
            { "bSortable": false, "aTargets": [ 6 ] }
        ],
    });
    runTable.fnSort( [ [0,'desc'] ] );

    var mapTable = $('#map_table').dataTable({
        "bJQueryUI": true,
        "bFilter": false,
        "bSearchable": false,
        "bInfo": false,
        "bLengthChange": false,
        "iDisplayLength": 1000,
        "aoColumnDefs": [ 
            { "bSortable": false, "aTargets": [ 0 ] }
        ],
        "fnDrawCallback":function(){
            $('#map_table_paginate').attr('style',"display:none");
        }
    });
    mapTable.fnSort( [ [1,'asc'] ] );
    $("#maps_checkbox").click(toggleAllMaps);

    $(window).bind('resize', function () {
        runTable.fnAdjustColumnSizing();
    } );

    $("#newRunButton").button();
    $("#newRunButton").click(function() {
        $("#newRunForm").show();
    });
    $(".overlay").click(function() {
        $("#newRunForm").hide();
    });
    $("#startButton").button();
    $("#startButton").click(function() { 
        if (newRun()) {
            $("#newRunForm").hide();
        }
    });

    handleServerResponse("");
});

// Navigate to view all matches for a run
function doNavMatches(id) {
    document.location="matches.html?id="+id;
}

// Toggle all of the checkboxes in the New Run selection
function toggleAllMaps() {
    var maps_checkbox = $("#maps_checkbox");
    var checked = maps_checkbox.attr("checked") === "checked";
    $("#map_table tbody input").each(function() {
        if (checked) {
            $(this).attr("checked", "checked");
        } else {
            $(this).removeAttr("checked");
        }
    });
}

// When the number of seeds is changed, make the appropriate seed fields visible
function numSeedsChange() {
    var num_seeds = parseInt($('#seed_selector').attr("value"));
    for(var i = 1; i < 11; i++) {
        var seed = $('#seed' + i);
        if (i <= num_seeds) {
            seed.removeClass('ui-helper-hidden');
        } else {
            seed.addClass('ui-helper-hidden');
        }
    }
}

// Pull form data and send query to server to start a new run
function newRun() {
    var team_a = $("#team_a_button").attr("value");
    var team_b = $("#team_b_button").attr("value");
    var num_matches = parseInt($("#seed_selector").attr("value"));
    var seeds = [];
    for (var i = 1; i <= num_matches; i++) {
        var seed = $('#seed' + i + " input:first").attr("value");
        if (isNaN(seed) || seed <= 0) {
            alert("Map seeds must be positive integers");
            return false;
        }
        seeds.push(seed);
    }
    var maps = [];
    $("#map_table tbody tr").each(function() {
        var box = $(this).find('input');
        if (box.attr("checked") === "checked") {
            maps.push(box.attr('value'));
        }
    });
    if (maps.length == 0) {
        alert("Must select at least one map");
        return false;
    }
  var url = "run.html?team_a="+team_a+"&team_b="+team_b+"&seeds="+seeds.join()+"&maps="+maps.join();

	if (team_a.length==0 || team_b.length==0) {
		alert("Must have a non-empty team name");
		return false;
	}
	xmlhttp1=new XMLHttpRequest();
	xmlhttp1.onreadystatechange=function() {
		if (xmlhttp1.readyState==4 && xmlhttp1.status==200) {
			if (xmlhttp1.responseText == "err team_a") {
				alert("Must have a valid name for Team A");
			} else if (xmlhttp1.responseText == "err team_b") {
				alert("Must have a valid name for Team B");
			} else if (xmlhttp1.responseText == "err seed") {
				alert("Seeds must either be an integer");
			} else if (xmlhttp1.responseText == "err maps") {
				alert("Must select at least one map");
			} else if (xmlhttp1.responseText != "success") {
				alert(xmlhttp1.responseText);
			} else {
        document.getElementById("team_a_button").value = "";
        document.getElementById("team_b_button").value = "";
			}
		}
	}
	xmlhttp1.open("GET",url,true);
	xmlhttp1.send();
    return true;
}

// Send query to server to delete a run
function delRun(id, ask) {
	if(ask && !confirm("This will delete the run and all replay files.  Continue?")) {
		return;
	}
	xmlhttp2=new XMLHttpRequest();
	xmlhttp2.onreadystatechange=function() {
		if (xmlhttp2.readyState==4 && xmlhttp2.status==200) {
			if (xmlhttp2.responseText != "success") {
				alert(xmlhttp2.responseText);
			} 
		}
	}
	xmlhttp2.open("GET","delete.html?id="+id,true);
	xmlhttp2.send();
}

// Delete selected row of the table
function deleteTableRow(rowid) {
    var table = $("#run_table").dataTable();
    var rowIndex;
    var rows = table.fnGetNodes();
    for (rowIndex in rows) {
        if ($($(rows[rowIndex]).children()[0]).html() === rowid) {
            table.fnDeleteRow(rowIndex);
            break;
        }
    }
    return;
}

// Change table to reflect that a new run is starting
function startRun(rowid, num_matches) {
    total_num_matches = num_matches;
    current_num_matches = 0;
    var table = $("#run_table").dataTable();
    var rowIndex = indexFromRowid(rowid);
    table.fnUpdate("0%", rowIndex, 4);
    table.fnUpdate("<a id='cntdwn' />", rowIndex, 5);
    $("#cntdwn").attr("name", "0");
    table.fnUpdate("<input type='button' id='temp1' />", rowIndex, 6);
    $("#temp1").removeAttr("id").attr("value", "cancel").click(function() {
        delRun(rowid, false);
    });
}

function indexFromRowid(rowid) {
    var table = $('#run_table').dataTable();
    var rowIndex;
    var rows = table.fnGetNodes();
    for (rowIndex in rows) {
        if ($($(rows[rowIndex]).children()[0]).html() === rowid) {
            return parseInt(rowIndex);
        }
    }
}

// Change status of running match to finished
function finishRun(rowid, run_status) {
    var table = $("#run_table").dataTable();
    var rowIndex = indexFromRowid(rowid);
    table.fnUpdate(run_status, rowIndex, 4);
    table.fnUpdate($("#cntdwn").html(), rowIndex, 5);
    table.fnUpdate("<input type='button' id='temp1' />", rowIndex, 6);
    $("#temp1").removeAttr("id").attr('value', 'delete').click(function() {
        delRun(rowid, true);
    });
}

// Create a new row in the table
function insertTableRow(rowid, team_a, team_b) {
    var table = $("#run_table").dataTable();
    table.fnAddData([
        rowid,
        team_a,
        team_b,
        "0/0",
        "Queued",
        "",
        "<input type='button' id='temp1' />",
    ]);
    $('#temp1').parent().parent().children().click(function() {
        if ($(this).parent().children().index(this) != 6) {
            doNavMatches(rowid);
        }
    });
    $('#temp1').attr('value', 'dequeue')
    .click(function() {
        delRun(rowid, false);
    })
    .removeAttr('id');
}

// Update the progress for the current run
function matchFinished(rowid, percent, wins) {
    var table = $("#run_table").dataTable();
    var rowIndex = indexFromRowid(rowid);
    table.fnUpdate(percent, rowIndex, 4);
    table.fnUpdate(wins, rowIndex, 3);
}

// Direct the server's Comet response appropriately
function handleServerResponse(response) {
  if (response != "") {
    args = response.split(",");
    cmd = args[0];
    lastheard = args[1];
    if (cmd == "DELETE_TABLE_ROW") {
      deleteTableRow(args[2]);
    } else if (cmd == "INSERT_TABLE_ROW") {
      insertTableRow(args[2], args[3], args[4]);
    } else if (cmd == "START_RUN") {
      startRun(args[2], args[3]);
    } else if (cmd == "FINISH_RUN") {
      finishRun(args[2], args[3]);
    } else if (cmd == "MATCH_FINISHED") {
      matchFinished(args[2], args[3], args[4]);
    } else {
      console.log("Unknown command: " + response);
    }
  }

  setTimeout("poll(handleServerResponse, 'matches', lastheard);",100);
}

// Poll the server for information
function listenForTeamsUpdate(response) {
  if (response != "") {
    args = response.split(",");
    cmd = args[0];
    lastheard_update = args[1];
    if (cmd == "RELOAD") {
      document.location.reload(true);
    } else {
      //alert(response);
    }
  }

  setTimeout("poll(listenForTeamsUpdate, \"teams_update\", lastheard_update);",100);
}

