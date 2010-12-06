// Makes an asynchronous call to change a user's status
function manageUser(id, username, cmd) {
  query("GET", "admin_action", "username="+username+"&cmd="+cmd, function(text) {process(id, username, cmd, text);});
}

// Handle the response of an asynchronous user change
function process(id, username, cmd, response) {
  var table = document.getElementById(id);
  var row_index = -1;
  for (var i = 1; i < table.rows.length; i++) {
    var name_cell = table.rows[i].cells[0];
    if (name_cell.innerHTML == username) {
      row_index = i;
      break;
    }
  }
  if (response == "admin_limit") {
    alert("Must have at least one admin");
  }
  else if (response == "success") {
    if (cmd == "delete")
      table.deleteRow(row_index);
    else if (cmd == "accept") {
      table.deleteRow(row_index);
      other_table = document.getElementById("existing_user_table");
      var len = other_table.rows.length;
      var row = other_table.insertRow(len);
      var name_cell = row.insertCell(0);
      name_cell.innerHTML = username;
      var status_cell = row.insertCell(1);
      status_cell.innerHTML = "normal";
      var promote_cell = row.insertCell(2);
      promote_cell.innerHTML = "<input type='button' value='Promote' onClick='manageUser(\"existing_user_table\", \"" + username + "\", \"make_admin\")'>";
      var delete_cell = row.insertCell(3);
      delete_cell.innerHTML = "<input type='button' value='Delete user' onClick='manageUser(\"existing_user_table\", \"" + username + "\", \"delete\")'>";
    }
    else if (cmd == "make_admin") {
      var row = table.rows[row_index];
      row.cells[1].innerHTML = "admin";
      row.cells[2].innerHTML = "<input type='button' value='Demote' onClick='manageUser(\"existing_user_table\", \"" + username + "\", \"remove_admin\")'>";
    }
    else if (cmd == "remove_admin") {
      var row = table.rows[row_index];
      row.cells[1].innerHTML = "normal";
      row.cells[2].innerHTML = "<input type='button' value='Promote' onClick='manageUser(\"existing_user_table\", \"" + username + "\", \"make_admin\")'>";
    }
  }
}
