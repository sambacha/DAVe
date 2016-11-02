
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
        vm.chartObject = createEmptyChartObject();
        vm.accountSelection = { accountSet: {}, availableOptions: [], selectedOption: null };
        vm.selectionChanged = selectionChanged;

        var refresh = $interval(loadData, 60000);
        var restQueryUrl = '/api/v1.0/pr/latest/';
        var bubbles = new Map();

        function createEmptyChartObject() {
            var chartObject = {};
            chartObject.type = "BubbleChart";
            chartObject.options = {
                    explorer: { actions: ['dragToZoom', 'rightClickToReset'] },
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
            return chartObject;
        }

        loadData();

        function loadData(){
            $http.get(restQueryUrl).success(function(data) {
                function compare(a, b) {
                    var first = a.symbol + '-' + a.maturityMonthYear;
                    var second = b.symbol + '-' + b.maturityMonthYear;
                    if (first < second)
                      return -1;
                    if (first > second)
                      return 1;
                    return 0;
                }
                data.sort(compare);
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
                if (bubbleKey in bubbles.keys()) {
                    bubbles.get(bubbleKey).radius += radius;
                } else {
                    bubbles.set(bubbleKey, {
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

        function selectionChanged(selection) {
            if (selection === null) return;
            var series = {};
            var underlyings = {};
            var hIndex = {optionsIndex: 0, futuresIndex: 0};
            var vIndex = 0;
            var rows = [];
            var hTicks = [];
            var vTicks = [];
            bubbles.forEach(function(bubble, bubbleKey, mapObj) {
                if (bubble.clearer !== selection.clearer || bubble.member !== selection.member || bubble.account !== selection.account) {
                  return;
                }
                var hAxisKey = bubble.symbol + '-' + bubble.maturityMonthYear;
                var vAxisKey = bubble.underlying;
                if (!(hAxisKey in series)) {
                    if (bubble.putCall && 0 !== bubble.putCall.length) {
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
                            v: bubbleKey
                        }, {
                            v: series[hAxisKey]
                        }, {
                            v: underlyings[vAxisKey]
                        }, {
                            v: bubbles.get(bubbleKey).radius >= 0 ? "Compvar positive" : "Compvar negative"
                        }, {
                            v: Math.abs(bubbles.get(bubbleKey).radius)
                        }
                    ]});
            });
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