/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('MarginComponentHistoryController', MarginComponentHistoryController);

    function MarginComponentHistoryController($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;
        $scope.initialLoad = true;
        $scope.page = 1;
        $scope.pageSize = 20;
        $scope.mcPaging = {"first": {"class": "disabled"}, "previous": {"class": "disabled"}, "pages": [], "next": {"class": "disabled"}, "last": {"class": "disabled"}};
        $scope.recordCount = 0;

        $scope.mcHistory = [];
        $scope.mcSource = [];
        $scope.existingRecords = [];
        $scope.error = "";
        $scope.mcChartData = [];
        $scope.ordering="-received";

        $scope.clearer = $routeParams.clearer;
        $scope.member = $routeParams.member;
        $scope.account = $routeParams.account;
        $scope.class = $routeParams.class;
        $scope.ccy = $routeParams.ccy;

        $scope.url = '/api/v1.0/mc/history/' + $scope.clearer + '/' + $scope.member + '/' + $scope.account + '/' + $scope.class + '/' + $scope.ccy;

        $http.get($scope.url).success(function(data) {
            $scope.error = "";
            $scope.processMarginComponents(data);
            $scope.prepareGraphData(data);
            $scope.initialLoad = false;
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
            $scope.initialLoad = false;
        });

        $scope.processMarginComponents = function(data) {
            $scope.mcSource = data;
            $scope.recordCount = data.length;
            $scope.updateViewport();
            $scope.updatePaging();
        }

        $scope.sortRecords = function(column) {
            if ($scope.ordering == column)
            {
                $scope.ordering = "-" + column;
            }
            else {
                $scope.ordering = column;
            }

            $scope.updateViewport();
        };

        $scope.updateViewport = function() {
            $scope.mcHistory = $filter('orderBy')($scope.mcSource, $scope.ordering).slice($scope.page*$scope.pageSize-$scope.pageSize, $scope.page*$scope.pageSize);
        }

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
                $scope.error = "";
                $scope.processMarginComponents(data);
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
            $scope.mcChartData = []

            var index;

            for (index = 0; index < data.length; ++index) {
                var tick = {
                    period: $filter('date')(data[index].received, "yyyy-MM-dd HH:mm:ss"),
                    variationMargin: data[index].variationMargin,
                    premiumMargin: data[index].premiumMargin,
                    liquiMargin: data[index].liquiMargin,
                    spreadMargin: data[index].spreadMargin,
                    additionalMargin: data[index].additionalMargin
                };

                $scope.mcChartData.push(tick);
            }
        }
    };
})();