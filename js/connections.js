var row_map = {'CONN' : 0, 'MAPS' : 1};
var lastheard = -1;

function getRow(conn) {
  var table = document.getElementById("conn_table");
	for (var i = 1; i < table.rows.length; i++) {
    id = table.rows[i].cells[row_map['CONN']].innerHTML;
    if (id == conn) {
      return table.rows[i];
		}
	}
	return null;
}

function removeMap(conn, map) {
  //alert("removing: '" + map + "'");
  var table = document.getElementById("conn_table");
	var row = getRow(conn);
	var maps = row.cells[row_map['MAPS']].innerHTML;
	var new_maps = maps.split(", ");
	var index = new_maps.indexOf(map);
	if (index > -1)
		new_maps.splice(index, 1);
  if (new_maps.length == 0)
    new_maps.push("&nbsp;");
	row.cells[row_map['MAPS']].innerHTML = new_maps.join(", ");
}

function addMap(conn, map) {
	var row = getRow(conn);
	var maps = row.cells[row_map['MAPS']].innerHTML;
	var new_maps = maps.split(", ");
  if (new_maps[0] == "&nbsp;")
    new_maps.splice(0, 1);
	new_maps.push(map);
	row.cells[row_map['MAPS']].innerHTML = new_maps.join(", ");
}

function addConn(conn) {
  var table = document.getElementById("conn_table");
  var row = table.insertRow(1);
  var conn_row = row.insertCell(row_map['CONN']);
  conn_row.innerHTML = conn;
  var maps_row = row.insertCell(row_map['MAPS']);
  maps_row.innerHTML = "&nbsp;";
}

function removeConn(conn) {
  var table = document.getElementById("conn_table");
  for (var i = 1; i < table.rows.length; i++) {
    id = table.rows[i].cells[row_map['CONN']].innerHTML;
    if (id == conn) {
      table.deleteRow(i);
      break;
    }
  }
}

function clearMaps() {
  var table = document.getElementById("conn_table");
  for (var i = 1; i < table.rows.length; i++) {
    table.rows[i].cells[row_map['MAPS']].innerHTML = "&nbsp;";
  }
}

function handleServerResponse(response) {
  if (response != "") {
    args = response.split(",");
    cmd = args[0];
    lastheard = args[1];
    if (cmd == "DELETE_TABLE_ROW") {
      removeConn(args[2]);
      conn_sorter.init();
      conn_sorter.search('query');
    } else if (cmd == "INSERT_TABLE_ROW") {
      addConn(args[2]);
      conn_sorter.init();
      conn_sorter.search('query');
    } else if (cmd == "ADD_MAP") {
      addMap(args[2], args[3]);
    } else if (cmd == "REMOVE_MAP") {
      removeMap(args[2], args[3]);
    } else if (cmd == "FINISH_RUN") {
      clearMaps();
    } else {
      //alert(response);
    }
  }

  setTimeout("poll(handleServerResponse, \"connections\", lastheard);",100);
}

handleServerResponse("");
