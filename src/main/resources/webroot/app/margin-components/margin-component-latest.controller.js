/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('MarginComponentLatestController', MarginComponentLatestController);

    function MarginComponentLatestController($scope, $routeParams, $http, $interval, $filter) {
        var vm = this;
        vm.initialLoad= true;
        vm.pageSize = 20;
        vm.recordCount = 0;
        vm.viewWindow = [];
        vm.updateViewWindow = updateViewWindow;
        vm.errorMessage = "";
        vm.filter = filter;
        vm.filterQuery = "";
        vm.sortRecords = sortRecords;
        vm.route = {
            "clearer": "*",
            "member": "*",
            "account": "*",
            "class": "*",
            "ccy": "*"
        };

        processRouting();

        var currentPage = 1;
        var refresh = $interval(loadData, 60000);
        var sourceData = [];
        var ordering = ["clearer", "member", "account", "class", "ccy"];
        var restQueryUrl = '/api/v1.0/mc/latest/' + vm.route.clearer + '/' + vm.route.member + '/' + vm.route.account + '/' + vm.route.class + '/' + vm.route.ccy;

        loadData();

        ////////////////////

        function loadData(){
            $http.get(restQueryUrl).success(function(data) {
                processData(data);
                vm.errorMessage = "";
                vm.initialLoad = false;
            }).error(function(data, status, headers, config) {
                vm.errorMessage = "Server returned status " + status;
                vm.initialLoad = false;
            });
        }

        function processRouting()
        {
            if ($routeParams.clearer) { vm.route.clearer = $routeParams.clearer } else { vm.route.clearer = "*" }
            if ($routeParams.member) { vm.route.member = $routeParams.member } else { vm.route.member = "*" }
            if ($routeParams.account) { vm.route.account = $routeParams.account } else { vm.route.account = "*" }
            if ($routeParams.class) { vm.route.class = $routeParams.class } else { vm.route.class = "*" }
            if ($routeParams.ccy) { vm.route.ccy = $routeParams.ccy } else { vm.route.ccy = "*" }
        }

        function processData(data) {
            var index;

            for (index = 0; index < data.length; ++index) {
                data[index].functionalKey = data[index].clearer + '-' + data[index].member + '-' + data[index].account + '-' + data[index].clss + '-' + data[index].ccy;
            }

            sourceData = data;
            filter();
            updateViewWindow(currentPage);
        }

        function sortRecords(column) {
            if (ordering[0] == column)
            {
                ordering = ["-" + column, "clearer", "member", "account", "class", "ccy"];
            }
            else {
                ordering = [column, "clearer", "member", "account", "class", "ccy"];
            }

            updateViewWindow(currentPage);
        }

        function updateViewWindow(page) {
            currentPage = page;
            vm.viewWindow = $filter('orderBy')($filter('spacedFilter')(sourceData, vm.filterQuery), ordering).slice(currentPage*vm.pageSize-vm.pageSize, currentPage*vm.pageSize);
        }

        function filter() {
            vm.recordCount = $filter('spacedFilter')(sourceData, vm.filterQuery).length;
            updateViewWindow(currentPage);
        }

        $scope.$on("$destroy", function() {
            if (refresh != null) {
                $interval.cancel(refresh);
            }
        });
    };
})();