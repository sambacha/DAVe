/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('MarginComponentTreemapController', MarginComponentTreemapController);

    function MarginComponentTreemapController($scope, $http, $interval) {
        var vm = this;
        vm.initialLoad= true;
        vm.errorMessage = "";
        vm.chartObject = {};

        var refresh = $interval(loadData, 60000);
        var restQueryUrl = '/api/v1.0/mc/latest/';

        loadData();

        function loadData(){
            $http.get(restQueryUrl).success(function(data) {
                processGraphData(data);
                vm.errorMessage = "";
                vm.initialLoad = false;
            }).error(function(data, status, headers, config) {
                vm.errorMessage = "Server returned status " + status;
                vm.initialLoad = false;
            });
        }

        function processGraphData(data) {
            var chartObject = {};
            chartObject.type = "TreeMap";
            chartObject.options = {
                    backgroundColor: {
                        fill: 'transparent'
                    },
                    minColor: '#f5f50a',
                    //minColor: '#c88c5a',
//                    midColor: '#ddd',
                    maxColor: '#FA6969',
                    fontColor: 'black',
                    showScale: false,
                    highlightOnMouseOver: true,
                    headerHeight: 15,
                    maxDepth: 1,
                    maxPostDepth: 2
                };
            chartObject.data = {
                cols: [{
                    id: 'aggregation',
                    type: 'string'
                }, {
                    id: 'parent',
                    type: 'string'
                }, {
                    id: 'margin',
                    type: 'number'
                }],
                rows: [
                    {
                        c: [{
                            v:'all'
                        }, {
                            v:null
                        }, {
                            v:0
                        }]
                    }]
            };

            var index;
            var members = {};
            var accounts = {};
            var classes = {};
            var tree = new Tree({id: 'all', value: 0});

            for (index = 0; index < data.length; ++index) {
                if (data[index].additionalMargin === 0) continue;

                var member = data[index].clearer + '-' + data[index].member;
                var account = data[index].clearer + '-' + data[index].member + '-' + data[index].account;
                var clss = data[index].clearer + '-' + data[index].member + '-' + data[index].account + '-' + data[index].clss;
                var ccy = data[index].clearer + '-' + data[index].member + '-' + data[index].account + '-' + data[index].clss + '-' + data[index].ccy;

                if (!(member in members))
                {
                    chartObject.data.rows.push({c: [{
                                v: member
                            }, {
                                v: 'all'
                            }, {
                                v: 0
                            }
                        ]});

                    members[member] = true;
                    tree.add({id: member, value: 0}, 'all');
                }

                if (!(account in accounts))
                {
                    chartObject.data.rows.push({c: [{
                                v: account
                            }, {
                                v: member
                            }, {
                                v: 0
                            }
                        ]});

                    accounts[account] = true;
                    tree.add({id: account, value: 0}, member);
                }

                if (!(clss in classes))
                {
                    chartObject.data.rows.push({c: [{
                        v: clss
                    }, {
                        v: account
                    }, {
                        v: 0
                    }
                    ]});

                    classes[clss] = true;
                    tree.add({id: clss, value: 0}, account);
                }

                chartObject.data.rows.push({c: [{
                            v: ccy
                        }, {
                            v: clss
                        }, {
                            v: data[index].additionalMargin
                        }
                    ]});
                tree.add({id: ccy, value: data[index].additionalMargin}, clss);
            }
            tree.traverseDF(function(node) {
                node.children.sort(function(a, b) {return a.data.value - b.data.value;});
            });
//            tree.traverseDF(function(node) {
//                console.log(node.data)
//            });
            vm.chartObject = chartObject;
        }

        $scope.$on("$destroy", function() {
            if (refresh !== null) {
                $interval.cancel(refresh);
            }
        });

        function Node(data) {
            this.data = data;
            this.parent = null;
            this.children = [];
        }

        function Tree(data) {
            var node = new Node(data);
            this._root = node;
        }

        Tree.prototype.traverseDF = function(callback) {
            (function recurse(currentNode) {
                for (var i = 0, length = currentNode.children.length; i < length; i++) {
                    recurse(currentNode.children[i]);
                }
                callback(currentNode);
            })(this._root);
        };

        Tree.prototype.contains = function(callback) {
            this.traverseDF(callback);
        };

        Tree.prototype.add = function(data, parentId) {
            var child = new Node(data),
                parent = null,
                callback = function(node) {
                    if (node.data.id === parentId) {
                        parent = node;
                    }
            };
            this.contains(callback);
            if (parent) {
                parent.children.push(child);
                child.parent = parent;
                while (parent !== null) {
                    parent.data.value += child.data.value;
                    parent = parent.parent;
                }
            } else {
                throw new Error('Cannot add node to a non-existent parent.');
            }
        };

    };
})();