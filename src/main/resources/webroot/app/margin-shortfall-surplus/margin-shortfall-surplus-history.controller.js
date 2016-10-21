/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('MarginShortfallSurplusHistoryController', MarginShortfallSurplusHistoryController);

    function MarginShortfallSurplusHistoryController($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;
        $scope.initialLoad = true;
        var currentPage = 1;
        $scope.pageSize = 20;
        $scope.recordCount = 0;

        $scope.mssHistory = [];
        $scope.mssSource = [];
        $scope.existingRecords = [];
        $scope.error = "";
        $scope.mssChartData = [];
        $scope.ordering="-received";

        $scope.clearer = $routeParams.clearer;
        $scope.pool = $routeParams.pool;
        $scope.member = $routeParams.member;
        $scope.clearingCcy = $routeParams.clearingCcy;
        $scope.ccy = $routeParams.ccy;

        $scope.url = '/api/v1.0/mss/history/' + $scope.clearer + '/' + $scope.pool + '/' + $scope.member + '/' + $scope.clearingCcy + '/' + $scope.ccy;

        $http.get($scope.url).success(function(data) {
            $scope.error = "";
            $scope.processMarginShortfallSurpluss(data);
            $scope.prepareGraphData(data);
            $scope.initialLoad = false;
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
            $scope.initialLoad = false;
        });

        $scope.processMarginShortfallSurpluss = function(data) {
            $scope.mssSource = data;
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
            $scope.mssHistory = $filter('orderBy')($scope.mssSource, $scope.ordering).slice(currentPage*$scope.pageSize-$scope.pageSize, currentPage*$scope.pageSize);
        }

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.error = "";
                $scope.processMarginShortfallSurpluss(data);
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
            $scope.mssChartData = []

            var index;

            for (index = 0; index < data.length; ++index) {
                var tick = {
                    period: $filter('date')(data[index].received, "yyyy-MM-dd HH:mm:ss"),
                    marginRequirement: data[index].marginRequirement,
                    securityCollateral: data[index].securityCollateral,
                    cashBalance: data[index].cashBalance,
                    shortfallSurplus: data[index].shortfallSurplus,
                    marginCall: data[index].marginCall
                };

                $scope.mssChartData.push(tick);
            }
        }
    };
})();