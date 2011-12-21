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
    $("<iframe id='graphs' />")
    .attr("src", 'analysis_content.html?id=' + matchId)
    .attr("style", "width:1000px; height:600px; margin-left: -10px; position:absolute; top:100px;")
    .appendTo("body")
    ;

    $("<button id='closeButton'>Close</button>")
    .attr("style", "position:absolute; top:650px; left: 920px;")
    .button()
    .click(function() {
        $('#graphs').remove();
        $('#closeButton').remove();
        $('#navButton').remove();
    })
    .appendTo("body");

    $("<button id='navButton'>View as page</button>")
    .attr("style", "position:absolute; top:650px; left: 1300px;")
    .button()
    .click(function() {
        document.location='analysis.html?id=' + matchId;
    })
    .appendTo("body");
}
