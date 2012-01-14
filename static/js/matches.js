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
        if ($('#matches_table').size() === 0) {
            $.ajax({
                async: false,
                url: "matches_individual.html",
                data: "id=" + $("#match_id").html(),
                success: function(data) {
                    $("#individual_container").html(data);
                },
            });
        }
        $("#individual_container").show();
        $("#map_container").hide();
    } else {
        if ($('#matches_by_map_table').size() === 0) {
            $.ajax({
                async: false,
                url: "matches_by_map.html",
                data: "id=" + $("#match_id").html(),
                success: function(data) {
                    $("#map_container").html(data);
                },
            });
        }
        $("#individual_container").hide();
        $("#map_container").show();
    }
}
