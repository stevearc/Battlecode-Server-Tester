$(function() {
    var runTable = $('#matches_by_map_table').dataTable({
        "bJQueryUI": true,
        "sPaginationType": "full_numbers",
        "aLengthMenu": [10, 25, 50],
    });

    runTable.fnSort( [ [0,'asc'] ] );
});
