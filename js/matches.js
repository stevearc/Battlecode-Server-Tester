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
