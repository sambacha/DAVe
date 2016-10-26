/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('RiskLimitLatestController', RiskLimitLatestController);

    function RiskLimitLatestController($scope, $routeParams, $http, $interval, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService) {
        BaseLatestController.call(this, $scope, $routeParams, $http, $interval, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService);
        var vm = this;
        vm.route = {
            "clearer": "*",
            "member": "*",
            "maintainer": "*",
            "limitType": "*"
        };
        vm.defaultOrdering = ["clearer", "member", "maintainer", "limitType"];
        vm.routingKeys = ["clearer", "member", "maintainer", "limitType"];
        vm.ordering = vm.defaultOrdering;
        vm.getRestQueryUrl = getRestQueryUrl;
        vm.processRecord = processRecord;

        vm.processRouting();
        vm.loadData();

        function processRecord(record) {
            record.functionalKey = record.clearer + '-' + record.member + '-' + record.maintainer + '-' + record.limitType;
        }

        function getRestQueryUrl() {
            return '/api/v1.0/rl/latest/' + vm.route.clearer + '/' + vm.route.member + '/' + vm.route.maintainer + '/' + vm.route.limitType;
        }
    };
})();
