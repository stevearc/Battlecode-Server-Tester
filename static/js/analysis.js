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

    $("#buttonWrapper input").each(function() {
        $(this).click(function() {
            var index = $.inArray($(this).attr("name"), chartedKeys);
            if (index >= 0) {
                $(this).attr("checked", false);
                chartedKeys.splice(index,1);
            } else {
                $(this).attr("checked", "checked");
            }
            $(this).button("refresh");
            updateChart();
        });
    });
    updateChart();
    $("#resetZoom").button().click(function() {
        chart.resetZoom();
    });
    $("#back").button().click(function() {
        document.location=$(this).attr("name");
    });


});

var chart;
var chartedKeys = [];
var options = {
    axesDefaults: {
        min: 0,
    },
    axes: {
        xaxis: {
            max: rounds,
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
        showTooltip:false,
    },
};

var aColorPalatte = [
    "E82E0C",
    "FF6E00",
    "E89B00",
    "FFDB00",
];

var bColorPalatte = [
    "0020E8",
    "5F00FF",
    "00A2FF",
    "0CE8C9",
];

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
