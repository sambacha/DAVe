/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('TotalMarginRequirementHistoryController', TotalMarginRequirementHistoryController);

    function TotalMarginRequirementHistoryController($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;
        $scope.initialLoad = true;
        var currentPage = 1;
        $scope.pageSize = 20;
        $scope.recordCount = 0;

        $scope.tmrHistory = [];
        $scope.tmrSource = [];
        $scope.existingRecords = [];
        $scope.error = "";
        $scope.tmrChartData = [];
        $scope.tmrChartOptions = { legendTemplate : "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<datasets.length; i++){%><li><span style=\"background-color:<%=datasets[i].lineColor%>\"></span><%if(datasets[i].label){%><%=datasets[i].label%><%}%></li><%}%></ul>" };
        $scope.ordering="-received";

        $scope.clearer = $routeParams.clearer;
        $scope.pool = $routeParams.pool;
        $scope.member = $routeParams.member;
        $scope.account = $routeParams.account;
        $scope.ccy = $routeParams.ccy;

        $scope.url = '/api/v1.0/tmr/history/' + $scope.clearer + '/' + $scope.pool + '/' + $scope.member + '/' + $scope.account + '/' + $scope.ccy;

        $http.get($scope.url).success(function(data) {
            $scope.error = "";
            $scope.processTotalMarginRequirements(data);
            $scope.prepareGraphData(data);
            $scope.initialLoad = false;
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
            $scope.initialLoad = false;
        });

        $scope.processTotalMarginRequirements = function(data) {
            $scope.tmrSource = data;
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
            $scope.tmrHistory = $filter('orderBy')($scope.tmrSource, $scope.ordering).slice(currentPage*$scope.pageSize-$scope.pageSize, currentPage*$scope.pageSize);
        }

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.error = "";
                $scope.processTotalMarginRequirements(data);
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
            $scope.tmrChartData = []

            var index;

            for (index = 0; index < data.length; ++index) {
                var tick = {
                    period: $filter('date')(data[index].received, "yyyy-MM-dd HH:mm:ss"),
                    adjustedMargin: data[index].adjustedMargin,
                    unadjustedMargin: data[index].unadjustedMargin
                };

                $scope.tmrChartData.push(tick);
            }
        }
    };
})();