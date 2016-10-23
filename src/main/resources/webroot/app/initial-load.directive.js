/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').directive('initialLoad', InitialLoadDirective);

    function InitialLoadDirective() {
        return {
            restrict: 'E',
            templateUrl: 'app/initial-load.html'
        };
    };
})();