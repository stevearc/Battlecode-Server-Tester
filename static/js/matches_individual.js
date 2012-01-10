$(function() {
    var runTable = $('#matches_table').dataTable({
        "bJQueryUI": true,
        "sPaginationType": "full_numbers",
        "aoColumnDefs": [ 
            { "bSortable": false, "aTargets": [ 6 ] }
        ],
        "aLengthMenu": [10, 25, 50],
    });

    runTable.fnSort( [ [0,'asc'] ] );
});

function rowClick(matchId) {
    $('#container').remove();
    var container = $("<div/>").attr('id', 'container')
        .attr('style', 'position: absolute; text-align:center; width:1000px; margin-left:-10px; top:0px; z-index:1010; background: #FFF;')
        .appendTo("body");
    $("<iframe />")
        .attr("src", 'analysis_content.html?id=' + matchId)
        .attr("style", "width:1000px; height:630px; border:0;")
        .appendTo(container);

    var buttonContainer = $("<div />").appendTo(container);

    $("<button>Close</button>")
        .attr("style", "")
        .button()
        .click(function() {
            $("#container").remove();
            $("#matches_table_wrapper").show();
        })
        .appendTo(buttonContainer);
    /*
    $("<button>View in full page</button>")
        .attr("style", "")
        .button()
        .click(function() {
            document.location='analysis.html?id=' + matchId;
        })
        .appendTo(buttonContainer);
    */
    $("#matches_table_wrapper").hide();
}
