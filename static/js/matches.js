$(function() {
    var byMatch = $('#byMatch').attr('checked') === 'checked';
    $("#viewStyle").buttonset();
    buttonChange();
    $("#viewStyle input").click(function() {
        $("#viewStyle input").prop('checked', false);
        $(this).prop('checked', true);
        $("#viewStyle").buttonset();
        buttonChange();
    });
});

function buttonChange() {
    if ($("#byMatch").prop('checked')) {
        var iframe = $("#byMatchFrame");
        if (iframe.length === 0) {
            iframe = $("<iframe id='byMatchFrame' src='matches_individual.html" + 
                document.location.search + "'" + 
                "style='width:1000px; height: 2000px; border:0'/>")
                .appendTo("body");
        }
        iframe.show();
        $("#byMapFrame").hide();
    } else {
        var iframe = $("#byMapFrame");
        if (iframe.length === 0) {
            iframe = $("<iframe id='byMapFrame' src='matches_by_map.html" + 
                document.location.search + "'" + 
                "style='width:1000px; height: 2000px; border:0'/>")
                .appendTo("body");
        }
        iframe.show();
        $("#byMatchFrame").hide();
    }
}
