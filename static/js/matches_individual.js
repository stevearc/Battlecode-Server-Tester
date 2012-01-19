$(function() {
    var runTable = $('#matches_table').dataTable({
        "bJQueryUI": true,
        "sPaginationType": "full_numbers",
        "aoColumnDefs": [ 
            { "bSortable": false, "aTargets": [ 6, 7 ] }
        ],
        "aLengthMenu": [10, 25, 50],
    });

    runTable.fnSort( [ [0,'asc'] ] );
});

function rowClick(matchId) {
    $('#container').remove();
    var container = $("<div/>").attr('id', 'container')
        .attr('style', 'text-align:center; width:1000px; margin-left:-10px; top:180px;')
        .addClass("overlay-contents")
        .appendTo("body");

    $("<iframe />")
        .attr("src", 'analysis_content.html?id=' + matchId)
        .attr("style", "width:1000px; height:620px; border:0;")
        .appendTo(container);

    var overlay = $("<div />")
    .addClass("overlay")
    .attr("id", "overlay")
    .click(function() {
        $("#container").remove();
            $("#overlay").remove();
        $("#matches_table_wrapper").show();
    })
    .appendTo("body");
    $("#scrimmage_table_wrapper").hide();
}
