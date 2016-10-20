/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('PositionReportLatestController', PositionReportLatestController);

    function PositionReportLatestController($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;
        $scope.initialLoad = true;
        $scope.page = 1;
        $scope.pageSize = 20;
        $scope.prPaging = {"first": {"class": "disabled"}, "previous": {"class": "disabled"}, "pages": [], "next": {"class": "disabled"}, "last": {"class": "disabled"}};
        $scope.recordCount = 0;

        $scope.prLatest = [];
        $scope.prSource = [];
        $scope.existingRecords = [];
        $scope.error = "";
        $scope.recordQuery = "";
        $scope.ordering= ["member", "account", "symbol", "putCall", "strikePrice", "optAttribute", "maturityMonthYear"];

        if ($routeParams.clearer) { $scope.clearer = $routeParams.clearer; } else { $scope.clearer = "*" }
        if ($routeParams.member) { $scope.member = $routeParams.member; } else { $scope.member = "*" }
        if ($routeParams.account) { $scope.account = $routeParams.account; } else { $scope.account = "*" }
        if ($routeParams.class) { $scope.class = $routeParams.class; } else { $scope.class = "*" }
        if ($routeParams.symbol) { $scope.symbol = $routeParams.symbol; } else { $scope.symbol = "*" }
        if ($routeParams.putCall) { $scope.putCall = $routeParams.putCall; } else { $scope.putCall = "*" }
        if ($routeParams.strikePrice) { $scope.strikePrice = $routeParams.strikePrice; } else { $scope.strikePrice = "*" }
        if ($routeParams.optAttribute) { $scope.optAttribute = $routeParams.optAttribute; } else { $scope.optAttribute = "*" }
        if ($routeParams.maturityMonthYear) { $scope.maturityMonthYear = $routeParams.maturityMonthYear; } else { $scope.maturityMonthYear = "*" }

        $scope.url = '/api/v1.0/pr/latest/' + $scope.clearer + '/' + $scope.member + '/' + $scope.account + '/' + $scope.class + '/' + $scope.symbol + '/' + $scope.putCall + '/' + $scope.strikePrice + '/' + $scope.optAttribute + "/" + $scope.maturityMonthYear;

        $http.get($scope.url).success(function(data) {
            $scope.processPositionReports(data);
            $scope.initialLoad = false;
            $scope.error = "";
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
            $scope.initialLoad = false;
        });

        $scope.processPositionReports = function(positionReports) {
            var index;

            for (index = 0; index < positionReports.length; ++index) {
                positionReports[index].functionalKey = positionReports[index].clearer + '-' + positionReports[index].member + '-' + positionReports[index].account + '-' + positionReports[index].clss + '-' + positionReports[index].symbol + '-' + positionReports[index].putCall + '-' + positionReports[index].maturityMonthYear + '-' + positionReports[index].strikePrice.replace("\.", "") + '-' + positionReports[index].optAttribute + '-' + positionReports[index].maturityMonthYear;
                positionReports[index].netLS = positionReports[index].crossMarginLongQty - positionReports[index].crossMarginShortQty;
                positionReports[index].netEA = (positionReports[index].optionExcerciseQty - positionReports[index].optionAssignmentQty) + (positionReports[index].allocationTradeQty - positionReports[index].deliveryNoticeQty);
            }

            $scope.prSource = positionReports;
            $scope.filter();
            $scope.updateViewport();
            $scope.updatePaging();
        }

        $scope.sortRecords = function(column) {
            if ($scope.ordering[0] == column)
            {
                $scope.ordering = ["-" + column, "member", "account", "symbol", "putCall", "strikePrice", "optAttribute", "maturityMonthYear"];
            }
            else {
                $scope.ordering = [column, "member", "account", "symbol", "putCall", "strikePrice", "optAttribute", "maturityMonthYear"];
            }

            $scope.updateViewport();
        };

        $scope.updateViewport = function() {
            $scope.prLatest = $filter('orderBy')($filter('spacedFilter')($scope.prSource, $scope.recordQuery), $scope.ordering).slice($scope.page*$scope.pageSize-$scope.pageSize, $scope.page*$scope.pageSize);
        }

        $scope.filter = function() {
            $scope.recordCount = $filter('spacedFilter')($scope.prSource, $scope.recordQuery).length;

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
                $scope.processPositionReports(data);
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