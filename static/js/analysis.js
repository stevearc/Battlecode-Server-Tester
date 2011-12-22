$(function() {
    $("#aViewButtons").buttonset();
    $("#bViewButtons").buttonset();
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
        document.location="matches.html?id=" + $(this).attr("name");
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

var nameMap = {
    aFluxIncome: "A: Flux Income",
    aFluxDrain: "A: Flux Drain",
    aFluxReserve: "A: Flux Reserve",
    aActiveRobots: "A: Active Robots",
    bFluxIncome: "B: Flux Income",
    bFluxDrain: "B: Flux Drain",
    bFluxReserve: "B: Flux Reserve",
    bActiveRobots: "B: Active Robots",
};

var colorMap = {
    aFluxIncome: "E82E0C",
    aFluxDrain: "FF6E00",
    aFluxReserve: "E89B00",
    aActiveRobots: "FFDB00",
    bFluxIncome: "0020E8",
    bFluxDrain: "5F00FF",
    bFluxReserve: "00A2FF",
    bActiveRobots: "0CE8C9",
};

function updateChart() {
    var array = [];
    chartedKeys = [];
    var seriesColors = [];
    $("#buttonWrapper input").each(function() {
        if ($(this).attr("checked") === "checked") {
            chartedKeys.push($(this).attr("name"));
            array.push(dataMap[$(this).attr("name")]);
            seriesColors.push(colorMap[$(this).attr("name")]);
        }
    });
    options['seriesColors'] = seriesColors;

    if (array.length == 0) {
        chart = $.jqplot('chart', [[[0,0]]], options);
    } else {
        chart = $.jqplot('chart', array, options);
        for (index in chartedKeys) {
            chart.series[index].label = nameMap[chartedKeys[index]];
            //chart.series[index].color = colorMap[chartedKeys[index]];
        }
    }
    chart.replot();
}
