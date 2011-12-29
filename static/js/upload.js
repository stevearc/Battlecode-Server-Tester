$(function() {
    $('#player-info').attr('class','ui-icon ui-icon-info').button().click(function() {
        $('#player-info-dialog').dialog("open");
    });
    $('#map-info').attr('class','ui-icon ui-icon-info').button().click(function() {
        $('#map-info-dialog').dialog("open");
    });
    $('#player-info-dialog').dialog({
        autoOpen:false,
        width:310,
    });
    $('#map-info-dialog').dialog({autoOpen:false});

    $("#mapForm").submit(function() {
        $("#mapName").val($("#mapFile").val().split("\\").pop());
    });
});

