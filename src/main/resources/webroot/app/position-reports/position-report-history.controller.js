/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('PositionReportHistoryController', PositionReportHistoryController);

    function PositionReportHistoryController($scope, $routeParams, $http, $interval, $filter, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService) {
        BaseHistoryController.call(this, $scope, $http, $interval, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService);
        var vm = this;
        vm.route = {
            "clearer": $routeParams.clearer,
            "member": $routeParams.member,
            "account": $routeParams.account,
            "symbol": $routeParams.symbol,
            "putCall": $routeParams.putCall,
            "strikePrice": $routeParams.strikePrice,
            "optAttribute": $routeParams.optAttribute,
            "maturityMonthYear": $routeParams.maturityMonthYear
        };
        vm.defaultOrdering = ["-received"];
        vm.ordering = vm.defaultOrdering;
        vm.getTickFromRecord = getTickFromRecord;
        vm.getRestQueryUrl = getRestQueryUrl;
        vm.processData = processData;

        vm.loadData();

        function processData(data) {
            var index;

            for (index = 0; index < data.length; ++index) {
                data[index].netLS = data[index].crossMarginLongQty - data[index].crossMarginShortQty;
                data[index].netEA = ((data[index].optionExcerciseQty ? data[index].optionExcerciseQty : 0) - (data[index].optionAssignmentQty ? data[index].optionAssignmentQty : 0)) + ((data[index].allocationTradeQty ? data[index].allocationTradeQty : 0) - (data[index].deliveryNoticeQty ? data[index].deliveryNoticeQty : 0));
            }

            vm.sourceData = data;
            vm.recordCount = data.length;
            vm.updateViewWindow(vm.currentPage);
            vm.processGraphData(data);
        }

        function getTickFromRecord(record) {
            var tick = {
                period: $filter('date')(record.received, "yyyy-MM-dd HH:mm:ss"),
                netLS: record.netLS,
                netEA: record.netEA
            };
            return tick;
        }

        function getRestQueryUrl() {
            return '/api/v1.0/pr/history/' + vm.route.clearer + '/' + vm.route.member + '/' + vm.route.account + '/' + vm.route.symbol + '/' + vm.route.putCall + '/' + vm.route.strikePrice + '/' + vm.route.optAttribute + "/" + vm.route.maturityMonthYear;
        }
    };
})();
