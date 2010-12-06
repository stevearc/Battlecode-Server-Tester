var row_map = {'ID' : 0, 'TEAM_A' : 1, 'TEAM_B' : 2, 'WINS' : 3, 'STATUS' : 4, 'TIME' : 5, 'CONTROL' : 6};
var lastheard = -1;
var lastheard_update = -1;
document.getElementById('seed_selector').selectedIndex=0;
document.getElementById("maps_checkbox").checked=false;

function roundNumber(number, digits) {
  var multiple = Math.pow(10, digits);
  var rndedNum = Math.round(number * multiple) / multiple;
  return rndedNum;
}

function init() {
  table = document.getElementById("table");
  for (var i = 1; i < table.rows.length; i++) {
    var status_row = table.rows[i].cells[row_map['STATUS']];
    if (status_row.innerHTML == 'Running') {
      status_row.innerHTML = roundNumber((100*current_num_matches/total_num_matches), 2) + "%";
      break;
    }
  }
}

function doNavMatches(id) {
  document.location="matches.html?id="+id;
}

function toggleMapsCheckbox() {
  var maps_checkbox = document.getElementById("maps_checkbox");
  var checked = maps_checkbox.checked;
  var table = document.getElementById("map_table");
  for (var i = 1; i < table.rows.length; i++) {
    var cell = table.rows[i].cells[0];
    cell.getElementsByTagName("input")[0].checked = checked;
  }
}

function toggleMapsCheckbox(size) {
  var maps_checkbox = document.getElementById("maps_checkbox_" + size);
  var checked = maps_checkbox.checked;
  var table = document.getElementById("map_table");
  for (var i = 1; i < table.rows.length; i++) {
    var toCheck = checked;
    var cell = table.rows[i].cells[0];
    var isCellChecked = cell.getElementsByTagName("input")[0].checked;
    if (table.rows[i].cells[2].innerHTML != size) {
      toCheck = isCellChecked;
    }
    cell.getElementsByTagName("input")[0].checked = toCheck;
  }
}

function numSeedsChange() {
  var num_seeds = parseInt(document.getElementById('seed_selector').value);
  for(var i = 1; i < 11; i++) {
    var seed = document.getElementById('seed' + i);
    if (i <= num_seeds) {
      seed.className = '';
    } else {
      seed.className = 'removed';
    }
  }
}

function toggleNewRun() {
  var form = document.getElementById("add_run");
  if (form.className == "removed") {
    form.className = "";
    document.getElementById("team_a_button").focus();
  } else {
    form.className = "removed";
  }
}

