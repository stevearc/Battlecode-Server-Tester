$(function() {
    var scrimTable = $('#scrim_table').dataTable({
        "bJQueryUI": true,
        "sPaginationType": "full_numbers",
        "aoColumnDefs": [ 
            { "bSortable": false, "aTargets": [ 5 ] }
        ],
    });
    scrimTable.fnSort( [ [0,'desc'] ] );

    $(window).bind('resize', function () {
        scrimTable.fnAdjustColumnSizing();
    } );

    $("#scrimmageForm").submit(function() {
        $("#scrimmageName").val($("#scrimFile").val().split("\\").pop());
    });

    var socket;  
    var host = "ws://" + document.location.hostname + ":" + 
        document.location.port + "/socket?channel=scrimmage";
    var socket = new WebSocket(host);  

    socket.onmessage = function(message){  
        if (message.data !== "") {
            args = message.data.split(",");
            cmd = args[0];
            if (cmd == "DELETE_TABLE_ROW") {
                deleteTableRow(args[1]);
            } else if (cmd == "INSERT_TABLE_ROW") {
                insertTableRow(args[1], args[2]);
            } else if (cmd == "START_SCRIMMAGE") {
                startScrimmage(args[1]);
            } else if (cmd == "FINISH_SCRIMMAGE") {
                finishScrimmage(args[1], args[2], args[3], args[4], args[5]);
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
    document.location="scrim.html?id="+id;
}

// Send query to server to delete a run
function delScrimmage(id) {
	xmlhttp2=new XMLHttpRequest();
	xmlhttp2.onreadystatechange=function() {
		if (xmlhttp2.readyState==4 && xmlhttp2.status==200) {
			if (xmlhttp2.responseText != "success") {
				console.log(xmlhttp2.responseText);
			} 
		}
	}
	xmlhttp2.open("GET","action.html?cmd=deleteScrim&id="+id,true);
	xmlhttp2.send();
}

// Delete selected row of the table
function deleteTableRow(rowid) {
    var table = $("#scrim_table").dataTable();
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

// Change table to reflect that a new scrimmage is starting
function startScrimmage(rowid) {
    var table = $("#scrim_table").dataTable();
    var rowIndex = indexFromRowid(rowid);
    table.fnUpdate("Running", rowIndex, 4);
    table.fnUpdate("<input type='button' id='temp1' />", rowIndex, 5);
    $("#temp1").removeAttr("id").attr('value', 'cancel').click(function() {
        delScrimmage(rowid);
    });
}

function indexFromRowid(rowid) {
    var table = $('#scrim_table').dataTable();
    var rowIndex;
    var rows = table.fnGetNodes();
    for (rowIndex in rows) {
        if ($($(rows[rowIndex]).children()[0]).html() === rowid) {
            return parseInt(rowIndex);
        }
    }
}

// Change status of running scrimmage to finished
function finishScrimmage(rowid, playerA, playerB, scrim_status, winner) {
    var winClass = "";
    if (myTeam !== "null") {
        if (myTeam !== playerA && myTeam !== playerB) {
            // We lost detection.  Take all those classes off.
            $("#scrim_table tr").removeClass("win").removeClass("loss");
        } else if ((winner === "A" && playerA === myTeam) || (winner === "B" && playerB === myTeam)) {
            winClass = "win";
        } else {
            winClass = "loss";
        }
    }

    var table = $("#scrim_table").dataTable();
    var rowIndex = indexFromRowid(rowid);
    table.fnUpdate(playerA, rowIndex, 2);
    table.fnUpdate(playerB, rowIndex, 3);
    table.fnUpdate(scrim_status, rowIndex, 4);
    table.fnUpdate("<input type='button' id='temp1' />", rowIndex, 5);
    $("#temp1").removeAttr("id").attr('value', 'delete').click(function() {
        delScrimmage(rowid);
    })
    .parent().parent().children().click(function() {
        if ($(this).parent().children().index(this) != 5) {
            doNavMatches(rowid);
        }
    })
    .parent().addClass(winClass);
}

// Create a new row in the table
function insertTableRow(rowid, fileName) {
    var table = $("#scrim_table").dataTable();
    table.fnAddData([
        rowid,
        fileName,
        "",
        "",
        "Queued",
        "<input type='button' id='temp1' />",
    ]);
    $('#temp1').attr('value', 'dequeue')
    .click(function() {
        delScrimmage(rowid);
    })
    .removeAttr('id');
}
