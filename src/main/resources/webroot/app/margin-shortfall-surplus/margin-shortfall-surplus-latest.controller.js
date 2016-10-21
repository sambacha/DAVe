/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('MarginShortfallSurplusLatestController', MarginShortfallSurplusLatestController);

    function MarginShortfallSurplusLatestController($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;
        $scope.initialLoad = true;
        var currentPage = 1;
        $scope.pageSize = 20;
        $scope.recordCount = 0;

        $scope.mssLatest = [];
        $scope.mssSource = [];
        $scope.existingRecords = [];
        $scope.error = "";
        $scope.ordering= ["pool", "member", "clearingCcy", "ccy"];

        if ($routeParams.clearer) { $scope.clearer = $routeParams.clearer } else { $scope.clearer = "*" }
        if ($routeParams.pool) { $scope.pool = $routeParams.pool } else { $scope.pool = "*" }
        if ($routeParams.member) { $scope.member = $routeParams.member } else { $scope.member = "*" }
        if ($routeParams.clearingCcy) { $scope.clearingCcy = $routeParams.clearingCcy } else { $scope.clearingCcy = "*" }

        $scope.url = '/api/v1.0/mss/latest/' + $scope.clearer + '/' + $scope.pool + '/' + $scope.member + '/' + $scope.clearingCcy;

        $http.get($scope.url).success(function(data) {
            $scope.processMarginShortfallSurplus(data);
            $scope.error = "";
            $scope.initialLoad = false;
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
            $scope.initialLoad = false;
        });

        $scope.processMarginShortfallSurplus = function(marginShortfallSurplus) {
            var index;

            for (index = 0; index < marginShortfallSurplus.length; ++index) {
                marginShortfallSurplus[index].functionalKey = marginShortfallSurplus[index].clearer + '-' + marginShortfallSurplus[index].pool + '-' + marginShortfallSurplus[index].member + '-' + marginShortfallSurplus[index].clearingCcy + '-' + marginShortfallSurplus[index].ccy;
            }

            $scope.mssSource = marginShortfallSurplus;
            $scope.filter();
            $scope.updateViewport(currentPage);
        }

        $scope.sortRecords = function(column) {
            if ($scope.ordering[0] == column)
            {
                $scope.ordering = ["-" + column, "pool", "member", "clearingCcy", "ccy"];
            }
            else {
                $scope.ordering = [column, "pool", "member", "clearingCcy", "ccy"];
            }

            $scope.updateViewport(currentPage);
        };

        $scope.updateViewport = function(page) {
            currentPage = page;
            $scope.mssLatest = $filter('orderBy')($filter('spacedFilter')($scope.mssSource, $scope.recordQuery), $scope.ordering).slice(currentPage*$scope.pageSize-$scope.pageSize, currentPage*$scope.pageSize);
        }

        $scope.filter = function() {
            $scope.recordCount = $filter('spacedFilter')($scope.mssSource, $scope.recordQuery).length;
            $scope.updateViewport(currentPage);
        };

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.processMarginShortfallSurplus(data);
                $scope.error = "";
            }).error(function(data, status, headers, config) {
                $scope.error = "Server returned status " + status;
            });
        },60000);

        $scope.$on("$destroy", function() {
            if ($scope.refresh != null) {
                $interval.cancel($scope.refresh);
            }
        });
    };
})();