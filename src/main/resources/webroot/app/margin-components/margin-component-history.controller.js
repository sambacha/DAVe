/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('MarginComponentHistoryController', MarginComponentHistoryController);

    function MarginComponentHistoryController($scope, $routeParams, $http, $interval, $filter, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService) {
        BaseHistoryController.call(this, $scope, $http, $interval, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService);
        var vm = this;
        vm.route = {
            "clearer": $routeParams.clearer,
            "member": $routeParams.member,
            "account": $routeParams.account,
            "class": $routeParams.class,
            "ccy": $routeParams.ccy
        };
        vm.defaultOrdering = ["-received"];
        vm.ordering = vm.defaultOrdering;
        vm.getTickFromRecord = getTickFromRecord;
        vm.getRestQueryUrl = getRestQueryUrl;
        vm.loadData();

        function getTickFromRecord(record) {
            var tick = {
                period: $filter('date')(record.received, "yyyy-MM-dd HH:mm:ss"),
                variationMargin: record.variationMargin,
                premiumMargin: record.premiumMargin,
                liquiMargin: record.liquiMargin,
                spreadMargin: record.spreadMargin,
                additionalMargin: record.additionalMargin
            };
            return tick;
        }

        function getRestQueryUrl() {
            return '/api/v1.0/mc/history/' + vm.route.clearer + '/' + vm.route.member + '/' + vm.route.account + '/' + vm.route.class + '/' + vm.route.ccy;
        }
    };
})();
