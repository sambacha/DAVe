/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('MarginComponentLatestAccountAggregationController', MarginComponentLatestAccountAggregationController);

    function MarginComponentLatestAccountAggregationController($scope, $http, $interval, $filter, sortRecordsService) {
        var vm = this;
        vm.defaultOrdering = ["clearer", "member", "account"];
        vm.ordering = vm.defaultOrdering;
        vm.initialLoad= true;
        vm.recordCount = 0;
        vm.errorMessage = "";
        vm.viewWindow = [];
        vm.viewSum = {
            "variationMargin": 0,
            "premiumMargin": 0,
            "liquiMargin": 0,
            "spreadMargin": 0,
            "additionalMargin": 0};
        vm.sortRecords = sortRecords;

        var refresh = $interval(loadData, 60000);

        loadData();

        ////////////////////

        function loadData() {
            $http.get(getRestQueryUrl()).success(function(data) {
                processData(data);
                vm.errorMessage = "";
                vm.initialLoad = false;
            }).error(function(data, status, headers, config) {
                vm.errorMessage = "Server returned status " + status;
                vm.initialLoad = false;
            });
        }

        function sortRecords(column) {
            vm.ordering = sortRecordsService(column, vm.ordering, vm.defaultOrdering);
            vm.viewWindow = $filter('orderBy')(vm.viewWindow, vm.ordering);
        }

        function processData(data) {
            var index;
            var newViewWindow = {};
            var sum = {
                "variationMargin": 0,
                "premiumMargin": 0,
                "liquiMargin": 0,
                "spreadMargin": 0,
                "additionalMargin": 0
            };

            for (index = 0; index < data.length; ++index) {
                var fKey = functionalKey(data[index]);

                if (fKey in newViewWindow)
                {
                    newViewWindow[fKey].variationMargin += data[index].variationMargin;
                    newViewWindow[fKey].premiumMargin += data[index].premiumMargin;
                    newViewWindow[fKey].liquiMargin += data[index].liquiMargin;
                    newViewWindow[fKey].spreadMargin += data[index].spreadMargin;
                    newViewWindow[fKey].additionalMargin += data[index].additionalMargin;

                    sum.variationMargin += data[index].variationMargin;
                    sum.premiumMargin += data[index].premiumMargin;
                    sum.liquiMargin += data[index].liquiMargin;
                    sum.spreadMargin += data[index].spreadMargin;
                    sum.additionalMargin += data[index].additionalMargin;
                }
                else {
                    newViewWindow[fKey] = {
                        "functionalKey": fKey,
                        "clearer": data[index].clearer,
                        "member": data[index].member,
                        "account": data[index].account,
                        "variationMargin": data[index].variationMargin,
                        "premiumMargin": data[index].premiumMargin,
                        "liquiMargin": data[index].liquiMargin,
                        "spreadMargin": data[index].spreadMargin,
                        "additionalMargin": data[index].additionalMargin
                    };

                    sum.variationMargin += data[index].variationMargin;
                    sum.premiumMargin += data[index].premiumMargin;
                    sum.liquiMargin += data[index].liquiMargin;
                    sum.spreadMargin += data[index].spreadMargin;
                    sum.additionalMargin += data[index].additionalMargin;
                }
            }

            vm.viewWindow = $filter('orderBy')(Object.keys(newViewWindow).map(function (key) { return newViewWindow[key]; }), vm.ordering);
            vm.viewSum = sum;
        }

        function functionalKey(record) {
            return record.clearer + '-' + record.member + '-' + record.account;
        }

        function getRestQueryUrl() {
            return '/api/v1.0/mc/latest/';
        }

        $scope.$on("$destroy", function() {
            if (refresh != null) {
                $interval.cancel(refresh);
            }
        });
    };
})();