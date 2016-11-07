/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('DashboardController', DashboardController);

    function DashboardController() {
        var vm = this;
        vm.activeTab = "cv";
        vm.setActiveTab = setActiveTab;

        ////////////////////
        function setActiveTab(tabName) {
            vm.activeTab = tabName;
        }
    };
})();