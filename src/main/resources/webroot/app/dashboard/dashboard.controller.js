/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('DashboardController', DashboardController);

    function DashboardController($scope, $routeParams, $http, $interval, $filter) {
        $scope.chartMarginRequirementData = [];
        $scope.chartMarginShortfallSurplusData = [];
        $scope.chartMarginCallData = [];

        $scope.url = '/api/v1.0/mss/latest';

        $http.get($scope.url).success(function(data) {
            $scope.error = "";
            $scope.prepareGraphData(data);
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
        });

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.error = "";
                $scope.prepareGraphData(data);
            }).error(function(data, status, headers, config) {
                $scope.error = "Server returned status " + status;
            });
        },60000);

        $scope.$on("$destroy", function() {
            if ($scope.refresh != null) {
                $interval.cancel($scope.refresh);
            }
        });

        $scope.prepareGraphData = function(data) {
            $scope.chartMarginRequirementData = [];
            $scope.chartMarginShortfallSurplusData = [];
            $scope.chartMarginCallData = [];

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

                $scope.chartMarginRequirementData.push(marginRequirementTick);
                $scope.chartMarginShortfallSurplusData.push(marginShortfallSurplusTick);
                $scope.chartMarginCallData.push(marginCallTick);
            }
        }
    };
})();