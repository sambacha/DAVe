/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('MenuController', MenuController);

    function MenuController($location) {
        var vm = this;
        vm.amIActive = amIActive;

        function amIActive(item) {
            if ($location.url().indexOf(item) > -1) {
                return "active";
            }
        };
    };
})();