
/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('PositionReportBubbleChartController', PositionReportBubbleChartController);

    function PositionReportBubbleChartController($scope, $http, $interval) {
        var vm = this;
        vm.initialLoad= true;
        vm.errorMessage = "";
        vm.chartObject = {};

        var refresh = $interval(loadData, 60000);
        var restQueryUrl = '/api/v1.0/pr/latest/';

        loadData();

        function loadData(){
            $http.get(restQueryUrl).success(function(data) {
                processGraphData(data);
                vm.errorMessage = "";
                vm.initialLoad = false;
            }).error(function(data, status, headers, config) {
                vm.errorMessage = "Server returned status " + status;
                vm.initialLoad = false;
            });
        }

        function processGraphData(data) {
            var chartObject = {};
            chartObject.type = "BubbleChart";
            chartObject.options = {
                    hAxis: {title: 'Series-Maturity', ticks: []},
                    vAxis: {title: 'Underlying', ticks: []},
                    backgroundColor: {
                        fill: 'transparent'
                    },
                    bubble: {textStyle: {color: 'none'}},
                    fontColor: 'black',
                    sortBubblesBySize: true
                };
            chartObject.data = {
                cols: [{
                    id: 'ID',
                    type: 'string'
                }, {
                    id: 'mmy',
                    type: 'number'
                }, {
                    id: 'underlying',
                    type: 'number'
                }, {
                    id: 'offset',
                    type: 'string'
                }, {
                    id: 'compVar',
                    type: 'number'
                }],
                rows: []
            };

            var bubbles = {};
            var series = {};
            var underlyings = {};
            var hIndex = 0;
            var vIndex = 0;

            for (var index = 0; index < data.length; ++index) {
                var bubbleKey = data[index].clearer + '-' + data[index].member + '-' + data[index].account + '-' + data[index].symbol + '-' + data[index].maturityMonthYear;
                var hAxisKey = data[index].symbol + '-' + data[index].maturityMonthYear;
                var vAxisKey = data[index].underlying;
                if (!(hAxisKey in series)) {
                    chartObject.options.hAxis.ticks.push({v: hIndex, f: hAxisKey});
                    series[hAxisKey] = hIndex;
                    hIndex++;
                }
                if (!(vAxisKey in underlyings)) {
                    chartObject.options.vAxis.ticks.push({v: vIndex, f: vAxisKey});
                    underlyings[vAxisKey] = vIndex;
                    vIndex++;
                }
                var radius = data[index].compVar;
                if (bubbleKey in bubbles) {
                    bubbles[bubbleKey].radius += radius;
                } else {
                    bubbles[bubbleKey] = {
                        hAxisKey: series[hAxisKey],
                        vAxisKey: underlyings[vAxisKey],
                        radius: radius
                    };
                }
            }
            for (var bubbleKey in bubbles) {
                chartObject.data.rows.push({c: [{
                            v: bubbleKey
                        }, {
                            v: bubbles[bubbleKey].hAxisKey
                        }, {
                            v: bubbles[bubbleKey].vAxisKey
                        }, {
                            v: bubbles[bubbleKey].radius >= 0 ? "contributing" : "offseting"
                        }, {
                            v: Math.abs(bubbles[bubbleKey].radius)
                        }
                    ]});

            }
            vm.chartObject = chartObject;
        }

        $scope.$on("$destroy", function() {
            if (refresh !== null) {
                $interval.cancel(refresh);
            }
        });
    };
})();