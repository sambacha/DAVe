/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('PositionReportHistoryController', PositionReportHistoryController);

    function PositionReportHistoryController($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;
        $scope.initialLoad = true;
        $scope.page = 1;
        $scope.pageSize = 20;
        $scope.prPaging = {"first": {"class": "disabled"}, "previous": {"class": "disabled"}, "pages": [], "next": {"class": "disabled"}, "last": {"class": "disabled"}};
        $scope.recordCount = 0;

        $scope.prHistory = [];
        $scope.prSource = [];
        $scope.existingRecords = [];
        $scope.error = "";
        $scope.prChartData = [];
        $scope.ordering="-received";

        $scope.clearer = $routeParams.clearer;
        $scope.member = $routeParams.member;
        $scope.account = $routeParams.account;
        $scope.class = $routeParams.class;
        $scope.symbol = $routeParams.symbol;
        $scope.putCall = $routeParams.putCall;
        $scope.strikePrice = $routeParams.strikePrice;
        $scope.optAttribute = $routeParams.optAttribute;
        $scope.maturityMonthYear = $routeParams.maturityMonthYear;

        $scope.url = '/api/v1.0/pr/history/' + $scope.clearer + '/' + $scope.member + '/' + $scope.account + '/' + $scope.class + '/' + $scope.symbol + '/' + $scope.putCall + '/' + $scope.strikePrice + '/' + $scope.optAttribute + '/' + $scope.maturityMonthYear;

        $scope.processPositionReports = function(positionReports) {
            var index;

            for (index = 0; index < positionReports.length; ++index) {
                positionReports[index].netLS = positionReports[index].crossMarginLongQty - positionReports[index].crossMarginShortQty;
                positionReports[index].netEA = (positionReports[index].optionExcerciseQty - positionReports[index].optionAssignmentQty) + (positionReports[index].allocationTradeQty - positionReports[index].deliveryNoticeQty);
            }

            $scope.prSource = positionReports;
            $scope.recordCount = positionReports.length;
            $scope.updateViewport();
            $scope.updatePaging();
        }

        $http.get($scope.url).success(function(data) {
            $scope.error = "";
            $scope.processPositionReports(data);
            $scope.prepareGraphData($scope.prHistory);
            $scope.initialLoad = false;
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
            $scope.initialLoad = false;
        });

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
            $scope.prHistory = $filter('orderBy')($scope.prSource, $scope.ordering).slice($scope.page*$scope.pageSize-$scope.pageSize, $scope.page*$scope.pageSize);
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
            var tempPrPaging = $scope.prPaging;
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
                tempPrPaging.first.class = "disabled";
                tempPrPaging.previous.class = "disabled";
            }
            else {
                tempPrPaging.first.class = "";
                tempPrPaging.previous.class = "";
            }

            tempPrPaging.pages = [];

            if ($scope.page > 3)
            {
                tempPrPaging.pages.push({"page": $scope.page-3, "class": ""});
            }

            if ($scope.page > 2)
            {
                tempPrPaging.pages.push({"page": $scope.page-2, "class": ""});
            }

            if ($scope.page > 1)
            {
                tempPrPaging.pages.push({"page": $scope.page-1, "class": ""});
            }

            tempPrPaging.pages.push({"page": $scope.page, "class": "active"});

            if ($scope.page < pageCount)
            {
                tempPrPaging.pages.push({"page": $scope.page+1, "class": ""});
            }

            if ($scope.page < pageCount-1)
            {
                tempPrPaging.pages.push({"page": $scope.page+2, "class": ""});
            }

            if ($scope.page < pageCount-2)
            {
                tempPrPaging.pages.push({"page": $scope.page+3, "class": ""});
            }

            if ($scope.page == pageCount) {
                tempPrPaging.next.class = "disabled";
                tempPrPaging.last.class = "disabled";
            }
            else {
                tempPrPaging.next.class = "";
                tempPrPaging.last.class = "";
            }

            $scope.prPaging = tempPrPaging;
        };

        $scope.showExtra = function(funcKey) {
            var extra = $("#extra-" + funcKey);
            var extraIcon = $("#extra-icon-" + funcKey);

            if (extra.hasClass("hidden"))
            {
                extra.removeClass("hidden");
                extraIcon.removeClass("fa-chevron-circle-down");
                extraIcon.addClass("fa-chevron-circle-up");
            }
            else {
                extra.addClass("hidden");
                extraIcon.removeClass("fa-chevron-circle-up");
                extraIcon.addClass("fa-chevron-circle-down");
            }
        };

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.error = "";
                $scope.processPositionReports(data);
                $scope.prepareGraphData($scope.prHistory);
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
            $scope.prChartData = []

            var index;

            for (index = 0; index < data.length; ++index) {
                var tick = {
                    period: $filter('date')(data[index].received, "yyyy-MM-dd HH:mm:ss"),
                    netLS: data[index].netLS,
                    netEA: data[index].netEA,
                    mVar: data[index].mVar,
                    compVar: data[index].compVar,
                    delta: data[index].delta,
                    compLiquidityAddOn: data[index].compLiquidityAddOn
                };

                $scope.prChartData.push(tick);
            }
        }
    };
})();