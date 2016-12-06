
/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('PositionReportBubbleChartController', PositionReportBubbleChartController);

    function PositionReportBubbleChartController($scope, $http, $interval, $filter, hostConfig) {
        var vm = this;
        vm.initialLoad= true;
        vm.errorMessage = "";
        vm.chartObject = createEmptyChartObject();
        vm.accountSelection = { accountSet: {}, availableOptions: [], selectedOption: null };
        vm.selectionChanged = selectionChanged;
        vm.topRecordsCount = "20";

        var refresh = $interval(loadData, 60000);
        var restQueryUrl = hostConfig.restURL + '/pr/latest/';
        var bubblesMap = new Map();
        var compVarPositiveLegend = "Positive";
        var compVarNegativeLegend = "Negative";
        var positiveCoveragePerc = 0;
        var negativeCoveragePerc = 0;
        var totalCompVar;

        function createEmptyChartObject() {
            var chartObject = {};
            chartObject.type = "BubbleChart";
            chartObject.options = {
                    explorer: { actions: ['dragToZoom', 'rightClickToReset'] },
                    legend: {position: 'right'},
                    hAxis: {title: 'Series-Maturity', ticks: [],  slantedText:true },
                    vAxis: {title: 'Underlying', ticks: []},
                    chartArea: {height: "50%"},
                    backgroundColor: {
                        fill: 'transparent'
                    },
                    bubble: {textStyle: {color: 'none'}},
                    series: {Positive: {color: 'red'}, Negative: {color: 'green'}},
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
            return chartObject;
        }

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
            for (var index = 0; index < data.length; ++index) {
                addRecordToBubbles(data[index]);
                addAccountToSelection(data[index]);
            }
            selectionChanged(vm.accountSelection.selectedOption);

            function addAccountToSelection(record) {
                var accountKey = record.clearer + '-' + record.member + '-' + record.account;
                if (!(accountKey in vm.accountSelection.accountSet)) {
                    vm.accountSelection.accountSet[accountKey] = {
                      clearer: record.clearer,
                      member: record.member,
                      account: record.account
                    };
                    vm.accountSelection.availableOptions.push(vm.accountSelection.accountSet[accountKey]);
                    if (vm.accountSelection.selectedOption === null) {
                        vm.accountSelection.selectedOption = vm.accountSelection.accountSet[accountKey];
                    }
                }
            }

            function addRecordToBubbles(record) {
                var bubbleKey = record.clearer + '-' + record.member + '-' + record.account + '-' + record.symbol + '-' + record.maturityMonthYear;
                var radius = record.compVar;
                if (bubbleKey in bubblesMap.keys()) {
                    bubblesMap.get(bubbleKey).radius += radius;
                } else {
                    bubblesMap.set(bubbleKey, {
                        key: bubbleKey,
                        clearer: record.clearer,
                        member: record.member,
                        account: record.account,
                        symbol: record.symbol,
                        maturityMonthYear: record.maturityMonthYear,
                        underlying: record.underlying,
                        putCall: record.putCall,
                        radius: radius
                    });
                }
            }
        }

        function getLargestBubbles(selection) {
            var totalPositiveCompVar = 0;
            var topNPositiveCompVar = 0;
            var topNNegativeCompVar = 0;
            var totalNegativeCompVar = 0;
            var positiveBubbles = [];
            var negativeBubbles = [];
            bubblesMap.forEach(function(bubble, bubbleKey, mapObj) {
                if (bubble.clearer !== selection.clearer || bubble.member !== selection.member || bubble.account !== selection.account) {
                  return;
                }
                if (bubble.radius >= 0 ) {
                    positiveBubbles.push(bubble);
                    totalPositiveCompVar += bubble.radius;
                } else {
                    negativeBubbles.push(bubble);
                    totalNegativeCompVar += Math.abs(bubble.radius);
                }
            });
            positiveBubbles = positiveBubbles.sort(function(a, b) { return b.radius - a.radius; }).slice(1, parseInt(vm.topRecordsCount) + 1);
            negativeBubbles = negativeBubbles.sort(function(a, b) { return a.radius - b.radius; }).slice(1, parseInt(vm.topRecordsCount) + 1);
            positiveBubbles.forEach(function(bubble) {
                topNPositiveCompVar += bubble.radius;
            });
            negativeBubbles.forEach(function(bubble) {
                topNNegativeCompVar += Math.abs(bubble.radius);
            });
            totalCompVar = totalPositiveCompVar - totalNegativeCompVar;
            if (totalPositiveCompVar > 0) {
                positiveCoveragePerc = parseFloat((topNPositiveCompVar / totalCompVar) * 100).toFixed(2);
            }
            if (totalNegativeCompVar > 0) {
                negativeCoveragePerc = parseFloat((topNNegativeCompVar / totalNegativeCompVar) * 100).toFixed(2);
            }
            var bubbles = negativeBubbles.concat(positiveBubbles);
            bubbles.sort(function(a, b) {
                var first = a.symbol + '-' + a.maturityMonthYear;
                var second = b.symbol + '-' + b.maturityMonthYear;
                if (first < second)
                  return -1;
                if (first > second)
                  return 1;
                return 0;
            });
            return bubbles;
        }

        function selectionChanged(selection) {
            if (selection === null) return;
            var series = {};
            var underlyings = {};
            var hIndex = {optionsIndex: 0, futuresIndex: 0};
            var vIndex = 0;
            var rows = [];
            var hTicks = [];
            var vTicks = [];
            var bubbles = getLargestBubbles(selection);
            for (var i = 0; i < bubbles.length; i++) {
                var hAxisKey = bubbles[i].symbol + '-' + bubbles[i].maturityMonthYear;
                var vAxisKey = bubbles[i].underlying;
                if (!(hAxisKey in series)) {
                    if (bubbles[i].putCall && 0 !== bubbles[i].putCall.length) {
                        hIndex.optionsIndex++;
                        hTicks.push({v: hIndex.optionsIndex, f: hAxisKey});
                        series[hAxisKey] = hIndex.optionsIndex;
                    } else {
                        hIndex.futuresIndex--;
                        hTicks.push({v: hIndex.futuresIndex, f: hAxisKey});
                        series[hAxisKey] = hIndex.futuresIndex;
                    }
                }
                if (!(vAxisKey in underlyings)) {
                    vTicks.push({v: vIndex, f: vAxisKey});
                    underlyings[vAxisKey] = vIndex;
                    vIndex++;
                }

                rows.push({c: [{
                            v: bubbles[i].key
                        }, {
                            v: series[hAxisKey]
                        }, {
                            v: underlyings[vAxisKey]
                        }, {
                            v: bubbles[i].radius >= 0 ? compVarPositiveLegend : compVarNegativeLegend
                        }, {
                            v: Math.abs(bubbles[i].radius)
                        }
                    ]});
            }
            vm.chartObject.options.title = vm.topRecordsCount + " top risk positions represent " + $filter('number')(positiveCoveragePerc, 2) + "%  of total portfolio VaR. " + vm.topRecordsCount + " top offsetting positions represent " + $filter('number')(negativeCoveragePerc, 2) + "% of total offsetting positions. Total portfolio VaR is <nobr>" + $filter('number')(totalCompVar, 2) + "</nobr>.";
            vm.chartObject.options.hAxis.ticks = hTicks;
            vm.chartObject.options.vAxis.ticks = vTicks;
            vm.chartObject.data.rows = rows;
        }

        $scope.$on("$destroy", function() {
            if (refresh !== null) {
                $interval.cancel(refresh);
            }
        });
    };
})();
