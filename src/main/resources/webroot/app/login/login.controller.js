/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('LoginController', LoginController);

    function LoginController($scope, $http, $interval, $rootScope, $location) {
        $rootScope.authStatus = false;
        $rootScope.authUsername = "";

        var vm = this;
        vm.username = null;
        vm.password = null;
        vm.errorMessage = null;
        vm.login = login;
        vm.logout = logout;

        var url = {
            "status": '/api/v1.0/user/loginStatus',
            "login": '/api/v1.0/user/login',
            "logout": '/api/v1.0/user/logout'
        };

        checkAuth();
        var refresh = $interval(checkAuth,60000);

        ////////////////////

        function checkAuth() {
            $http.get(url.status).success(function (data) {
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

                    if ($location.path() != "/login") {
                        $location.path("/login");
                    }
                }
            })
                .error(function (data) {
                    $rootScope.authStatus = false;
                    $rootScope.authUsername = "";

                    if ($location.path() != "/login") {
                        $location.path("/login");
                    }
                });
        }

        function login(username, password) {
            vm.errorMessage = null;
            var loginData = { "username": username, "password": password };

            $http.post(url.login, loginData).success(function(data) {
                $rootScope.authStatus = true;
                $rootScope.authUsername = username;

                if ($rootScope.authRequestedPath) {
                    var path = $rootScope.authRequestedPath;
                    $rootScope.authRequestedPath = null;
                    $location.path(path);
                }
            }).error(function(data) {
                vm.errorMessage = "Authentication failed. Is the username and password correct?";
            });
        }

        function logout() {
            $http.get(url.logout).success(function(data) {
                $rootScope.authStatus = false;
                $rootScope.authUsername = "";
                $location.path("/login");
            }).error(function(data) {
                // Nothing
            });
        }

        $scope.$on("$destroy", function() {
            if ($scope.refresh != null) {
                $interval.cancel(refresh);
            }
        });
    };
})();