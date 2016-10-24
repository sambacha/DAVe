/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('PositionReportLatestController', PositionReportLatestController);

    function PositionReportLatestController($scope, $routeParams, $http, $interval, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService) {
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
        vm.showExtraInfo = showExtraInfo;
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

        processRouting();

        var currentPage = 1;
        var refresh = $interval(loadData, 60000);
        var sourceData = [];
        var defaultOrdering = ["clearer", "member", "account", "symbol", "putCall", "strikePrice", "optAttribute", "maturityMonthYear"];
        var ordering = defaultOrdering;
        var restQueryUrl = '/api/v1.0/pr/latest/' + vm.route.clearer + '/' + vm.route.member + '/' + vm.route.account + '/' + vm.route.class + '/' + vm.route.symbol + '/' + vm.route.putCall + '/' + vm.route.strikePrice + '/' + vm.route.optAttribute + "/" + vm.route.maturityMonthYear;

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
            if ($routeParams.clearer) { vm.route.clearer = $routeParams.clearer; } else { vm.route.clearer = "*" }
            if ($routeParams.member) { vm.route.member = $routeParams.member; } else { vm.route.member = "*" }
            if ($routeParams.account) { vm.route.account = $routeParams.account; } else { vm.route.account = "*" }
            if ($routeParams.class) { vm.route.class = $routeParams.class; } else { vm.route.class = "*" }
            if ($routeParams.symbol) { vm.route.symbol = $routeParams.symbol; } else { vm.route.symbol = "*" }
            if ($routeParams.putCall) { vm.route.putCall = $routeParams.putCall; } else { vm.route.putCall = "*" }
            if ($routeParams.strikePrice) { vm.route.strikePrice = $routeParams.strikePrice; } else { vm.route.strikePrice = "*" }
            if ($routeParams.optAttribute) { vm.route.optAttribute = $routeParams.optAttribute; } else { vm.route.optAttribute = "*" }
            if ($routeParams.maturityMonthYear) { vm.route.maturityMonthYear = $routeParams.maturityMonthYear; } else { vm.route.maturityMonthYear = "*" }
        }

        function processData(data) {
            var index;

            for (index = 0; index < data.length; ++index) {
                data[index].functionalKey = data[index].clearer + '-' + data[index].member + '-' + data[index].account + '-' + data[index].clss + '-' + data[index].symbol + '-' + data[index].putCall + '-' + data[index].maturityMonthYear + '-' + data[index].strikePrice.replace("\.", "") + '-' + data[index].optAttribute + '-' + data[index].maturityMonthYear;
                data[index].netLS = data[index].crossMarginLongQty - data[index].crossMarginShortQty;
                data[index].netEA = (data[index].optionExcerciseQty - data[index].optionAssignmentQty) + (data[index].allocationTradeQty - data[index].deliveryNoticeQty);
            }

            sourceData = data;
            filter();
            updateViewWindow(currentPage);
        }

        function sortRecords(column) {
            ordering = sortRecordsService(column, ordering, defaultOrdering);
            updateViewWindow(currentPage);
        }

        function updateViewWindow(page) {
            currentPage = page;
            vm.viewWindow = updateViewWindowService(sourceData, vm.filterQuery, ordering, currentPage, vm.pageSize);
        }

        function filter() {
            vm.recordCount = recordCountService(sourceData, vm.filterQuery);
            updateViewWindow(currentPage);
        }

        function showExtraInfo(funcKey) {
            showExtraInfoService(funcKey);
        }

        $scope.$on("$destroy", function() {
            if (refresh != null) {
                $interval.cancel(refresh);
            }
        });
    };
})();