/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('MenuController', MenuController);

    function MenuController($scope, $location) {
        $scope.amIActive = function(item) {
            if ($location.url().indexOf(item) > -1) {
                return "active";
            }
        };
    };
})();