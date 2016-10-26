function BaseHistoryController($scope, $http, $interval, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService) {
    BaseController.call(this, $scope, $http, $interval, sortRecordsService, recordCountService, updateViewWindowService, showExtraInfoService)
    var vm = this;
    vm.filterQuery = null;
    vm.processData = processData;

    function processData(data) {
        vm.sourceData = data;
        vm.recordCount = data.length;
        vm.updateViewWindow(vm.currentPage);
        processGraphData(data);
    }

    function processGraphData(data) {
        var chartData = [];
        var index;

        for (index = 0; index < data.length; ++index) {
            var tick = vm.getTickFromRecord(data[index]);
            chartData.push(tick);
        }
        vm.chartData = chartData;
    }

}

