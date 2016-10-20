/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('TradingSessionStatusController', TradingSessionStatusController);

    function TradingSessionStatusController($scope, $http, $interval, $rootScope) {
        $scope.refresh = null;
        $scope.tss = null;
        $scope.url = '/api/v1.0/tss/latest';

        if ($rootScope.authStatus == true) {
            $http.get($scope.url).success(function (data) {
                $scope.tss = data;
            });
        }

        $scope.refresh = $interval(function(){
            if ($rootScope.authStatus == true) {
                $http.get($scope.url).success(function (data) {
                    $scope.tss = data;
                })
            }
        },60000);

        $scope.$on("$destroy", function() {
            if ($scope.refresh != null) {
                $interval.cancel($scope.refresh);
            }
        });
    };
})();