/**
 * Created by jakub on 15.12.14.
 */

var daveFilters = angular.module('daveFilters', []);

daveFilters.filter('spacedFilter', function() {
    return function(items, filter) {
        if (filter) {
            var filters = filter.toLowerCase().split(" ");
            var index;
            var index2;
            var filteredItems = [];

            for (index = 0; index < items.length; index++) {
                var match = true;

                for (index2 = 0; index2 < filters.length; index2++) {
                    if (items[index].functionalKey.toLowerCase().indexOf(filters[index2]) == -1) {
                        match = false;
                        break;
                    }
                }

                if (match == true) {
                    filteredItems.push(items[index]);
                }
            }

            return filteredItems;
        }
        else {
            return items;
        }
    };
});

daveFilters.filter('nullHandler', function() {
    return function(item, subst) {
        subst = subst || "null";
        item = item || subst;

        return item;
    };
});