function newRun() {
  var team_a = document.getElementById("team_a_button").value;
  var team_b = document.getElementById("team_b_button").value;
  var num_matches = parseInt(document.getElementById("seed_selector").value);
  var seeds = [];
  for (var i = 1; i <= num_matches; i++) {
    var seed = document.getElementById("seed" + i).getElementsByTagName("input")[0].value;
    if (isNaN(seed) || seed <= 0) {
      alert("Map seeds must be positive integers");
      return false;
    }
    seeds.push(seed);
  }
  var maps = [];
  var table = document.getElementById("map_table");
  for (var i = 1; i < table.rows.length; i++) {
    var cell = table.rows[i].cells[0];
    if (cell.getElementsByTagName("input")[0].checked) {
      maps.push(table.rows[i].cells[1].innerHTML);
    }
  }
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

function startRun(rowid, num_matches) {
  total_num_matches = num_matches;
  current_num_matches = 0;
  var clickEvent = 'doNavMatches("' + rowid + '")';
  var style = 'cursor:pointer';
  table = document.getElementById("table");
  for (var i = 1; i < table.rows.length; i++) {
    id = table.rows[i].cells[0].innerHTML;
    if (id == rowid) {
      for (key in row_map) {
        if (key == 'CONTROL')
          continue;
        table.rows[i].cells[row_map[key]].setAttribute('onClick', clickEvent);
        table.rows[i].cells[row_map[key]].setAttribute('style', style);
      }
      var status_row = table.rows[i].cells[row_map['STATUS']];
      status_row.innerHTML = "0%";
      var time_row = table.rows[i].cells[row_map['TIME']];
      time_row.innerHTML = "<a id=\"cntdwn\" name=\"0\"></a>";
      var control_row = table.rows[i].cells[row_map['CONTROL']];
      control_row.innerHTML = "<input type=\"button\" value=\"cancel\" onclick=\"delRun(" + rowid + ", false)\">";
      break;
    }
  }
}

function finishRun(rowid, run_status) {
  table = document.getElementById("table");
  for (var i = 1; i < table.rows.length; i++) {
    id = table.rows[i].cells[0].innerHTML;
    if (id == rowid) {
      var status_row = table.rows[i].cells[row_map['STATUS']];
      if (run_status == 2)
        status_row.innerHTML = "Complete";
      else if (run_status == 4)
        status_row.innerHTML = "Canceled";
      var time_row = table.rows[i].cells[row_map['TIME']];
      var time = document.getElementById("cntdwn").innerHTML;
      time_row.innerHTML = time;
      var control_row = table.rows[i].cells[row_map['CONTROL']];
      control_row.innerHTML = "<input type=\"button\" value=\"delete\" onclick=\"delRun(" + rowid + ", true)\">";
      break;
    }
  }
}

function insertTableRow(rowid, team_a, team_b) {
  table = document.getElementById("table");
  var row = table.insertRow(1);
  var id_row = row.insertCell(row_map['ID']);
  id_row.innerHTML = rowid;
  var team_a_row = row.insertCell(row_map['TEAM_A']);
  team_a_row.innerHTML = team_a;
  var team_b_row = row.insertCell(row_map['TEAM_B']);
  team_b_row.innerHTML = team_b;
  var wins_row = row.insertCell(row_map['WINS']);
  wins_row.innerHTML = "0/0";
  var status_row = row.insertCell(row_map['STATUS']);
  status_row.innerHTML = "Queued";
  var time_row = row.insertCell(row_map['TIME']);
  time_row.innerHTML="&nbsp;";
  var control_row = row.insertCell(row_map['CONTROL']);
  control_row.innerHTML="<input type=\"button\" value=\"dequeue\" onclick=\"delRun(" + rowid + ", false)\">";
}

function matchFinished(rowid, win) {
  current_num_matches += 1;
  table = document.getElementById("table");
  for (var i = 1; i < table.rows.length; i++) {
    id = table.rows[i].cells[0].innerHTML;
    if (id == rowid) {
      var wins_row = table.rows[i].cells[row_map['WINS']];
      split = wins_row.innerHTML.split("/");
      wins = parseInt(split[0]);
      losses = parseInt(split[1]);
      if (win)
        wins += 1;
      else
        losses += 1;
      wins_row.innerHTML = wins + "/" + losses;
      var status_row = table.rows[i].cells[row_map['STATUS']];
      status_row.innerHTML = roundNumber((100*current_num_matches/total_num_matches), 2) + "%";
      break;
    }
  }
}

function runError(rowid) {
  table = document.getElementById("table");
  for (var i = 1; i < table.rows.length; i++) {
    id = table.rows[i].cells[0].innerHTML;
    if (id == rowid) {
      var status_row = table.rows[i].cells[row_map['STATUS']];
      status_row.innerHTML = "Error";
      var control_row = table.rows[i].cells[row_map['CONTROL']];
      control_row.innerHTML = "<input type=\"button\" value=\"delete\" onclick=\"delRun(" + rowid + ", false)\">";
      break;
    }
  }
}

function handleServerResponse(response) {
  if (response != "") {
    args = response.split(",");
    cmd = args[0];
    lastheard = args[1];
    if (cmd == "DELETE_TABLE_ROW") {
      deleteTableRow(args[2]);
      sorter.init();
      sorter.search('query');
    } else if (cmd == "INSERT_TABLE_ROW") {
      insertTableRow(args[2], args[3], args[4]);
      sorter.init();
      sorter.search('query');
    } else if (cmd == "START_RUN") {
      startRun(args[2], args[3]);
    } else if (cmd == "FINISH_RUN") {
      finishRun(args[2], args[3]);
    } else if (cmd == "MATCH_FINISHED") {
      matchFinished(args[2], parseInt(args[3]));
    } else if (cmd == "RUN_ERROR") {
      runError(args[2]);
    } else {
      //alert(response);
    }
  }

  setTimeout("poll(handleServerResponse, \"matches\", lastheard);",100);
}

function doRepoUpdate() {
  listenForTeamsUpdate("");
  query("GET", "admin_action", "cmd=update", null);
  var button = document.getElementById("update_button");
  button.value = "updating...";
  button.onclick = "";
}

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

init();
handleServerResponse("");
