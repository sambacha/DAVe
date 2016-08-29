/**
 * Created by jakub on 15.12.14.
 */

var opnFiRiskControllers = angular.module('opnFiRiskControllers', ['angular.morris']);

opnFiRiskControllers.controller('Login', ['$scope', '$http', '$interval', '$rootScope', '$location',
    function($scope, $http, $interval, $rootScope, $location) {
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
                    path = $rootScope.authRequestedPath;
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
            loginData = { "username": username, "password": password };

            $http.post($scope.loginUrl, loginData).success(function(data) {
                $rootScope.authStatus = true;
                $rootScope.authUsername = username;

                if ($rootScope.authRequestedPath) {
                    path = $rootScope.authRequestedPath;
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
        },60000);

        $scope.$on("$destroy", function() {
            if ($scope.refresh != null) {
                $interval.cancel($scope.refresh);
            }
        });
    }]);

opnFiRiskControllers.controller('MarginComponentLatest', ['$scope', '$routeParams', '$http', '$interval', '$filter',
    function($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;
        $scope.sorting = false;

        $scope.mcLatest = [];
        $scope.existingRecords = [];
        $scope.error = "";
        $scope.ordering= ["member", "account", "clss", "ccy"];

        if ($routeParams.clearer) { $scope.clearer = $routeParams.clearer } else { $scope.clearer = "*" }
        if ($routeParams.member) { $scope.member = $routeParams.member } else { $scope.member = "*" }
        if ($routeParams.account) { $scope.account = $routeParams.account } else { $scope.account = "*" }
        if ($routeParams.class) { $scope.class = $routeParams.class } else { $scope.class = "*" }

        $scope.url = '/api/v1.0/latest/mc/' + $scope.clearer + '/' + $scope.member + '/' + $scope.account + '/' + $scope.class;

        $http.get($scope.url).success(function(data) {
            $scope.processMarginComponents(data);
            $scope.error = "";
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
        });

        $scope.processMarginComponents = function(marginComponents) {
            var index;

            for (index = 0; index < marginComponents.length; ++index) {
                marginComponents[index].functionalKey = marginComponents[index].clearer + '-' + marginComponents[index].member + '-' + marginComponents[index].account + '-' + marginComponents[index].clss + '-' + marginComponents[index].ccy;
            }

            $scope.mcLatest = marginComponents;
        }

        $scope.sortRecords = function(column) {
            if ($scope.ordering[0] == column)
            {
                $scope.ordering = ["-" + column, "member", "account", "clss", "ccy"];
            }
            else {
                $scope.ordering = [column, "member", "account", "clss", "ccy"];
            }
        };

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.processMarginComponents(data);
                $scope.error = "";
            }).error(function(data, status, headers, config) {
                $scope.error = "Server returned status " + status;
            });
        },60000);

        $scope.$on("$destroy", function() {
           if ($scope.refresh != null) {
               $interval.cancel($scope.refresh);
           }
        });
    }]);

opnFiRiskControllers.controller('MarginComponentHistory', ['$scope', '$routeParams', '$http', '$interval', '$filter',
    function($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;

        $scope.mcHistory = [];
        $scope.existingRecords = [];
        $scope.error = "";
        $scope.mcChartData = [];
        $scope.ordering="-received";

        $scope.clearer = $routeParams.clearer;
        $scope.member = $routeParams.member;
        $scope.account = $routeParams.account;
        $scope.class = $routeParams.class;
        $scope.ccy = $routeParams.ccy;

        $scope.url = '/api/v1.0/history/mc/' + $scope.clearer + '/' + $scope.member + '/' + $scope.account + '/' + $scope.class + '/' + $scope.ccy;

        $http.get($scope.url).success(function(data) {
            $scope.error = "";
            $scope.mcHistory = data;
            $scope.prepareGraphData(data);
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
        });

        $scope.sortRecords = function(column) {
            if ($scope.ordering == column)
            {
                $scope.ordering = "-" + column;
            }
            else {
                $scope.ordering = column;
            }
        };

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.error = "";
                $scope.mcHistory = data;
                //$scope.dtInstance.DataTable.rows().add(data);
                $scope.prepareGraphData(data);
            }).error(function(data, status, headers, config) {
                $scope.error = "Server returned status " + status;
            });
        },60000);

        $scope.$on("$destroy", function() {
            if ($scope.refresh != null) {
                $interval.cancel($scope.refresh);
            }
        });

        $scope.prepareGraphData = function(data) {
            $scope.mcChartData = []

            var index;

            for (index = 0; index < data.length; ++index) {
                tick = {
                    period: $filter('date')(data[index].received, "yyyy-MM-dd HH:mm:ss"),
                    variationMargin: data[index].variationMargin,
                    premiumMargin: data[index].premiumMargin,
                    liquiMargin: data[index].liquiMargin,
                    spreadMargin: data[index].spreadMargin,
                    additionalMargin: data[index].additionalMargin
                };

                $scope.mcChartData.push(tick);
            }
        }
    }]);

