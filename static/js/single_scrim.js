$(function() {
    var scrimTable = $('#scrimmage_table').dataTable({
        "bJQueryUI": true,
        "bFilter": false,
        "bSearchable": false,
        "bLengthChange": false,
        "iDisplayLength": 1000,
    });
    scrimTable.fnSort( [ [0,'asc'] ] );

    $(window).bind('resize', function () {
        scrimTable.fnAdjustColumnSizing();
    });
});

function rowClick(scrimId) {
    $('#container').remove();
    var container = $("<div/>").attr('id', 'container')
        .attr('style', 'text-align:center; width:1000px; margin-left:-10px; top:180px;')
        .addClass("overlay-contents")
        .appendTo("body");

    $("<iframe />")
        .attr("src", 'analysis_content.html?id=' + scrimId + "&scrimmage=true")
        .attr("style", "width:1000px; height:600px; border:0;")
        .appendTo(container);

    var overlay = $("<div />")
    .addClass("overlay")
    .attr("id", "overlay")
    .click(function() {
        $("#container").remove();
            $("#overlay").remove();
        $("#scrimmage_table_wrapper").show();
    })
    .appendTo("body");
    $("#scrimmage_table_wrapper").hide();
}
