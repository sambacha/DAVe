/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('PositionReportLatestController', PositionReportLatestController);

    function PositionReportLatestController($scope, $routeParams, $http, $interval, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService, downloadAsCsvService) {
        BaseLatestController.call(this, $scope, $routeParams, $http, $interval, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService, downloadAsCsvService);
        var vm = this;
        vm.route = {
            "clearer": "*",
            "member": "*",
            "account": "*",
            "class": "*",
            "symbol": "*",
            "putCall": "*",
            "strikePrice": "*",
            "optAttribute": "*",
            "maturityMonthYear": "*"
        };
        vm.defaultOrdering = ["clearer", "member", "account", "symbol", "putCall", "strikePrice", "optAttribute", "maturityMonthYear"];
        vm.routingKeys = ["clearer", "member", "account", "class", "symbol", "putCall", "strikePrice", "optAttribute", "maturityMonthYear"];
        vm.ordering = vm.defaultOrdering;
        vm.exportKeys = ["clearer", "member", "account", "bizDt", "symbol", "putCall", "maturityMonthYear", "strikePrice", "optAttribute",
            "crossMarginLongQty", "crossMarginShortQty", "optionExcerciseQty", "optionAssignmentQty", "allocationTradeQty", "deliveryNoticeQty",
            "clearingCcy", "mVar", "compVar", "compCorrelationBreak", "compCompressionError", "compLiquidityAddOn", "compLongOptionCredit",
            "productCcy", "variationMarginPremiumPayment", "premiumMargin", "delta", "gamma", "vega", "rho", "theta", "received", "clss",
            "underlying", "netLS", "netEA"
        ];
        vm.getRestQueryUrl = getRestQueryUrl;
        vm.processRecord = processRecord;

        vm.processRouting();
        vm.loadData();

        function processRecord(record) {
            record.functionalKey = record.clearer + '-' + record.member + '-' + record.account + '-' + record.clss + '-' + record.symbol + '-' + record.putCall + '-' + record.maturityMonthYear + '-' + record.strikePrice.replace("\.", "") + '-' + record.optAttribute + '-' + record.maturityMonthYear;
            record.netLS = record.crossMarginLongQty - record.crossMarginShortQty;
            record.netEA = (record.optionExcerciseQty - record.optionAssignmentQty) + (record.allocationTradeQty - record.deliveryNoticeQty);
        }

        function getRestQueryUrl() {
            return '/api/v1.0/pr/latest/' + vm.route.clearer + '/' + vm.route.member + '/' + vm.route.account + '/' + vm.route.class + '/' + vm.route.symbol + '/' + vm.route.putCall + '/' + vm.route.strikePrice + '/' + vm.route.optAttribute + "/" + vm.route.maturityMonthYear;
        }
    };
})();
