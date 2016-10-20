/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('TotalMarginRequirementHistoryController', TotalMarginRequirementHistoryController);

    function TotalMarginRequirementHistoryController($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;
        $scope.initialLoad = true;
        $scope.page = 1;
        $scope.pageSize = 20;
        $scope.paging = {"first": {"class": "disabled"}, "previous": {"class": "disabled"}, "pages": [], "next": {"class": "disabled"}, "last": {"class": "disabled"}};
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
            $scope.tmrHistory = $filter('orderBy')($scope.tmrSource, $scope.ordering).slice($scope.page*$scope.pageSize-$scope.pageSize, $scope.page*$scope.pageSize);
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
            var tempPaging = $scope.paging;
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
                tempPaging.first.class = "disabled";
                tempPaging.previous.class = "disabled";
            }
            else {
                tempPaging.first.class = "";
                tempPaging.previous.class = "";
            }

            tempPaging.pages = [];

            if ($scope.page > 3)
            {
                tempPaging.pages.push({"page": $scope.page-3, "class": ""});
            }

            if ($scope.page > 2)
            {
                tempPaging.pages.push({"page": $scope.page-2, "class": ""});
            }

            if ($scope.page > 1)
            {
                tempPaging.pages.push({"page": $scope.page-1, "class": ""});
            }

            tempPaging.pages.push({"page": $scope.page, "class": "active"});

            if ($scope.page < pageCount)
            {
                tempPaging.pages.push({"page": $scope.page+1, "class": ""});
            }

            if ($scope.page < pageCount-1)
            {
                tempPaging.pages.push({"page": $scope.page+2, "class": ""});
            }

            if ($scope.page < pageCount-2)
            {
                tempPaging.pages.push({"page": $scope.page+3, "class": ""});
            }

            if ($scope.page == pageCount) {
                tempPaging.next.class = "disabled";
                tempPaging.last.class = "disabled";
            }
            else {
                tempPaging.next.class = "";
                tempPaging.last.class = "";
            }

            $scope.paging = tempPaging;
        };

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