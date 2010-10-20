function newRun(team_a, team_b) {
	if (team_a.length==0 || team_b.length==0) {
		alert("Must have a non-empty team name");
		return;
	}
	xmlhttp=new XMLHttpRequest();
	xmlhttp.onreadystatechange=function() {
		if (xmlhttp.readyState==4 && xmlhttp.status==200) {
			if (xmlhttp.responseText == "err team_a") {
				alert("Must have a valid name for Team A");
			} else if (xmlhttp.responseText == "err team_b") {
				alert("Must have a valid name for Team B");
			} else if (xmlhttp.responseText != "success") {
				alert(xmlhttp.responseText);
			} else {
				location.reload(true);
			}
		}
	}
	xmlhttp.open("GET","run.html?team_a="+team_a+"&team_b="+team_b,true);
	xmlhttp.send();
}

function delRun(id, prompt) {
	if(prompt && !confirm("This will delete the run and all replay files.  Continue?")) {
		return;
	}
	xmlhttp=new XMLHttpRequest();
	xmlhttp.onreadystatechange=function() {
		if (xmlhttp.readyState==4 && xmlhttp.status==200) {
			if (xmlhttp.responseText != "success") {
				alert(xmlhttp.responseText);
			} else {
				location.reload(true);
			}
		}
	}
	xmlhttp.open("GET","delete.html?id="+id,true);
	xmlhttp.send();
}