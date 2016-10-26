/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('PositionReportLatestController', PositionReportLatestController);

    function PositionReportLatestController($scope, $routeParams, $http, $interval, $filter, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService) {
        BaseLatestController.call(this, $scope, $routeParams, $http, $interval, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService);
        var vm = this;
        vm.route = {
            "clearer": "*",
            "member": "*",
            "account": "*",
            "symbol": "*",
            "putCall": "*",
            "strikePrice": "*",
            "optAttribute": "*",
            "maturityMonthYear": "*"
        };
        vm.defaultOrdering = ["clearer", "member", "account", "symbol", "putCall", "strikePrice", "optAttribute", "maturityMonthYear"];
        vm.routingKeys = ["clearer", "member", "account", "symbol", "putCall", "strikePrice", "optAttribute", "maturityMonthYear"];
        vm.ordering = vm.defaultOrdering;
        vm.getRestQueryUrl = getRestQueryUrl;
        vm.processRecord = processRecord;

        vm.processRouting();
        vm.loadData();

        function processRecord(record) {
            record.functionalKey = record.clearer + '-' + record.member + '-' + record.account + '-' + record.symbol + '-' + record.putCall + '-' + record.maturityMonthYear + '-' + $filter('nullHandler')(record.strikePrice, "").replace("\.", "") + '-' + record.optAttribute + '-' + record.maturityMonthYear;
            record.netLS = record.crossMarginLongQty - record.crossMarginShortQty;
            record.netEA = ((record.optionExcerciseQty ? record.optionExcerciseQty : 0) - (record.optionAssignmentQty ? record.optionAssignmentQty : 0)) + ((record.allocationTradeQty ? record.allocationTradeQty : 0) - (record.deliveryNoticeQty ? record.deliveryNoticeQty : 0));;
        }

        function getRestQueryUrl() {
            return '/api/v1.0/pr/latest/' + vm.route.clearer + '/' + vm.route.member + '/' + vm.route.account + '/' + vm.route.symbol + '/' + vm.route.putCall + '/' + vm.route.strikePrice + '/' + vm.route.optAttribute + "/" + vm.route.maturityMonthYear;
        }
    };
})();
