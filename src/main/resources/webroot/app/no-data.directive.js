/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').directive('noData', NoDataDirective);

    function NoDataDirective() {
        return {
            restrict: 'E',
            templateUrl: 'app/no-data.html'
        };
    };
})();