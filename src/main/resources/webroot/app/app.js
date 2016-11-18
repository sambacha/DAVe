/**
 * Created by jakub on 15.12.14.
 */

(function() {
    'use strict';

    var dave = angular.module('dave', [
        'ngRoute',
        'angular.morris',
        'googlechart'
    ]);

    dave.config(['$httpProvider', function($httpProvider) {
        $httpProvider.defaults.withCredentials = true;
    }]);

    dave.constant('hostConfig', {
        restURL: '/api/v1.0' // 'http(s)://someUrl:port/path'
    });
})();