/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('PositionReportHistoryController', PositionReportHistoryController);

    function PositionReportHistoryController($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;
        $scope.initialLoad = true;
        var currentPage = 1;
        $scope.pageSize = 20;
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
            $scope.updateViewport(currentPage);
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

            $scope.updateViewport(currentPage);
        };

        $scope.updateViewport = function(page) {
            currentPage = page;
            $scope.prHistory = $filter('orderBy')($scope.prSource, $scope.ordering).slice(currentPage*$scope.pageSize-$scope.pageSize, currentPage*$scope.pageSize);
        }

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
            $scope.prChartData = [];

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