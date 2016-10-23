/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('MarginShortfallSurplusHistoryController', MarginShortfallSurplusHistoryController);

    function MarginShortfallSurplusHistoryController($scope, $routeParams, $http, $interval, $filter) {
        var vm = this;
        vm.initialLoad= true;
        vm.pageSize = 20;
        vm.recordCount = 0;
        vm.viewWindow = [];
        vm.updateViewWindow = updateViewWindow;
        vm.chartData = [];
        vm.errorMessage = "";
        vm.sortRecords = sortRecords;
        vm.route = {
            "clearer": $routeParams.clearer,
            "pool": $routeParams.pool,
            "member": $routeParams.member,
            "clearingCcy": $routeParams.clearingCcy,
            "ccy": $routeParams.ccy
        };

        var currentPage = 1;
        var refresh = $interval(loadData, 60000);
        var restQueryUrl = '/api/v1.0/mss/history/' + vm.route.clearer + '/' + vm.route.pool + '/' + vm.route.member + '/' + vm.route.clearingCcy + '/' + vm.route.ccy;
        var ordering = ["-received"];
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
                    marginRequirement: data[index].marginRequirement,
                    securityCollateral: data[index].securityCollateral,
                    cashBalance: data[index].cashBalance,
                    shortfallSurplus: data[index].shortfallSurplus,
                    marginCall: data[index].marginCall
                };

                chartData.push(tick);
            }

            vm.chartData = chartData;
        }

        function sortRecords(column) {
            if (ordering[0] == column)
            {
                ordering = ["-" + column, "-received"];
            }
            else {
                ordering = [column, "-received"];
            }

            updateViewWindow(currentPage);
        }

        function updateViewWindow(page) {
            currentPage = page;
            vm.viewWindow = $filter('orderBy')(sourceData, ordering).slice(currentPage*vm.pageSize-vm.pageSize, currentPage*vm.pageSize);
        }

        $scope.$on("$destroy", function() {
            if (refresh != null) {
                $interval.cancel(refresh);
            }
        });
    };
})();