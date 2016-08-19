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
            when('/tmr-overview', {
                templateUrl: 'view/tmr-overview.html',
                controller: 'TotalMarginRequirementOverview'
            }).
            when('/tmr-overview/:clearer', {
                templateUrl: 'view/tmr-overview.html',
                controller: 'TotalMarginRequirementOverview'
            }).
            when('/tmr-overview/:clearer/:pool', {
                templateUrl: 'view/tmr-overview.html',
                controller: 'TotalMarginRequirementOverview'
            }).
            when('/tmr-overview/:clearer/:pool/:member', {
                templateUrl: 'view/tmr-overview.html',
                controller: 'TotalMarginRequirementOverview'
            }).
            when('/tmr-overview/:clearer/:pool/:member/:account', {
                templateUrl: 'view/tmr-overview.html',
                controller: 'TotalMarginRequirementOverview'
            }).
            when('/tmr-detail/:clearer/:pool/:member/:account/:ccy', {
                templateUrl: 'view/tmr-detail.html',
                controller: 'TotalMarginRequirementDetail'
            }).
            when('/mss-overview', {
                templateUrl: 'view/mss-overview.html',
                controller: 'MarginShortfallSurplusOverview'
            }).
            when('/mss-overview/:clearer', {
                templateUrl: 'view/mss-overview.html',
                controller: 'MarginShortfallSurplusOverview'
            }).
            when('/mss-overview/:clearer/:pool', {
                templateUrl: 'view/mss-overview.html',
                controller: 'MarginShortfallSurplusOverview'
            }).
            when('/mss-overview/:clearer/:pool/:member', {
                templateUrl: 'view/mss-overview.html',
                controller: 'MarginShortfallSurplusOverview'
            }).
            when('/mss-overview/:clearer/:pool/:member/:clearingCyy', {
                templateUrl: 'view/mss-overview.html',
                controller: 'MarginShortfallSurplusOverview'
            }).
            when('/mss-detail/:clearer/:pool/:member/:clearingCcy/:ccy', {
                templateUrl: 'view/mss-detail.html',
                controller: 'MarginShortfallSurplusDetail'
            }).
            otherwise({
                redirectTo: '/mc-overview'
            });
    }]);

