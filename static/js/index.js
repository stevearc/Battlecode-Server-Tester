$(function() {
    $("#seed_selector").prop("selectedIndex", 0);
    numSeedsChange();
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

    $('#match-info').attr('class','ui-icon ui-icon-info').button().click(function() {
        $('#match-info-dialog').dialog("open");
    });
    $('#match-info-dialog').dialog({
        autoOpen:false,
        width:500,
        zIndex:1020,
    });

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

    var socket;  
    var host = "ws://" + document.location.hostname + ":" + 
        document.location.port + "/socket?channel=index";
    var socket = new WebSocket(host);  

    socket.onmessage = function(message){  
        if (message.data !== "") {
            args = message.data.split(",");
            cmd = args[0];
            if (cmd == "DELETE_TABLE_ROW") {
                deleteTableRow(args[1]);
            } else if (cmd == "INSERT_TABLE_ROW") {
                insertTableRow(args[1], args[2], args[3]);
            } else if (cmd == "START_RUN") {
                startRun(args[1]);
            } else if (cmd == "FINISH_RUN") {
                finishRun(args[1], args[2]);
            } else if (cmd == "MATCH_FINISHED") {
                matchFinished(args[1], args[2], args[3]);
            } else if (cmd == "ADD_MAP") {
                addMap(args[1], args[2]);
            } else if (cmd == "ADD_PLAYER") {
                addPlayer(args[1], args[2]);
            } else {
                console.log("Unknown command: " + cmd);
            }
        }
    }
    socket.onclose = function(){  
        bsAlert("error", "Lost connection to server! Please refresh page.");
    }             
});

// Navigate to view all matches for a run
function doNavMatches(id) {
    document.location="matches.html?id="+id;
}

function addPlayer(playerId, playerName) {
    $("#team_a_button").prepend($("<option />").attr("value", playerId).html(playerName));
    $("#team_b_button").prepend($("<option />").attr("value", playerId).html(playerName));
}

function addMap(mapId, mapName) {
    var table = $("#map_table").dataTable();
    table.fnAddData([
        "<input type='checkbox' value='" + mapId + "' />",
        mapName,
    ]);
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
    for(var i = 1; i < 21; i++) {
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
            bsAlert("error", "Map seeds must be positive integers", 5, "overlayAlerts");
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
        bsAlert("error", "Must select at least one map", 5, "overlayAlerts");
        return false;
    }

	if (team_a.length==0 || team_b.length==0) {
		bsAlert("error", "Must have a non-empty team name", 5, "overlayAlerts");
		return false;
	}

    var urlData = "cmd=run&team_a="+team_a+"&team_b="+team_b+"&seeds="+seeds.join()+"&maps="+maps.join();

    $.ajax({
        url: "action.html",
        data: urlData,
        error: function(data) {
			if (data === "err team_a") {
                bsAlert("error", "Must have a valid name for Team A", 5, "overlayAlerts");
			} else if (data === "err team_b") {
                bsAlert("error", "Must have a valid name for Team B", 5, "overlayAlerts");
			} else if (data === "err seed") {
                bsAlert("error", "Seeds must be positive integers", 5, "overlayAlerts");
			} else if (data === "err maps") {
                bsAlert("error", "Must select at least one map", 5, "overlayAlerts");
			} else {
                bsAlert("error", "Unknown error adding run", 5, "overlayAlerts");
                console.log(data);
            }
        },
    });
    return true;
}

// Send query to server to delete a run
function delRun(id) {
	if(!confirm("This will delete the run and all replay files.  Continue?")) {
		return;
	}
    $.ajax({
        url: "action.html",
        data: "cmd=delete&id="+id,
        success: function(data) {
            deleteTableRow(id);
        },
    });
}

// Send query to server to delete a run
function cancelRun(id) {
    $.ajax({
        url: "action.html",
        data: "cmd=cancel&id="+id,
    });
}

function dequeueRun(id) {
    $.ajax({
        url: "action.html",
        data: "cmd=dequeue&id="+id,
        success: function(data) {
            deleteTableRow(id);
        },
    });
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
function startRun(rowid) {
    var table = $("#run_table").dataTable();
    var rowIndex = indexFromRowid(rowid);
    table.fnUpdate("0%", rowIndex, 4);
    table.fnUpdate("<a id='cntdwn' />", rowIndex, 5);
    $("#cntdwn").attr("name", "0");
    table.fnUpdate("<input type='button' id='temp1' />", rowIndex, 6);
    $("#temp1").removeAttr("id").attr("value", "cancel").click(function() {
        cancelRun(rowid);
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
        delRun(rowid);
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
        dequeueRun(rowid);
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
