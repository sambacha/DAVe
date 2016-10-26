/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('PositionReportHistoryController', PositionReportHistoryController);

    function PositionReportHistoryController($scope, $routeParams, $http, $interval, $filter, sortRecordsService, updateViewWindowService, showExtraInfoService) {
        var vm = this;
        vm.initialLoad= true;
        vm.pageSize = 20;
        vm.recordCount = 0;
        vm.viewWindow = [];
        vm.updateViewWindow = updateViewWindow;
        vm.chartData = [];
        vm.errorMessage = "";
        vm.sortRecords = sortRecords;
        vm.showExtraInfo = showExtraInfo;
        vm.route = {
            "clearer": $routeParams.clearer,
            "member": $routeParams.member,
            "account": $routeParams.account,
            "class": $routeParams.class,
            "symbol": $routeParams.symbol,
            "putCall": $routeParams.putCall,
            "strikePrice": $routeParams.strikePrice,
            "optAttribute": $routeParams.optAttribute,
            "maturityMonthYear": $routeParams.maturityMonthYear
        };

        var currentPage = 1;
        var refresh = $interval(loadData, 60000);
        var restQueryUrl = '/api/v1.0/pr/history/' + vm.route.clearer + '/' + vm.route.member + '/' + vm.route.account + '/' + vm.route.class + '/' + vm.route.symbol + '/' + vm.route.putCall + '/' + vm.route.strikePrice + '/' + vm.route.optAttribute + "/" + vm.route.maturityMonthYear;
        var defaultOrdering = ["-received"];
        var ordering = defaultOrdering;
        var sourceData = [];

        loadData();

        ///////////////////

        function loadData()
        {
            $http.get(restQueryUrl).success(function(data) {
                vm.errorMessage = "";
                processData(data);
                processGraphData(data);
                vm.initialLoad = false;
            }).error(function(data, status, headers, config) {
                vm.errorMessage = "Server returned status " + status;
                vm.initialLoad = false;
            });
        }

        function processData(data) {
            var index;

            for (index = 0; index < data.length; ++index) {
                data[index].netLS = data[index].crossMarginLongQty - data[index].crossMarginShortQty;
                data[index].netEA = (data[index].optionExcerciseQty - data[index].optionAssignmentQty) + (data[index].allocationTradeQty - data[index].deliveryNoticeQty);
            }
            
            sourceData = data;
            vm.recordCount = data.length;
            updateViewWindow(currentPage);
        }

        function processGraphData(data) {
            var chartData = [];
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

                chartData.push(tick);
            }

            vm.chartData = chartData;
        }

        function sortRecords(column) {
            ordering = sortRecordsService(column, ordering, defaultOrdering);
            updateViewWindow(currentPage);
        }

        function updateViewWindow(page) {
            currentPage = page;
            vm.viewWindow = vm.viewWindow = updateViewWindowService(sourceData, null, ordering, currentPage, vm.pageSize);
        }

        function showExtraInfo(funcKey) {
            showExtraInfoService(funcKey);
        }

        $scope.$on("$destroy", function() {
            if (refresh != null) {
                $interval.cancel(refresh);
            }
        });
    };
})();