opnFiRiskControllers.controller('TotalMarginRequirementLatest', ['$scope', '$routeParams', '$http', '$interval', '$filter',
    function($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;
        $scope.sorting = false;

        $scope.tmrLatest = [];
        $scope.existingRecords = [];
        $scope.error = "";
        $scope.ordering= ["pool", "member", "account", "ccy"];

        if ($routeParams.clearer) { $scope.clearer = $routeParams.clearer } else { $scope.clearer = "*" }
        if ($routeParams.pool) { $scope.pool = $routeParams.pool } else { $scope.pool = "*" }
        if ($routeParams.member) { $scope.member = $routeParams.member } else { $scope.member = "*" }
        if ($routeParams.account) { $scope.account = $routeParams.account } else { $scope.account = "*" }

        $scope.url = '/api/v1.0/latest/tmr/' + $scope.clearer + '/' + $scope.pool + '/' + $scope.member + '/' + $scope.account;

        $http.get($scope.url).success(function(data) {
            $scope.processTotalMarginRequirements(data);
            $scope.error = "";
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
        });

        $scope.processTotalMarginRequirements = function(totalMarginRequirements) {
            var index;

            for (index = 0; index < totalMarginRequirements.length; ++index) {
                totalMarginRequirements[index].functionalKey = totalMarginRequirements[index].clearer + '-' + totalMarginRequirements[index].pool + '-' + totalMarginRequirements[index].member + '-' + totalMarginRequirements[index].account + '-' + totalMarginRequirements[index].ccy;
            }

            $scope.tmrLatest = totalMarginRequirements;
        }

        $scope.sortRecords = function(column) {
            if ($scope.ordering[0] == column)
            {
                $scope.ordering = ["-" + column, "pool", "member", "account", "ccy"];
            }
            else {
                $scope.ordering = [column, "pool", "member", "account", "ccy"];
            }
        };

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.processTotalMarginRequirements(data);
                $scope.error = "";
            }).error(function(data, status, headers, config) {
                $scope.error = "Server returned status " + status;
            });
        },60000);

        $scope.$on("$destroy", function() {
            if ($scope.refresh != null) {
                $interval.cancel($scope.refresh);
            }
        });
    }]);

opnFiRiskControllers.controller('TotalMarginRequirementHistory', ['$scope', '$routeParams', '$http', '$interval', '$filter',
    function($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;

        $scope.tmrHistory = [];
        $scope.existingRecords = [];
        $scope.error = "";
        $scope.tmrChartData = [];
        $scope.tmrChartOptions = { legendTemplate : "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<datasets.length; i++){%><li><span style=\"background-color:<%=datasets[i].lineColor%>\"></span><%if(datasets[i].label){%><%=datasets[i].label%><%}%></li><%}%></ul>" };
        $scope.ordering="-received";

        $scope.clearer = $routeParams.clearer;
        $scope.pool = $routeParams.pool;
        $scope.member = $routeParams.member;
        $scope.account = $routeParams.account;
        $scope.ccy = $routeParams.ccy;

        $scope.url = '/api/v1.0/history/tmr/' + $scope.clearer + '/' + $scope.pool + '/' + $scope.member + '/' + $scope.account + '/' + $scope.ccy;

        $http.get($scope.url).success(function(data) {
            $scope.error = "";
            $scope.tmrHistory = data;
            $scope.prepareGraphData(data);
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
        });

        $scope.sortRecords = function(column) {
            if ($scope.ordering == column)
            {
                $scope.ordering = "-" + column;
            }
            else {
                $scope.ordering = column;
            }
        };

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.error = "";
                $scope.tmrHistory = data;
                $scope.prepareGraphData(data);
            }).error(function(data, status, headers, config) {
                $scope.error = "Server returned status " + status;
            });
        },60000);

        $scope.$on("$destroy", function() {
            if ($scope.refresh != null) {
                $interval.cancel($scope.refresh);
            }
        });

        $scope.prepareGraphData = function(data) {
            $scope.tmrChartData = []

            var index;

            for (index = 0; index < data.length; ++index) {
                tick = {
                    period: $filter('date')(data[index].received, "yyyy-MM-dd HH:mm:ss"),
                    adjustedMargin: data[index].adjustedMargin,
                    unadjustedMargin: data[index].unadjustedMargin
                };

                $scope.tmrChartData.push(tick);
            }
        }
    }]);

