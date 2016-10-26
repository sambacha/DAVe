/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').directive('tableSummary', TableSummaryDirective);

    function TableSummaryDirective() {
        return {
            restrict: 'E',
            scope: {
                recordsTotal: '=total',
                recordsShown: '=shown'
            },
            templateUrl: 'app/table-summary.html'
        };
    };
})();