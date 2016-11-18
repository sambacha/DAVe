/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('TradingSessionStatusController', TradingSessionStatusController);

    function TradingSessionStatusController($scope, $http, $interval, $rootScope, hostConfig) {
        var vm = this;
        vm.tss = null;

        var url = hostConfig.restURL + '/tss/latest';
        var refresh = null;

        getTradingSessionStatus()

        ////////////////////

        function getTradingSessionStatus()
        {
            if ($rootScope.authStatus == true) {
                $http.get(url).success(function (data) {
                    vm.tss = data;
                });
            }
        }

        refresh = $interval(getTradingSessionStatus,60000);

        $scope.$on("$destroy", function() {
            if (refresh != null) {
                $interval.cancel(refresh);
            }
        });
    };
})();