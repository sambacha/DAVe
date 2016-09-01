/**
 * Created by jakub on 15.12.14.
 */

var opnFiRiskFilters = angular.module('opnFiRiskFilters', []);

opnFiRiskFilters.filter('spacedFilter', function() {
    return function(items, filter) {
        if (filter) {
            var filters = filter.split(" ");
            var index;
            var index2;
            var filteredItems = [];

            for (index = 0; index < items.length; index++) {
                var match = true;

                for (index2 = 0; index2 < filters.length; index2++) {
                    if (items[index].functionalKey.indexOf(filters[index2]) == -1) {
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

opnFiRiskFilters.filter('nullHandler', function() {
    return function(item, subst) {
        subst = subst || "null";
        item = item || subst;

        return item;
    };
});