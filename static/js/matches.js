$(function() {
    var byMatch = $('#byMatch').attr('checked') === 'checked';

    if (byMatch) {
        var runTable = $('#matches_table').dataTable({
            "bJQueryUI": true,
            "sPaginationType": "full_numbers",
            "aoColumnDefs": [ 
                { "bSortable": false, "aTargets": [ 5 ] }
            ],
        });
    } else {
        var runTable = $('#matches_table').dataTable({
            "bJQueryUI": true,
            "sPaginationType": "full_numbers",
        });
    }
    runTable.fnSort( [ [0,'asc'] ] );

    $("#viewStyle").buttonset();
});

function rowClick(matchId) {
    $('#graphs').remove();
    $('#closeButton').remove();
    $('#navButton').remove();
    var div = $("<div/>").attr('class','overlay').attr('id', 'overlay').click(function() {
        $('#overlay').remove();
        $('#graphs').remove();
        $('#closeButton').remove();
        $('#navButton').remove();
    }).appendTo("body");
    $("<iframe id='graphs' />")
    .attr("src", 'analysis_content.html?id=' + matchId)
    .attr("style", "width:1000px; height:600px; margin-left: -10px; position:fixed; top:100px; z-index:1010")
    .appendTo("body");

    $("<button id='navButton'>View in full page</button>")
    .attr("style", "position:fixed; top:660px; left: 900px; z-index:1020")
    .button()
    .click(function() {
        document.location='analysis.html?id=' + matchId;
    })
    .appendTo("body");
}
