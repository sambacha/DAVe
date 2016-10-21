/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('TotalMarginRequirementLatestController', TotalMarginRequirementLatestController);

    function TotalMarginRequirementLatestController($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;
        $scope.initialLoad = true;
        var currentPage = 1;
        $scope.pageSize = 20;
        $scope.recordCount = 0;

        $scope.tmrLatest = [];
        $scope.tmrSource = [];
        $scope.existingRecords = [];
        $scope.error = "";
        $scope.ordering= ["pool", "member", "account", "ccy"];

        if ($routeParams.clearer) { $scope.clearer = $routeParams.clearer } else { $scope.clearer = "*" }
        if ($routeParams.pool) { $scope.pool = $routeParams.pool } else { $scope.pool = "*" }
        if ($routeParams.member) { $scope.member = $routeParams.member } else { $scope.member = "*" }
        if ($routeParams.account) { $scope.account = $routeParams.account } else { $scope.account = "*" }
        if ($routeParams.ccy) { $scope.ccy = $routeParams.ccy } else { $scope.ccy = "*" }

        $scope.url = '/api/v1.0/tmr/latest/' + $scope.clearer + '/' + $scope.pool + '/' + $scope.member + '/' + $scope.account + '/' + $scope.ccy;

        $http.get($scope.url).success(function(data) {
            $scope.processTotalMarginRequirements(data);
            $scope.error = "";
            $scope.initialLoad = false;
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
            $scope.initialLoad = false;
        });

        $scope.processTotalMarginRequirements = function(totalMarginRequirements) {
            var index;

            for (index = 0; index < totalMarginRequirements.length; ++index) {
                totalMarginRequirements[index].functionalKey = totalMarginRequirements[index].clearer + '-' + totalMarginRequirements[index].pool + '-' + totalMarginRequirements[index].member + '-' + totalMarginRequirements[index].account + '-' + totalMarginRequirements[index].ccy;
            }

            $scope.tmrSource = totalMarginRequirements;
            $scope.filter();
            $scope.updateViewport(currentPage);
        }

        $scope.sortRecords = function(column) {
            if ($scope.ordering[0] == column)
            {
                $scope.ordering = ["-" + column, "pool", "member", "account", "ccy"];
            }
            else {
                $scope.ordering = [column, "pool", "member", "account", "ccy"];
            }

            $scope.updateViewport(currentPage);
        };

        $scope.updateViewport = function(page) {
            currentPage = page;
            $scope.tmrLatest = $filter('orderBy')($filter('spacedFilter')($scope.tmrSource, $scope.recordQuery), $scope.ordering).slice(currentPage*$scope.pageSize-$scope.pageSize, currentPage*$scope.pageSize);
        }

        $scope.filter = function() {
            $scope.recordCount = $filter('spacedFilter')($scope.tmrSource, $scope.recordQuery).length;
            $scope.updateViewport(currentPage);
        };

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.processTotalMarginRequirements(data);
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