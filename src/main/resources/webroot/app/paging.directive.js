/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').directive('paging', PagingDirective);

    function PagingDirective() {
        return {
            restrict: 'E',
            scope: {
                pageSize: '=?pageSize',
                totalRecords: '&totalRecords',
                updateCallback: '=updateCallback'
            },
            controller: 'PagingController',
            controllerAs: 'paging',
            templateUrl: 'app/paging.html'
        };
    };
})();