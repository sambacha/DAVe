/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('PositionReportLatestController', PositionReportLatestController);

    function PositionReportLatestController($scope, $routeParams, $http, $interval, $filter) {
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
        var ordering = ["clearer", "member", "account", "symbol", "putCall", "strikePrice", "optAttribute", "maturityMonthYear"];
        var restQueryUrl = '/api/v1.0/pr/latest/' + vm.route.clearer + '/' + vm.route.member + '/' + vm.route.account + '/' + vm.route.class + '/' + vm.route.symbol + '/' + vm.route.putCall + '/' + vm.route.strikePrice + '/' + vm.route.optAttribute + "/" + vm.route.maturityMonthYear;

        loadData();

        ////////////////////

        function loadData(){
            $http.get(restQueryUrl).success(function(data) {
                processData(data);
                vm.error = "";
                vm.initialLoad = false;
            }).error(function(data, status, headers, config) {
                vm.error = "Server returned status " + status;
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
            if (ordering[0] == column)
            {
                ordering = ["-" + column, "clearer", "member", "account", "symbol", "putCall", "strikePrice", "optAttribute", "maturityMonthYear"];
            }
            else {
                ordering = [column, "clearer", "member", "account", "symbol", "putCall", "strikePrice", "optAttribute", "maturityMonthYear"];
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

        function showExtraInfo(funcKey) {
            var extra = $("#extra-" + funcKey);
            var extraIcon = $("#extra-icon-" + funcKey);

            if (extra.hasClass("hidden"))
            {
                extra.removeClass("hidden");
                extraIcon.removeClass("fa-chevron-circle-down");
                extraIcon.addClass("fa-chevron-circle-up");
            }
            else {
                extra.addClass("hidden");
                extraIcon.removeClass("fa-chevron-circle-up");
                extraIcon.addClass("fa-chevron-circle-down");
            }
        }

        $scope.$on("$destroy", function() {
            if (refresh != null) {
                $interval.cancel(refresh);
            }
        });
    };
})();