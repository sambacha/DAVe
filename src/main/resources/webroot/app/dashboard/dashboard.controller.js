/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('DashboardController', DashboardController);

    function DashboardController($scope, $routeParams, $http, $interval, $filter) {
        var vm = this;
        vm.errorMessage = "";
        vm.chartData = {
            "MarginRequirement": [],
            "MarginShortfallSurplus": [],
            "MarginCall": []
        };

        var refresh = $interval(loadData, 60000);
        var restQueryUrl = '/api/v1.0/mss/latest/';

        loadData();

        ////////////////////

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
            var chartData = {
                "MarginRequirement": [],
                "MarginShortfallSurplus": [],
                "MarginCall": []
            };
            var index;

            for (index = 0; index < data.length; ++index) {
                var marginRequirementTick = {
                    label: data[index].pool + " / " + data[index].member + " / " + data[index].clearingCcy,
                    value: data[index].marginRequirement
                };

                var marginShortfallSurplusTick = {
                    label: data[index].pool + " / " + data[index].member + " / " + data[index].clearingCcy,
                    value: data[index].shortfallSurplus
                };

                var marginCallTick = {
                    label: data[index].pool + " / " + data[index].member + " / " + data[index].clearingCcy,
                    value: data[index].marginCall
                };

                chartData.MarginRequirement.push(marginRequirementTick);
                chartData.MarginShortfallSurplus.push(marginShortfallSurplusTick);
                chartData.MarginCall.push(marginCallTick);
            }

            vm.chartData = chartData;
        }

        $scope.$on("$destroy", function() {
            if (refresh != null) {
                $interval.cancel(refresh);
            }
        });
    };
})();