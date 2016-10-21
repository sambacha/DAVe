/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('PositionReportLatestController', PositionReportLatestController);

    function PositionReportLatestController($scope, $routeParams, $http, $interval, $filter) {
        var currentPage = 1;
        $scope.refresh = null;
        $scope.initialLoad = true;
        $scope.pageSize = 20;
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
            $scope.updateViewport(currentPage);
        }

        $scope.sortRecords = function(column) {
            if ($scope.ordering[0] == column)
            {
                $scope.ordering = ["-" + column, "member", "account", "symbol", "putCall", "strikePrice", "optAttribute", "maturityMonthYear"];
            }
            else {
                $scope.ordering = [column, "member", "account", "symbol", "putCall", "strikePrice", "optAttribute", "maturityMonthYear"];
            }

            $scope.updateViewport(currentPage);
        };

        $scope.updateViewport = function(page) {
            currentPage = page;
            $scope.prLatest = $filter('orderBy')($filter('spacedFilter')($scope.prSource, $scope.recordQuery), $scope.ordering).slice(page*$scope.pageSize-$scope.pageSize, page*$scope.pageSize);
        }

        $scope.filter = function() {
            $scope.recordCount = $filter('spacedFilter')($scope.prSource, $scope.recordQuery).length;
            $scope.updateViewport(currentPage);
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