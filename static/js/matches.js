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
    $('#container').remove();
    var div = $("<div/>").attr('class','overlay').attr('id', 'overlay').click(function() {
        $('#overlay').remove();
        $('#container').remove();
    }).appendTo("body");
    var container = $("<div/>").attr('id', 'container')
        .attr('style', 'position: absolute; top:100px; text-align:center; width:1000px; margin-left:-10px; z-index:1010; background: #FFF;')
        .appendTo("body");
    $("<iframe />")
        .attr("src", 'analysis_content.html?id=' + matchId)
        .attr("style", "width:1000px; height:570px; border:0;")
        .appendTo(container);

    $("<button>View in full page</button>")
        .attr("style", "")
        .button()
        .click(function() {
            document.location='analysis.html?id=' + matchId;
        })
        .appendTo(container);
}
