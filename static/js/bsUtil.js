// Display a pretty alert at the top.  If duration is 0 it will be permanent
function bsAlert(type, message, duration, containerId) {
    var uiClass;
    if (type === "error") {
        uiClass = "ui-state-error";
    } else if (type === "alert") {
        uiClass = "ui-state-highlight";
    } else {
        uiClass = "ui-state-highlight";
    }
    if (containerId === undefined) {
        containerId = "alerts";
    }
    var id = Math.floor(Math.random()*100000);
    $("<p/>")
    .addClass(uiClass)
    .attr("style", "padding:10px")
    .attr("id", id)
    .html(message)
    .appendTo($("#" + containerId));
    $("<p class='ui-state-error' style='padding:10px'>Lost connection to server! Please refresh page.</p>")

    if (duration > 0) {
        setTimeout("$('#" + id + "').fadeOut(300, function(){$('#" + id + "').remove()})", 1000 * duration);
    }
    return id;
}

function bsRound(number, decimals) {
    return Math.round(number * 10 * decimals)/(10*decimals);
}
