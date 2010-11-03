function newRun(team_a, team_b) {
	if (team_a.length==0 || team_b.length==0) {
		alert("Must have a non-empty team name");
		return;
	}
	xmlhttp1=new XMLHttpRequest();
	xmlhttp1.onreadystatechange=function() {
		if (xmlhttp1.readyState==4 && xmlhttp1.status==200) {
			if (xmlhttp1.responseText == "err team_a") {
				alert("Must have a valid name for Team A");
			} else if (xmlhttp1.responseText == "err team_b") {
				alert("Must have a valid name for Team B");
			} else if (xmlhttp1.responseText != "success") {
				alert(xmlhttp1.responseText);
			} else {
        document.getElementById("team_a_button").value = "";
        document.getElementById("team_b_button").value = "";
			}
		}
	}
	xmlhttp1.open("GET","run.html?team_a="+team_a+"&team_b="+team_b,true);
	xmlhttp1.send();
}

function delRun(id, prompt) {
	if(prompt && !confirm("This will delete the run and all replay files.  Continue?")) {
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

function deleteTableRow(rowid) {
  table = document.getElementById("table");
  for (var i = 1; i < table.rows.length; i++) {
    id = table.rows[i].cells[0].innerHTML;
    if (id == rowid) {
      table.deleteRow(i);
      break;
    }
  }
}

function startRun(rowid) {
  table = document.getElementById("table");
  for (var i = 1; i < table.rows.length; i++) {
    id = table.rows[i].cells[0].innerHTML;
    if (id == rowid) {
      var matches_row = table.rows[i].cells[4];
      matches_row.innerHTML = "<a href=\"matches.html?id=" + rowid + "\">matches</a>";
      var status_row = table.rows[i].cells[5];
      status_row.innerHTML = "Running";
      var time_row = table.rows[i].cells[6];
      time_row.innerHTML = "<a id=\"cntdwn\" name=\"0\"></a>";
      var control_row = table.rows[i].cells[7];
      control_row.innerHTML = "<input type=\"button\" value=\"cancel\" onclick=\"delRun(" + rowid + ", false)\">";
      break;
    }
  }
}

function cancelRun(rowid) {
  table = document.getElementById("table");
  for (var i = 1; i < table.rows.length; i++) {
    id = table.rows[i].cells[0].innerHTML;
    if (id == rowid) {
      var matches_row = table.rows[i].cells[4];
      matches_row.innerHTML = "<a href=\"matches.html?id=" + rowid + "\">matches</a>";
      var status_row = table.rows[i].cells[5];
      status_row.innerHTML = "Complete";
      var time_row = table.rows[i].cells[6];
      var time = document.getElementById("cntdwn").innerHTML;
      time_row.innerHTML = time;
      var control_row = table.rows[i].cells[7];
      control_row.innerHTML = "<input type=\"button\" value=\"delete\" onclick=\"delRun(" + rowid + ", false)\">";
      break;
    }
  }
}

function insertTableRow(rowid, team_a, team_b) {
  table = document.getElementById("table");
  var row = table.insertRow(1);
  var id_row = row.insertCell(0);
  id_row.innerHTML = rowid;
  var team_a_row = row.insertCell(1);
  team_a_row.innerHTML = team_a;
  var team_b_row = row.insertCell(2);
  team_b_row.innerHTML = team_b;
  var wins_row = row.insertCell(3);
  wins_row.innerHTML = "0/0";
  var matches_row = row.insertCell(4);
  var status_row = row.insertCell(5);
  status_row.innerHTML = "Queued";
  var time_row = row.insertCell(6);
  var control_row = row.insertCell(7);
  control_row.innerHTML="<input type=\"button\" value=\"dequeue\" onclick=\"delRun(" + rowid + ", false)\">";
}

function matchFinished(rowid, win) {
  table = document.getElementById("table");
  for (var i = 1; i < table.rows.length; i++) {
    id = table.rows[i].cells[0].innerHTML;
    if (id == rowid) {
      var wins_row = table.rows[i].cells[3];
      split = wins_row.innerHTML.split("/");
      wins = parseInt(split[0]);
      maps = parseInt(split[1]);
      if (win)
        wins += 1;
      maps += 1;
      wins_row.innerHTML = wins + "/" + maps;
      break;
    }
  }
}

function runError(rowid) {
  table = document.getElementById("table");
  for (var i = 1; i < table.rows.length; i++) {
    id = table.rows[i].cells[0].innerHTML;
    if (id == rowid) {
      var status_row = table.rows[i].cells[5];
      status_row.innerHTML = "Error";
      var control_row = table.rows[i].cells[7];
      control_row.innerHTML = "<input type=\"button\" value=\"delete\" onclick=\"delRun(" + rowid + ", false)\">";
      break;
    }
  }
}

function handleServerResponse(response) {
  if (response != "") {
    args = response.split(",");
    cmd = args[0];
    if (cmd == "DELETE_TABLE_ROW") {
      deleteTableRow(args[1]);
    } else if (cmd == "INSERT_TABLE_ROW") {
      insertTableRow(args[1], args[2], args[3]);
    } else if (cmd == "START_RUN") {
      startRun(args[1]);
    } else if (cmd == "CANCEL_RUN") {
      cancelRun(args[1]);
    } else if (cmd == "MATCH_FINISHED") {
      matchFinished(args[1], parseInt(args[2]));
    } else if (cmd == "RUN_ERROR") {
      runError(args[1]);
    } else {
      alert(response);
    }
  }

  setTimeout("poll(handleServerResponse, \"table\");",100);
}

handleServerResponse("");
