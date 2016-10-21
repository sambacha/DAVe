/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('RiskLimitLatestController', RiskLimitLatestController);

    function RiskLimitLatestController($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;
        $scope.initialLoad = true;
        var currentPage = 1;
        $scope.pageSize = 20;
        $scope.recordCount = 0;

        $scope.rlLatest = [];
        $scope.rlSource = [];
        $scope.existingRecords = [];
        $scope.error = "";
        $scope.ordering= ["clearer", "member", "maintainer", "limitType"];

        if ($routeParams.clearer) { $scope.clearer = $routeParams.clearer } else { $scope.clearer = "*" }
        if ($routeParams.member) { $scope.member = $routeParams.member } else { $scope.member = "*" }
        if ($routeParams.maintainer) { $scope.maintainer = $routeParams.maintainer } else { $scope.maintainer = "*" }
        if ($routeParams.limitType) { $scope.limitType = $routeParams.limitType } else { $scope.limitType = "*" }

        $scope.url = '/api/v1.0/rl/latest/' + $scope.clearer + '/' + $scope.member + '/' + $scope.maintainer + '/' + $scope.limitType;

        $http.get($scope.url).success(function(data) {
            $scope.processRiskLimits(data);
            $scope.error = "";
            $scope.initialLoad = false;
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
            $scope.initialLoad = false;
        });

        $scope.processRiskLimits = function(data) {
            var index;

            for (index = 0; index < data.length; ++index) {
                data[index].functionalKey = data[index].clearer + '-' + data[index].member + '-' + data[index].maintainer + '-' + data[index].limitType;
            }

            $scope.rlSource = data;
            $scope.filter();
            $scope.updateViewport(currentPage);
        }

        $scope.sortRecords = function(column) {
            if ($scope.ordering[0] == column)
            {
                $scope.ordering = ["-" + column, "clearer", "member", "maintainer", "limitType"];
            }
            else {
                $scope.ordering = [column, "clearer", "member", "maintainer", "limitType"];
            }

            $scope.updateViewport(currentPage);
        };

        $scope.updateViewport = function(page) {
            currentPage = page;
            $scope.rlLatest = $filter('orderBy')($filter('spacedFilter')($scope.rlSource, $scope.recordQuery), $scope.ordering).slice(currentPage*$scope.pageSize-$scope.pageSize, currentPage*$scope.pageSize);
        }

        $scope.filter = function() {
            $scope.recordCount = $filter('spacedFilter')($scope.rlSource, $scope.recordQuery).length;
            $scope.updateViewport(currentPage);
        };

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.processRiskLimits(data);
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