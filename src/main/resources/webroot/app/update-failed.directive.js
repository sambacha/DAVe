/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').directive('updateFailed', UpdateFailedDirective);

    function UpdateFailedDirective() {
        return {
            restrict: 'E',
            scope: {
                errorMessage: '=message'
            },
            templateUrl: 'app/update-failed.html'
        };
    };
})();