opnFiRiskControllers.controller('MarginShortfallSurplusLatest', ['$scope', '$routeParams', '$http', '$interval', '$filter',
    function($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;
        $scope.sorting = false;

        $scope.mssLatest = [];
        $scope.existingRecords = [];
        $scope.error = "";
        $scope.ordering= ["pool", "member", "clearingCcy", "ccy"];

        if ($routeParams.clearer) { $scope.clearer = $routeParams.clearer } else { $scope.clearer = "*" }
        if ($routeParams.pool) { $scope.pool = $routeParams.pool } else { $scope.pool = "*" }
        if ($routeParams.member) { $scope.member = $routeParams.member } else { $scope.member = "*" }
        if ($routeParams.clearingCcy) { $scope.clearingCcy = $routeParams.clearingCcy } else { $scope.clearingCcy = "*" }

        $scope.url = '/api/v1.0/latest/mss/' + $scope.clearer + '/' + $scope.pool + '/' + $scope.member + '/' + $scope.clearingCcy;

        $http.get($scope.url).success(function(data) {
            $scope.processMarginShortfallSurplus(data);
            $scope.error = "";
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
        });

        $scope.processMarginShortfallSurplus = function(marginShortfallSurplus) {
            var index;

            for (index = 0; index < marginShortfallSurplus.length; ++index) {
                marginShortfallSurplus[index].functionalKey = marginShortfallSurplus[index].clearer + '-' + marginShortfallSurplus[index].pool + '-' + marginShortfallSurplus[index].member + '-' + marginShortfallSurplus[index].clearingCcy + '-' + marginShortfallSurplus[index].ccy;
            }

            $scope.mssLatest = marginShortfallSurplus;
        }

        $scope.sortRecords = function(column) {
            if ($scope.ordering[0] == column)
            {
                $scope.ordering = ["-" + column, "pool", "member", "clearingCcy", "ccy"];
            }
            else {
                $scope.ordering = [column, "pool", "member", "clearingCcy", "ccy"];
            }
        };

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.processMarginShortfallSurplus(data);
                $scope.error = "";
            }).error(function(data, status, headers, config) {
                $scope.error = "Server returned status " + status;
            });
        },60000);

        $scope.$on("$destroy", function() {
            if ($scope.refresh != null) {
                $interval.cancel($scope.refresh);
            }
        });
    }]);

