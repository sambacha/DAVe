/**
 * Created by jakub on 15.12.14.
 */

var sandboxDirectives = angular.module('sandboxDirectives', []);

sandboxDirectives.directive('highlighter', ['$timeout', function($timeout) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            scope.$watch(attrs.highlighter, function (nv, ov) {
                //console.log("Triggering highlighter: " + ov + " vs " + nv)
                element.hasClass("");
                if (nv !== ov) {
                    element.addClass('bg-warning');

                    $timeout(function () {
                        element.removeClass('bg-warning');
                    }, 15000);
                }
            });
        }
    };
}]);

sandboxDirectives.directive('rowHighlighter', ['$timeout', function($timeout) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            scope.$watch(attrs.rowHighlighter, function (nv, ov) {
                if ($.inArray(nv, scope.existingRecords) === -1) {
                    scope.existingRecords.push(nv);
                    $(element).children().each(function (index, child) { $(child).addClass('bg-warning'); });

                    $timeout(function () {
                        $(element).children().each(function (index, child) { $(child).removeClass('bg-warning'); });
                    }, 15000);
                }
            });
        }
    };
}]);

sandboxDirectives.directive('repeatDone', function() {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            console.log("Entering repeatDone");
            if (scope.$last) { // all are rendered
                //console.log("Sorting set to false");
            }
        }
    }
})