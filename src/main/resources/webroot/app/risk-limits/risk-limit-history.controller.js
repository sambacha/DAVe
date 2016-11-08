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
        vm.processData = processData;
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

        function processData(data) {
            for (var index = 0; index < data.length; ++index) {
                if (data[index].warningLevel > 0) {
                    data[index].warningUtil = data[index].utilization / data[index].warningLevel * 100;
                }

                if (data[index].throttleLevel > 0) {
                    data[index].throttleUtil = data[index].utilization / data[index].throttleLevel * 100;
                }

                if (data[index].rejectLevel > 0) {
                    data[index].rejectUtil = data[index].utilization / data[index].rejectLevel * 100;
                }
            }

            vm.sourceData = data;
            vm.recordCount = data.length;
            vm.updateViewWindow(vm.currentPage);
            vm.processGraphData(data);
        }

        function getRestQueryUrl() {
            return '/api/v1.0/rl/history/' + vm.route.clearer + '/' + vm.route.member + '/' + vm.route.maintainer + '/' + vm.route.limitType;
        }
    };
})();
