/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').controller('PagingController', PagingController);

    function PagingController($scope, $attrs) {
        var paging = this;
        paging.first = {"class": "disabled"};
        paging.previous = {"class": "disabled"};
        paging.pages = [];
        paging.next = {"class": "disabled"};
        paging.last= {"class": "disabled"};

        paging.goToFirst = goToFirst;
        paging.goToPrevious = goToPrevious;
        paging.goToNext = goToNext;
        paging.goToLast = goToLast;
        paging.goToPage = goToPage;

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
                paging.first.class = "disabled";
                paging.previous.class = "disabled";
            }
            else {
                paging.first.class = "";
                paging.previous.class = "";
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

            paging.pages = pages;

            // Next and last buttons
            if (page == pageCount) {
                paging.next.class = "disabled";
                paging.last.class = "disabled";
            }
            else {
                paging.next.class = "";
                paging.last.class = "";
            }
        };
    };
})();