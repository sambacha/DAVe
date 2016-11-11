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
                    minColor: '#f39d3c',
                    midColor: '#ec7a08',
                    maxColor: '#b35c00',
                    fontColor: 'black',
                    showScale: false,
                    highlightOnMouseOver: true,
                    headerHeight: 15,
                    maxDepth: 1,
                    maxPostDepth: 1
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
                rows: []
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
                    members[member] = true;
                    tree.add({id: member, value: 0}, 'all');
                }

                if (!(account in accounts))
                {
                    accounts[account] = true;
                    tree.add({id: account, value: 0}, member);
                }

                if (!(clss in classes))
                {
                    classes[clss] = true;
                    tree.add({id: clss, value: 0}, account);
                }

                tree.add({id: ccy, value: data[index].additionalMargin}, clss);
            }
            tree.traverseDF(function(node) {
                node.children.sort(function(a, b) {return b.data.value - a.data.value;});
            });
            tree.traverseBF(function(node) {
                var restNode = new Node({id: node.data.id + "-REST", value: 0});
                restNode.parent = node;
                var aggregateCount = Math.max(node.children.length - 10, 0);
                for (var i = 0; i < aggregateCount; i++) {
                    var smallNode = node.children.pop();
                    restNode.data.value += smallNode.data.value;
                    restNode.children = restNode.children.concat(smallNode.children);
                    for (var j = 0; j < smallNode.children.length; j++) {
                        smallNode.children[j].parent = restNode;
                    }
                }
                if (aggregateCount > 0) {
                    node.children.push(restNode);
                }
            });
            tree.traverseDF(function(node) {
                chartObject.data.rows.push({c: [{
                            v: node.data.id
                        }, {
                            v: node.parent !== null ? node.parent.data.id : null
                        }, {
                            v: node.children.length > 0 ? 0 : node.data.value
                        }
                    ]});
            });
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
        
        Tree.prototype.traverseBF = function(callback) {
          var queue = [];
          queue.push(this._root);
          var currentTree = queue.pop();
          while(currentTree){
              callback(currentTree);
              for (var i = 0, length = currentTree.children.length; i < length; i++) {
                  queue.push(currentTree.children[i]);
              }
              currentTree = queue.pop();
          }
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