$(function() {
    var newTable = $('#new_user_table').dataTable({
        "bJQueryUI": true,
        "sPaginationType": "full_numbers",
        "aoColumnDefs": [ 
            { "bSortable": false, "aTargets": [ 1, 2 ] }
        ],
    });
    var existingTable = $('#existing_user_table').dataTable({
        "bJQueryUI": true,
        "sPaginationType": "full_numbers",
        "aoColumnDefs": [ 
            { "bSortable": false, "aTargets": [ 2, 3 ] }
        ],
    });
    var playerTable = $('#player_table').dataTable({
        "bJQueryUI": true,
        "sPaginationType": "full_numbers",
        "aoColumnDefs": [ 
            { "bSortable": false, "aTargets": [ 1 ] }
        ],
    });
    var playerTable = $('#map_table').dataTable({
        "bJQueryUI": true,
        "sPaginationType": "full_numbers",
        "aoColumnDefs": [ 
            { "bSortable": false, "aTargets": [ 3 ] }
        ],
    });


    $('.dataTables_wrapper').attr("style", "width:470px; margin:8");
    $('#download').button().click(function() {
        document.location='/bs-worker.tar.gz';
    });
    
});

function togglePlayer(id, show) {
    $.ajax({
        url: "admin_action",
        data: "playerid=" + id + "&cmd=toggle_player",
        success: function(data) {
            var table = $("#player_table").dataTable();
            var rowIndex;
            var rows = table.fnGetNodes();
            for (rowIndex in rows) {
                if (parseInt($($(rows[rowIndex]).children()[0]).attr("playerid")) === id) {
                    var button = $("<input type='button' />")
                    .attr("value", (show ? "Hide" : "Show"))
                    .click(function() {
                        togglePlayer(id, !show);
                    });
                    $($(rows[rowIndex]).children()[1]).empty();
                    $($(rows[rowIndex]).children()[1]).append(button);
                    break;
                }
            }
        },
    });
}

function toggleMap(id, show) {
    $.ajax({
        url: "admin_action",
        data: "mapid=" + id + "&cmd=toggle_map",
        success: function(data) {
            var table = $("#map_table").dataTable();
            var rowIndex;
            var rows = table.fnGetNodes();
            for (rowIndex in rows) {
                if (parseInt($($(rows[rowIndex]).children()[0]).attr("mapid")) === id) {
                    var button = $("<input type='button' />")
                    .attr("value", (show ? "Hide" : "Show"))
                    .click(function() {
                        toggleMap(id, !show);
                    });
                    $($(rows[rowIndex]).children()[5]).empty();
                    $($(rows[rowIndex]).children()[5]).append(button);
                    break;
                }
            }
        },
    });
}

// Makes an asynchronous call to change a user's status
function manageUser(id, username, userid, cmd) {
    $.ajax({
        url: "admin_action",
        data: "userid=" + userid + "&cmd=" + cmd,
        success: function(data) {
            process(id, username, userid, cmd, data);
        },
    });
}

// Handle the response of an asynchronous user change
function process(id, username, userid, cmd, response) {
    var table = $('#' + id).dataTable();
    var row_index;
    var rows = table.fnGetNodes();
    for (row_index in rows) {
        if ($($(rows[row_index]).children()[0]).html() === username) {
            break;
        }
    }
    if (response == "admin_limit") {
        bsAlert("error", "Must have at least one admin", 5);
    }
    else if (response == "success") {
        if (cmd == "delete") {
          table.fnDeleteRow(row_index);
        } else if (cmd == "accept") {
            table.fnDeleteRow(row_index);
            other_table = $("#existing_user_table").dataTable();
            other_table.fnAddData([
                username,
                "normal",
                "<input id='temp1' type='button'/>",
                "<input id='temp2' type='button'/>",
            ]);
            $("#temp1").removeAttr("id").attr("value", "Promote").click(function() {
                manageUser('existing_user_table',username,userid,'make_admin');
            });
            $("#temp2").removeAttr("id").attr("value", "Delete user").click(function() {
                manageUser('existing_user_table',username,userid,'delete');
            });
        }
        else if (cmd == "make_admin") {
            var row = $(rows[row_index]);
            $(row.children()[1]).html("admin");
            var td = $(row.children()[2]);
            td.empty();
            var button = $('<input />', {
                type: "button",
                value: "Demote",
            });
            button.click(function() {
                manageUser('existing_user_table', username, userid, 'remove_admin');
            });
            button.appendTo(td);
        }
        else if (cmd == "remove_admin") {
            var row = $(rows[row_index]);
            $(row.children()[1]).html("normal");
            var td = $(row.children()[2]);
            td.empty();
            var button = $('<input />', {
                type: "button",
                value: "Promote",
            });
            button.click(function() {
                manageUser('existing_user_table', username, userid, 'make_admin');
            });
            button.appendTo(td);
        }
    }
}
