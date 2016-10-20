/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('MarginComponentLatestController', MarginComponentLatestController);

    function MarginComponentLatestController($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;
        $scope.initialLoad = true;
        $scope.page = 1;
        $scope.pageSize = 20;
        $scope.mcPaging = {"first": {"class": "disabled"}, "previous": {"class": "disabled"}, "pages": [], "next": {"class": "disabled"}, "last": {"class": "disabled"}};
        $scope.recordCount = 0;

        $scope.mcLatest = [];
        $scope.mcSource = [];
        $scope.existingRecords = [];
        $scope.error = "";
        $scope.ordering= ["member", "account", "clss", "ccy"];

        if ($routeParams.clearer) { $scope.clearer = $routeParams.clearer } else { $scope.clearer = "*" }
        if ($routeParams.member) { $scope.member = $routeParams.member } else { $scope.member = "*" }
        if ($routeParams.account) { $scope.account = $routeParams.account } else { $scope.account = "*" }
        if ($routeParams.class) { $scope.class = $routeParams.class } else { $scope.class = "*" }
        if ($routeParams.ccy) { $scope.ccy = $routeParams.ccy } else { $scope.ccy = "*" }

        $scope.url = '/api/v1.0/mc/latest/' + $scope.clearer + '/' + $scope.member + '/' + $scope.account + '/' + $scope.class + '/' + $scope.ccy;

        $http.get($scope.url).success(function(data) {
            $scope.processMarginComponents(data);
            $scope.error = "";
            $scope.initialLoad = false;
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
            $scope.initialLoad = false;
        });

        $scope.processMarginComponents = function(data) {
            var index;

            for (index = 0; index < data.length; ++index) {
                data[index].functionalKey = data[index].clearer + '-' + data[index].member + '-' + data[index].account + '-' + data[index].clss + '-' + data[index].ccy;
            }

            $scope.mcSource = data;
            $scope.filter();
            $scope.updateViewport();
            $scope.updatePaging();
        }

        $scope.sortRecords = function(column) {
            if ($scope.ordering[0] == column)
            {
                $scope.ordering = ["-" + column, "member", "account", "clss", "ccy"];
            }
            else {
                $scope.ordering = [column, "member", "account", "clss", "ccy"];
            }

            $scope.updateViewport();
        };

        $scope.updateViewport = function() {
            $scope.mcLatest = $filter('orderBy')($filter('spacedFilter')($scope.mcSource, $scope.recordQuery), $scope.ordering).slice($scope.page*$scope.pageSize-$scope.pageSize, $scope.page*$scope.pageSize);
        }

        $scope.filter = function() {
            $scope.recordCount = $filter('spacedFilter')($scope.mcSource, $scope.recordQuery).length;

            $scope.updatePaging();
            $scope.updateViewport();
        };

        $scope.pagingNext = function() {
            if ($scope.page < Math.ceil($scope.recordCount/$scope.pageSize))
            {
                $scope.page++;
            }

            $scope.updateViewport();
            $scope.updatePaging();
        };

        $scope.pagingPrevious = function() {
            if ($scope.page > 1)
            {
                $scope.page--;
            }

            $scope.updateViewport();
            $scope.updatePaging();
        };

        $scope.pagingFirst = function() {
            $scope.page = 1;
            $scope.updateViewport();
            $scope.updatePaging();
        };

        $scope.pagingLast = function() {
            $scope.page = Math.ceil($scope.recordCount/$scope.pageSize);
            $scope.updateViewport();
            $scope.updatePaging();
        };

        $scope.pagingGoTo = function(pageNo) {
            $scope.page = pageNo;
            $scope.updateViewport();
            $scope.updatePaging();
        };

        $scope.updatePaging = function() {
            var tempMcPaging = $scope.mcPaging;
            var pageCount = Math.ceil($scope.recordCount/$scope.pageSize);

            if ($scope.page > pageCount)
            {
                $scope.page = pageCount;
                $scope.updateViewport();
            }

            if ($scope.page < 1)
            {
                $scope.page = 1;
                $scope.updateViewport();
            }

            if ($scope.page == 1) {
                tempMcPaging.first.class = "disabled";
                tempMcPaging.previous.class = "disabled";
            }
            else {
                tempMcPaging.first.class = "";
                tempMcPaging.previous.class = "";
            }

            tempMcPaging.pages = [];

            if ($scope.page > 3)
            {
                tempMcPaging.pages.push({"page": $scope.page-3, "class": ""});
            }

            if ($scope.page > 2)
            {
                tempMcPaging.pages.push({"page": $scope.page-2, "class": ""});
            }

            if ($scope.page > 1)
            {
                tempMcPaging.pages.push({"page": $scope.page-1, "class": ""});
            }

            tempMcPaging.pages.push({"page": $scope.page, "class": "active"});

            if ($scope.page < pageCount)
            {
                tempMcPaging.pages.push({"page": $scope.page+1, "class": ""});
            }

            if ($scope.page < pageCount-1)
            {
                tempMcPaging.pages.push({"page": $scope.page+2, "class": ""});
            }

            if ($scope.page < pageCount-2)
            {
                tempMcPaging.pages.push({"page": $scope.page+3, "class": ""});
            }

            if ($scope.page == pageCount) {
                tempMcPaging.next.class = "disabled";
                tempMcPaging.last.class = "disabled";
            }
            else {
                tempMcPaging.next.class = "";
                tempMcPaging.last.class = "";
            }

            $scope.mcPaging = tempMcPaging;
        };

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.processMarginComponents(data);
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