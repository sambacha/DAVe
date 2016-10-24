/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').value('googleChartApiConfig', googleChartApiConfig);

    var googleChartApiConfig = {
        version: '1.1',
        optionalSettings: {
            packages: ['treemap'] //load just the package you want
        }
    };
})();