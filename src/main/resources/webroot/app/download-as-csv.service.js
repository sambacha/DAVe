/**
 * Created by jakub on 20/10/2016.
 */

(function() {
    'use strict';

    angular.module('dave').factory('downloadAsCsvService', DownloadAsCsvService);

    function DownloadAsCsvService() {
        return function(filename, data, includedKeys) {
            var processRow = function (row) {
                var keys = Object.keys(row);
                var finalVal = '';
                var first = true;
                for (var j = 0; j < keys.length; j++) {
                    if ($.inArray(keys[j], includedKeys) > -1)
                    {
                        var innerValue = row[keys[j]] === null ? '' : row[keys[j]].toString();

                        if (row[keys[j]] instanceof Date) {
                            innerValue = row[keys[j]].toLocaleString();
                        };

                        var result = innerValue.replace(/"/g, '""');

                        if (result.search(/("|,|\n)/g) >= 0)
                            result = '"' + result + '"';

                        if (!first)
                            finalVal += ',';

                        first = false;
                        finalVal += result;
                    }
                }
                return finalVal + '\n';
            };

            var createHeader = function (row) {
                var keys = Object.keys(row);
                var finalVal = '';
                var first = true;
                for (var j = 0; j < keys.length; j++) {
                    if ($.inArray(keys[j], includedKeys) > -1) {
                        var innerValue = keys[j] === null ? '' : keys[j].toString();
                        var result = innerValue.replace(/"/g, '""');
                        if (result.search(/("|,|\n)/g) >= 0)
                            result = '"' + result + '"';
                        if (!first)
                            finalVal += ',';
                        first = false;
                        finalVal += result;
                    }
                }
                return finalVal + '\n';
            };

            var csvFile = '';

            if (data.length > 0)
            {
                csvFile += createHeader(data[0])
            }

            for (var i = 0; i < data.length; i++) {
                csvFile += processRow(data[i]);
            }

            var blob = new Blob([csvFile], { type: 'text/csv;charset=utf-8;' });
            if (navigator.msSaveBlob) { // IE 10+
                navigator.msSaveBlob(blob, filename);
            } else {
                var link = document.createElement("a");
                if (link.download !== undefined) { // feature detection
                    // Browsers that support HTML5 download attribute
                    var url = URL.createObjectURL(blob);
                    link.setAttribute("href", url);
                    link.setAttribute("download", filename);
                    link.style.visibility = 'hidden';
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);
                }
            }
        };
    };
})();