/**
 * Created by jakub on 15.12.14.
 */

var sandbox = angular.module('sandbox', [
    'ngRoute',
    'angles',
    'sandboxControllers',
    'sandboxFilters',
    'sandboxDirectives'
]);

sandbox.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
            when('/mc-overview', {
                templateUrl: 'view/mc-overview.html',
                controller: 'MarginComponentOverview'
            }).
            when('/mc-overview/:clearer', {
                templateUrl: 'view/mc-overview.html',
                controller: 'MarginComponentOverview'
            }).
            when('/mc-overview/:clearer/:member', {
                templateUrl: 'view/mc-overview.html',
                controller: 'MarginComponentOverview'
            }).
            when('/mc-overview/:clearer/:member/:account', {
                templateUrl: 'view/mc-overview.html',
                controller: 'MarginComponentOverview'
            }).
            when('/mc-overview/:clearer/:member/:account/:class', {
                templateUrl: 'view/mc-overview.html',
                controller: 'MarginComponentOverview'
            }).
            when('/mc-detail/:clearer/:member/:account/:class/:ccy', {
                templateUrl: 'view/mc-detail.html',
                controller: 'MarginComponentDetail'
            }).
            otherwise({
                redirectTo: '/mc-overview'
            });
    }]);

