/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('LoginController', LoginController);

    function LoginController($scope, $http, $interval, $rootScope, $location) {
        $rootScope.authStatus = false;
        $rootScope.authUsername = "";
        $scope.authError = null;
        $scope.refresh = null;
        $scope.statusUrl = '/api/v1.0/user/loginStatus';
        $scope.loginUrl = '/api/v1.0/user/login';
        $scope.logoutUrl = '/api/v1.0/user/logout';

        $http.get($scope.statusUrl).success(function(data) {
            if (data.username != null) {
                $rootScope.authStatus = true;
                $rootScope.authUsername = data.username;

                if ($rootScope.authRequestedPath) {
                    var path = $rootScope.authRequestedPath;
                    $rootScope.authRequestedPath = null;
                    $location.path(path);
                }
            }
            else {
                $rootScope.authStatus = false;
                $rootScope.authUsername = "";

                if ($location.path() != "/login")
                {
                    $location.path( "/login" );
                }
            }
        })
            .error(function(data) {
                $rootScope.authStatus = false;
                $rootScope.authUsername = "";

                if ($location.path() != "/login")
                {
                    $location.path( "/login" );
                }
            });

        $scope.login = function(username, password) {
            $scope.authError = null;
            var loginData = { "username": username, "password": password };

            $http.post($scope.loginUrl, loginData).success(function(data) {
                $rootScope.authStatus = true;
                $rootScope.authUsername = username;

                if ($rootScope.authRequestedPath) {
                    var path = $rootScope.authRequestedPath;
                    $rootScope.authRequestedPath = null;
                    $location.path(path);
                }
            }).error(function(data) {
                $scope.authError = "Authentication failed. Is the username and password correct?";
            });
        }

        $scope.logout = function(username, password) {
            $http.get($scope.logoutUrl).success(function(data) {
                $rootScope.authStatus = false;
                $rootScope.authUsername = "";
                $location.path( "/login" );
            }).error(function(data) {
                // Nothing
            });
        }

        $scope.refresh = $interval(function(){
            $http.get($scope.statusUrl).success(function(data) {
                if (data.username != null) {
                    $rootScope.authStatus = true;
                    $rootScope.authUsername = data.username;
                }
                else {
                    $rootScope.authStatus = false;
                    $rootScope.authUsername = "";

                    if ($location.path() != "/login")
                    {
                        $rootScope.authRequestedPath = $location.path();
                        $location.path( "/login" );
                    }
                }
            })
                .error(function(data) {
                    $rootScope.authStatus = false;
                    $rootScope.authUsername = "";

                    if ($location.path() != "/login")
                    {
                        $rootScope.authRequestedPath = $location.path();
                        $location.path( "/login" );
                    }
                });
        },60000);

        $scope.$on("$destroy", function() {
            if ($scope.refresh != null) {
                $interval.cancel($scope.refresh);
            }
        });
    };
})();