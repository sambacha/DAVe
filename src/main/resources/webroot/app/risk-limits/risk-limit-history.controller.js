/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('RiskLimitHistoryController', RiskLimitHistoryController);

    function RiskLimitHistoryController($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;
        $scope.initialLoad = true;
        var currentPage = 1;
        $scope.pageSize = 20;
        $scope.recordCount = 0;

        $scope.rlHistory = [];
        $scope.rlSource = [];
        $scope.existingRecords = [];
        $scope.error = "";
        $scope.rlChartData = [];
        $scope.ordering="-received";

        $scope.clearer = $routeParams.clearer;
        $scope.member = $routeParams.member;
        $scope.maintainer = $routeParams.maintainer;
        $scope.limitType = $routeParams.limitType;

        $scope.url = '/api/v1.0/rl/history/' + $scope.clearer + '/' + $scope.member + '/' + $scope.maintainer + '/' + $scope.limitType;

        $http.get($scope.url).success(function(data) {
            $scope.error = "";
            $scope.processRiskLimits(data);
            $scope.prepareGraphData(data);
            $scope.initialLoad = false;
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
            $scope.initialLoad = false;
        });

        $scope.processRiskLimits = function(data) {
            $scope.rlSource = data;
            $scope.recordCount = data.length;
            $scope.updateViewport(currentPage);
        }

        $scope.sortRecords = function(column) {
            if ($scope.ordering == column)
            {
                $scope.ordering = "-" + column;
            }
            else {
                $scope.ordering = column;
            }

            $scope.updateViewport(currentPage);
        };

        $scope.updateViewport = function(page) {
            currentPage = page;
            $scope.rlHistory = $filter('orderBy')($scope.rlSource, $scope.ordering).slice(currentPage*$scope.pageSize-$scope.pageSize, currentPage*$scope.pageSize);
        }

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.error = "";
                $scope.processRiskLimits(data);
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
            $scope.rlChartData = []

            var index;

            for (index = 0; index < data.length; ++index) {
                var tick = {
                    period: $filter('date')(data[index].received, "yyyy-MM-dd HH:mm:ss"),
                    utilization: data[index].utilization,
                    warningLevel: data[index].warningLevel,
                    throttleLevel: data[index].throttleLevel,
                    rejectLevel: data[index].rejectLevel
                };

                $scope.rlChartData.push(tick);
            }
        }
    };
})();