/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('RiskLimitHistoryController', RiskLimitHistoryController);

    function RiskLimitHistoryController($scope, $routeParams, $http, $interval, $filter, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService) {
        BaseHistoryController.call(this, $scope, $http, $interval, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService);
        var vm = this;
        vm.route = {
            "clearer": $routeParams.clearer,
            "member": $routeParams.member,
            "maintainer": $routeParams.maintainer,
            "limitType": $routeParams.limitType
        };
        vm.defaultOrdering = ["-received"];
        vm.ordering = vm.defaultOrdering;
        vm.getTickFromRecord = getTickFromRecord;
        vm.getRestQueryUrl = getRestQueryUrl;
        vm.loadData();

        function getTickFromRecord(record) {
            var tick = {
                period: $filter('date')(record.received, "yyyy-MM-dd HH:mm:ss"),
                utilization: record.utilization,
                warningLevel: record.warningLevel,
                throttleLevel: record.throttleLevel,
                rejectLevel: record.rejectLevel
            };
            return tick;
        }

        function getRestQueryUrl() {
            return '/api/v1.0/rl/history/' + vm.route.clearer + '/' + vm.route.member + '/' + vm.route.maintainer + '/' + vm.route.limitType;
        }
    };
})();
