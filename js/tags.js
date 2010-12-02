function saveTags() {
  var table = document.getElementById("tag_table");
  var revs = "";
  var aliases = "";
  for (var i = 1; i < table.rows.length; i++) {
    var row = table.rows[i];
    revs += row.cells[0].innerHTML + ",";
    var txt = row.cells[1].getElementsByTagName("input")[0];
    aliases += txt.value + ",";
  }
  var button = document.getElementById("save_tags");
  button.value = "Saving...";
  button.onclick = "";
  query("GET", "tags.html", "revs="+encodeURI(revs)+"&aliases="+encodeURI(aliases), function (response) {document.location.reload(true)});
}

function keyDown(i) {
  var box = document.getElementById("box" + i);
  box.className = "changed";
}