opnFiRiskControllers.controller('MarginShortfallSurplusHistory', ['$scope', '$routeParams', '$http', '$interval', '$filter',
    function($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;

        $scope.mssHistory = [];
        $scope.existingRecords = [];
        $scope.error = "";
        $scope.mssChartData = [];
        $scope.ordering="-received";

        $scope.clearer = $routeParams.clearer;
        $scope.pool = $routeParams.pool;
        $scope.member = $routeParams.member;
        $scope.clearingCcy = $routeParams.clearingCcy;
        $scope.ccy = $routeParams.ccy;

        $scope.url = '/api/v1.0/history/mss/' + $scope.clearer + '/' + $scope.pool + '/' + $scope.member + '/' + $scope.clearingCcy + '/' + $scope.ccy;

        $http.get($scope.url).success(function(data) {
            $scope.error = "";
            $scope.mssHistory = data;
            $scope.prepareGraphData(data);
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
        });

        $scope.sortRecords = function(column) {
            if ($scope.ordering == column)
            {
                $scope.ordering = "-" + column;
            }
            else {
                $scope.ordering = column;
            }
        };

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.error = "";
                $scope.mssHistory = data;
                $scope.prepareGraphData(data);
            }).error(function(data, status, headers, config) {
                $scope.error = "Server returned status " + status;
            });
        },60000);

        $scope.$on("$destroy", function() {
            if ($scope.refresh != null) {
                $interval.cancel($scope.refresh);
            }
        });

        $scope.prepareGraphData = function(data) {
            $scope.mssChartData = []

            var index;

            for (index = 0; index < data.length; ++index) {
                tick = {
                    period: $filter('date')(data[index].received, "yyyy-MM-dd HH:mm:ss"),
                    marginRequirement: data[index].marginRequirement,
                    securityCollateral: data[index].securityCollateral,
                    cashBalance: data[index].cashBalance,
                    shortfallSurplus: data[index].shortfallSurplus,
                    marginCall: data[index].marginCall
                };

                $scope.mssChartData.push(tick);
            }
        }
    }]);

opnFiRiskControllers.controller('Dashboard', ['$scope', '$routeParams', '$http', '$interval', '$filter',
    function($scope, $routeParams, $http, $interval, $filter) {
        $scope.chartMarginRequirementData = [];
        $scope.chartMarginShortfallSurplusData = [];
        $scope.chartMarginCallData = [];

        $scope.url = '/api/v1.0/latest/mss';

        $http.get($scope.url).success(function(data) {
            $scope.error = "";
            $scope.prepareGraphData(data);
        }).error(function(data, status, headers, config) {
            $scope.error = "Server returned status " + status;
        });

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.error = "";
                $scope.prepareGraphData(data);
            }).error(function(data, status, headers, config) {
                $scope.error = "Server returned status " + status;
            });
        },60000);

        $scope.$on("$destroy", function() {
            if ($scope.refresh != null) {
                $interval.cancel($scope.refresh);
            }
        });

        $scope.prepareGraphData = function(data) {
            $scope.chartMarginRequirementData = [];
            $scope.chartMarginShortfallSurplusData = [];
            $scope.chartMarginCallData = [];

            var index;

            for (index = 0; index < data.length; ++index) {
                marginRequirementTick = {
                    label: data[index].pool + " / " + data[index].member + " / " + data[index].clearingCcy,
                    value: data[index].marginRequirement
                };

                marginShortfallSurplusTick = {
                    label: data[index].pool + " / " + data[index].member + " / " + data[index].clearingCcy,
                    value: data[index].shortfallSurplus
                };

                marginCallTick = {
                    label: data[index].pool + " / " + data[index].member + " / " + data[index].clearingCcy,
                    value: data[index].marginCall
                };

                $scope.chartMarginRequirementData.push(marginRequirementTick);
                $scope.chartMarginShortfallSurplusData.push(marginShortfallSurplusTick);
                $scope.chartMarginCallData.push(marginCallTick);
            }
        }
    }]);

opnFiRiskControllers.controller('TssCtrl', ['$scope', '$http', '$interval', '$rootScope',
    function($scope, $http, $interval, $rootScope) {
        $scope.refresh = null;
        $scope.tss = null;
        $scope.url = '/api/v1.0/latest/tss';

        if ($rootScope.authStatus == true) {
            $http.get($scope.url).success(function (data) {
                $scope.tss = data;
            });
        }

        $scope.refresh = $interval(function(){
            if ($rootScope.authStatus == true) {
                $http.get($scope.url).success(function (data) {
                    $scope.tss = data;
                })
            }
        },60000);

        $scope.$on("$destroy", function() {
            if ($scope.refresh != null) {
                $interval.cancel($scope.refresh);
            }
        });
    }]);

opnFiRiskControllers.controller('MenuCtrl', ['$scope', "$location",
    function($scope, $location) {
        $scope.amIActive = function(item) {
            //console.log("Item: " + item + ", location.url: " + $location.url() + " ... result is: " + $location.url().indexOf(item));
            if ($location.url().indexOf(item) > -1) {
                return "active";
            }
        };
    }]);
