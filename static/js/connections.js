$(function() {
    var connTable = $('#conn_table').dataTable({
        "bJQueryUI": true,
        "sPaginationType": "full_numbers",
        "aoColumnDefs": [ 
            { "bSortable": false, "aTargets": [ 1 ] }
        ],
    });

    var socket;  
    var host = "ws://" + document.location.hostname + ":" + 
        document.location.port + "/socket?channel=connections";
    var socket = new WebSocket(host);  

    socket.onmessage = function(message){  
        if (message.data !== "") {
            args = message.data.split(",");
            cmd = args[0];
            if (cmd == "DELETE_TABLE_ROW") {
                removeConn(args[1]);
            } else if (cmd == "INSERT_TABLE_ROW") {
                addConn(args[1]);
            } else if (cmd == "ADD_MAP") {
                addMap(args[1], args[2]);
            } else if (cmd == "REMOVE_MAP") {
                removeMap(args[1], args[2]);
            } else if (cmd == "FINISH_RUN") {
                clearMaps();
            } else {
                console.log("Unknown command: " + cmd);
            }
        }
    }
    socket.onclose = function(){  
        // TODO: auto-reconnect
        $("<p class='ui-state-error' style='padding:10px'>Lost connection to server! Please refresh page.</p>")
        .appendTo($("#alerts"));
    } 
});

// Find what row the connection is in
function getRow(conn) {
    var table = $('#conn_table').dataTable();
    var row_index;
    var rows = table.fnGetNodes();
    for (row_index in rows) {
        if ($($(rows[row_index]).children()[0]).html() === conn) {
            return row_index;
        }
    }
    return;
}

// Remove a map from the list of maps a connection is running
function removeMap(conn, map) {
    var table = $("#conn_table").dataTable();
    var rowIndex = getRow(conn);
    var row = table.fnGetData(rowIndex);
    var new_maps = row[1].split(", ");
    var index = new_maps.indexOf(map);
    if (index > -1) {
        new_maps.splice(index, 1);
    }
    if (new_maps.length > 1) {
        table.fnUpdate(new_maps.join(", "), rowIndex, 1);
    } else {
        table.fnUpdate("", rowIndex, 1);
    }
}

// Add a map to the list of maps a connection is running
function addMap(conn, map) {
    var table = $("#conn_table").dataTable();
    var rowIndex = getRow(conn);
    var row = table.fnGetData(rowIndex);
    var newMapsCol;
    if (row[1] === "") {
        newMapsCol = map;
    } else {
        var new_maps = row[1].split(", ");
        new_maps.push(map);
        newMapsCol = new_maps.join(", ");
    }
    table.fnUpdate(newMapsCol, rowIndex, 1);
}

// Add a new connection to the table
function addConn(conn) {
    var table = $("#conn_table").dataTable();
    table.fnAddData([
        conn,
        "",
    ]);
}

// Remove a connection from the table
function removeConn(conn) {
    var table = $("#conn_table").dataTable();
    table.fnDeleteRow(getRow(conn));
}

// Remove all maps from all connections
function clearMaps() {
    var table = $("#conn_table").dataTable();
    for (index in table.fnGetNodes()) {
        table.fnUpdate("", index, 1);
    }
}
