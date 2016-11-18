/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('MarginComponentLatestController', MarginComponentLatestController);

    function MarginComponentLatestController($scope, $routeParams, $http, $interval, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService, downloadAsCsvService, hostConfig) {
        BaseLatestController.call(this, $scope, $routeParams, $http, $interval, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService, downloadAsCsvService);
        var vm = this;
        vm.route = {
            "clearer": "*",
            "member": "*",
            "account": "*",
            "class": "*",
            "ccy": "*"
        };
        vm.defaultOrdering = ["clearer", "member", "account", "class", "ccy"];
        vm.routingKeys = ["clearer", "member", "account", "class", "ccy"];
        vm.ordering = vm.defaultOrdering;
        vm.exportKeys = ["clearer", "member", "account", "clss", "ccy", "bizDt", "variationMargin", "premiumMargin", "liquiMargin", "spreadMargin",
            "additionalMargin", "variLiqui", "received"
        ];
        vm.getRestQueryUrl = getRestQueryUrl;
        vm.processRecord = processRecord;

        vm.processRouting();
        vm.loadData();

        function processRecord(record) {
            record.functionalKey = record.clearer + '-' + record.member + '-' + record.account + '-' + record.clss + '-' + record.ccy;
            record.variLiqui = record.variationMargin + record.liquiMargin;
        }

        function getRestQueryUrl() {
            return hostConfig.restURL + '/mc/latest/' + vm.route.clearer + '/' + vm.route.member + '/' + vm.route.account + '/' + vm.route.class + '/' + vm.route.ccy;
        }
    };
})();