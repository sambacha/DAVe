/**
 * Created by jakub on 15.12.14.
 */

var sandboxControllers = angular.module('sandboxControllers', []);

sandboxControllers.controller('MarginComponentOverview', ['$scope', '$routeParams', '$http', '$interval', '$filter',
    function($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;
        $scope.sorting = false;

        $scope.mcOverview = [];
        $scope.existingRecords = [];
        $scope.ordering= ["member", "account", "clss"];

        if ($routeParams.clearer) { $scope.clearer = $routeParams.clearer } else { $scope.clearer = "*" }
        if ($routeParams.member) { $scope.member = $routeParams.member } else { $scope.member = "*" }
        if ($routeParams.account) { $scope.account = $routeParams.account } else { $scope.account = "*" }
        if ($routeParams.class) { $scope.class = $routeParams.class } else { $scope.class = "*" }

        $scope.url = 'http://localhost:9000/api/0.1/mc-overview/' + $scope.clearer + '/' + $scope.member + '/' + $scope.account + '/' + $scope.class;

        $http.get($scope.url).success(function(data) {
            $scope.processMarginComponents(data);
        });

        $scope.processMarginComponents = function(marginComponents) {
            var index;

            for (index = 0; index < marginComponents.length; ++index) {
                marginComponents[index].functionalKey = marginComponents[index].clearer + '-' + marginComponents[index].member + '-' + marginComponents[index].account + '-' + marginComponents[index].clss + '-' + marginComponents[index].ccy;
            }

            $scope.mcOverview = marginComponents;
        }

        $scope.sortRecords = function(column) {
            $scope.ordering = [column, "member", "account", "clss"];
        };

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.processMarginComponents(data);
            })
        },60000);

        $scope.$on("$destroy", function() {
           if ($scope.refresh != null) {
               $interval.cancel($scope.refresh);
           }
        });
    }]);

sandboxControllers.controller('MarginComponentDetail', ['$scope', '$routeParams', '$http', '$interval', '$filter',
    function($scope, $routeParams, $http, $interval, $filter) {
        $scope.refresh = null;

        $scope.mcDetail = [];
        $scope.existingRecords = [];
        $scope.mcChartData = [];
        $scope.mcChartOptions = { legendTemplate : "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<datasets.length; i++){%><li><span style=\"background-color:<%=datasets[i].lineColor%>\"></span><%if(datasets[i].label){%><%=datasets[i].label%><%}%></li><%}%></ul>" };
        $scope.ordering="-received";

        $scope.clearer = $routeParams.clearer;
        $scope.member = $routeParams.member;
        $scope.account = $routeParams.account;
        $scope.class = $routeParams.class;
        $scope.ccy = $routeParams.ccy;

        $scope.url = 'http://localhost:9000/api/0.1/mc-detail/' + $scope.clearer + '/' + $scope.member + '/' + $scope.account + '/' + $scope.class + '/' + $scope.ccy;

        $http.get($scope.url).success(function(data) {
            $scope.mcDetail = data;
            $scope.prepareGraphData(data);
        });

        $scope.sortRecords = function(column) {
            $scope.ordering = column;
        };

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.mcDetail = data;
                $scope.prepareGraphData(data);
            })
        },60000);

        $scope.$on("$destroy", function() {
            if ($scope.refresh != null) {
                $interval.cancel($scope.refresh);
            }
        });

        $scope.prepareGraphData = function(data) {
            //$scope.mcChartOptions = {};
            $scope.mcChartData = {
                labels: [],
                datasets: [
                    {
                        label: "Variation Margin",
                        fillColor: "rgba(220,220,220,0.2)",
                        strokeColor: "rgba(220,220,220,1)",
                        pointColor: "rgba(220,220,220,1)",
                        pointStrokeColor: "#fff",
                        pointHighlightFill: "#fff",
                        pointHighlightStroke: "rgba(220,220,220,1)",
                        data: []
                    },
                    {
                        label: "Premium Margin",
                        fillColor: "rgba(151,187,205,0.2)",
                        strokeColor: "rgba(151,187,205,1)",
                        pointColor: "rgba(151,187,205,1)",
                        pointStrokeColor: "#fff",
                        pointHighlightFill: "#fff",
                        pointHighlightStroke: "rgba(151,187,205,1)",
                        data: []
                    },
                    {
                        label: "Liquidation Margin",
                        fillColor: "rgba(220,220,220,0.2)",
                        strokeColor: "rgba(220,220,220,1)",
                        pointColor: "rgba(220,220,220,1)",
                        pointStrokeColor: "#fff",
                        pointHighlightFill: "#fff",
                        pointHighlightStroke: "rgba(220,220,220,1)",
                        data: []
                    },
                    {
                        label: "Spread Margin",
                        fillColor: "rgba(151,187,205,0.2)",
                        strokeColor: "rgba(151,187,205,1)",
                        pointColor: "rgba(151,187,205,1)",
                        pointStrokeColor: "#fff",
                        pointHighlightFill: "#fff",
                        pointHighlightStroke: "rgba(151,187,205,1)",
                        data: []
                    },
                    {
                        label: "Additional Margin",
                        fillColor: "rgba(151,187,205,0.2)",
                        strokeColor: "rgba(151,187,205,1)",
                        pointColor: "rgba(151,187,205,1)",
                        pointStrokeColor: "#fff",
                        pointHighlightFill: "#fff",
                        pointHighlightStroke: "rgba(151,187,205,1)",
                        data: []
                    }
                ]
            };

            var index;

            for (index = 0; index < data.length; ++index) {
                $scope.mcChartData.labels.push($filter('date')(data[index].received, "dd.MM.yyyy HH:mm:ss"));
                $scope.mcChartData.datasets[0].data.push(data[index].variationMargin);
                $scope.mcChartData.datasets[1].data.push(data[index].premiumMargin);
                $scope.mcChartData.datasets[2].data.push(data[index].liquiMargin);
                $scope.mcChartData.datasets[3].data.push(data[index].spreadMargin);
                $scope.mcChartData.datasets[4].data.push(data[index].additionalMargin);
            }
        }
    }]);

sandboxControllers.controller('TssCtrl', ['$scope', '$http', '$interval',
    function($scope, $http, $interval) {
        $scope.refresh = null;
        $scope.url = 'http://localhost:9000/api/0.1/tss';
        $http.get($scope.url).success(function(data) {
            $scope.tss = data;
        });

        $scope.refresh = $interval(function(){
            $http.get($scope.url).success(function(data) {
                $scope.tss = data;
            })
        },60000);

        $scope.$on("$destroy", function() {
            if ($scope.refresh != null) {
                $interval.cancel($scope.refresh);
            }
        });
    }]);
