/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('PagingController', PagingController);

    function PagingController($scope) {
        var vm = this;
        vm.first = {"class": "disabled"};
        vm.previous = {"class": "disabled"};
        vm.pages = [];
        vm.next = {"class": "disabled"};
        vm.last= {"class": "disabled"};

        vm.goToFirst = goToFirst;
        vm.goToPrevious = goToPrevious;
        vm.goToNext = goToNext;
        vm.goToLast = goToLast;
        vm.goToPage = goToPage;

        var page = 1;
        var pageSize = $scope.pageSize ? $scope.pageSize : 20;
        var totalRecords = $scope.totalRecords();
        var updateCallback = $scope.updateCallback;

        updatePaging();

        $scope.$watch($scope.totalRecords, function(value) {
            totalRecords = value;
            updatePaging();
        });

        ////////////////////

        function goToNext() {
            if (page < Math.ceil(totalRecords/pageSize))
            {
                page++;
            }

            updateCallback(page);
            updatePaging();
        };

        function goToPrevious() {
            if (page > 1)
            {
                page--;
            }

            updateCallback(page);
            updatePaging();
        };

        function goToFirst() {
            page = 1;
            updateCallback(page);
            updatePaging();
        };

        function goToLast() {
            page = Math.ceil(totalRecords/pageSize);
            updateCallback(page);
            updatePaging();
        };

        function goToPage(pageNo) {
            page = pageNo;
            updateCallback(page);
            updatePaging();
        };

        function updatePaging() {
            var pageCount = Math.ceil(totalRecords/pageSize);

            // Move page if pagecount decreased
            if (page > pageCount)
            {
                page = pageCount;
                updateCallback(page);
            }

            // Move page if we got somehow below 1
            if (page < 1)
            {
                page = 1;
                updateCallback(page);
            }

            // First and previous buttons
            if (page == 1) {
                vm.first.class = "disabled";
                vm.previous.class = "disabled";
            }
            else {
                vm.first.class = "";
                vm.previous.class = "";
            }

            // Individual page buttons
            var pages = [];

            if (page > 3)
            {
                pages.push({"page": page-3, "class": ""});
            }

            if (page > 2)
            {
                pages.push({"page": page-2, "class": ""});
            }

            if (page > 1)
            {
                pages.push({"page": page-1, "class": ""});
            }

            pages.push({"page": page, "class": "active"});

            if (page < pageCount)
            {
                pages.push({"page": page+1, "class": ""});
            }

            if (page < pageCount-1)
            {
                pages.push({"page": page+2, "class": ""});
            }

            if (page < pageCount-2)
            {
                pages.push({"page": page+3, "class": ""});
            }

            vm.pages = pages;

            // Next and last buttons
            if (page == pageCount) {
                vm.next.class = "disabled";
                vm.last.class = "disabled";
            }
            else {
                vm.next.class = "";
                vm.last.class = "";
            }
        };
    };
})();