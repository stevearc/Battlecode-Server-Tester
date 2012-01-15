$(function() {
    var index;
    var aButtons = $("#aViewButtons").children().children("select");
    for (index = 0; index < aButtons.length; index++) {
        $(aButtons[index].selectedIndex = viewLines[0][index]);
    }
    var bButtons = $("#bViewButtons").children().children("select");
    for (index = 0; index < bButtons.length; index++) {
        $(bButtons[index].selectedIndex = viewLines[0][index]);
    }

    $("#resetZoom").button().click(function() {
        chart.resetZoom();
    });
    $("#back").button().click(function() {
        document.location=$(this).attr("name");
    });

    $("#chartView").prop('checked', viewChart);
    $("#summaryView").prop('checked', !viewChart);
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
    var viewChart = false;
    if ($("#chartView").prop('checked')) {
        viewChart = true;
        $("#chart").show();
        $("#zoomContainer").show();
        $("#buttonWrapper").show();
        $("#summary").hide();
        updateChart();
        
    } else {
        $("#chart").hide();
        $("#zoomContainer").hide();
        $("#buttonWrapper").hide();
        $("#summary").show();
        displaySummary();
    }
    $.ajax({
        url: "analysis.html",
        type: "POST",
        data: "viewChart=" + viewChart,
    });
}

function displaySummary() {
    var tabs = $("#summary").tabs({
        show: function(e, ui) {
            $.ajax({
                url: "analysis.html",
                type: "POST",
                data: "summaryTab=" + ui.index,
            });
        },
    });
    tabs.tabs('option', 'selected', summaryTab);
    $("#summary tr").each(function() {
        $(this).attr("style", "height:20px");
        var tds = $(this).children();
        var name = $(this).attr("name");
        var aVal = bsRound(dataMap['a'+name][rounds-1][1], 1);
        var bVal = bsRound(dataMap['b'+name][rounds-1][1], 1);
        var aText = $("<div />")
        .html(aVal)
        .attr("style", "width:100%; text-align:center; color:white");
        var bText = $("<div />")
        .html(bVal)
        .attr("style", "width:100%; text-align:center; color:white");
        var aSpan = $("<div />")
        .html("&nbsp;")
        .attr("style", "background-color:" + aColorPalatte[0] + ";" +
        "width:" + (aVal === 0 ? "0" : (100*aVal/Math.max(aVal,bVal))) + "%;" + 
        "margin-top:-20px");
        var bSpan = $("<div />")
        .html("&nbsp;")
        .attr("style", "background-color:" + bColorPalatte[0] + ";" +
        "width:" + (bVal === 0 ? "0" : (100*bVal/Math.max(aVal,bVal))) + "%;" + 
        "margin-top:-20px");
        $(tds[1]).empty();
        $(tds[2]).empty();
        $(tds[1]).attr("style", "width:150px; background-color:" + darkAColor);
        $(tds[2]).attr("style", "width:150px; background-color:" + darkBColor);
        $(tds[1]).append(aText);
        $(tds[2]).append(bText);
        $(tds[1]).append(aSpan);
        $(tds[2]).append(bSpan);
    });
}

var chart;
var chartedKeys = [];
var options = {
    axesDefaults: {
        min: 0,
    },
    axes: {
        xaxis: {
            max: rounds,
            tickInterval: 500,
            numberTicks: Math.floor(rounds/500) + 1,
        },
    },
    seriesDefaults: {
        showMarker: false,
    },
    legend: {
        show: true,
    },
    cursor:{
        show: true,
        zoom: true,
    },
};

var aColorPalatte = [
    "E82E0C",
    "FF6E00",
    "E89B00",
    "FFDB00",
];
var darkAColor = "801907";

var bColorPalatte = [
    "0020E8",
    "5F00FF",
    "00A2FF",
    "0CE8C9",
];
var darkBColor = "000D5E";

function updateChart() {
    var array = [];
    chartedKeys = [];
    var seriesColors = [];
    var seriesIndex = 0;
    $("#aViewButtons select").each(function() {
        var name = $($(this).children("option")[this.selectedIndex]).attr("name")
        if (name !== "None") {
            chartedKeys.push(name);
            array.push(dataMap[name]);
            seriesColors.push(aColorPalatte[(seriesIndex++)%aColorPalatte.length]);
        }
    });
    seriesIndex = 0;
    $("#bViewButtons select").each(function() {
        var name = $($(this).children("option")[this.selectedIndex]).attr("name")
        if (name !== "None") {
            chartedKeys.push(name);
            array.push(dataMap[name]);
            seriesColors.push(bColorPalatte[(seriesIndex++)%bColorPalatte.length]);
        }
    });
    options['seriesColors'] = seriesColors;

    // Update the session preferences
    var aButtons = $("#aViewButtons").children().children("select");
    for (index = 0; index < aButtons.length; index++) {
        viewLines[0][index] = aButtons[index].selectedIndex;
    }
    var bButtons = $("#bViewButtons").children().children("select");
    for (index = 0; index < bButtons.length; index++) {
        viewLines[1][index] = bButtons[index].selectedIndex;
    }
    $.ajax({
        url: "analysis.html",
        type: "POST",
        data: "viewLines=" + viewLines,
    });

    if (array.length == 0) {
        chart = $.jqplot('chart', [[[0,0]]], options);
    } else {
        chart = $.jqplot('chart', array, options);
        for (index in chartedKeys) {
            chart.series[index].label = nameMap[chartedKeys[index]];
        }
    }
    chart.replot();
}
