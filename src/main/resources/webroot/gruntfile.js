module.exports = function (grunt) {
    'use strict';

    var fallback = require('connect-history-api-fallback'),
        log = require('connect-logger'),
        cssPattern = ['app/**/*.css', 'vendor/**/*.css'],
        htmlPattern = ['app/**/*.html', 'vendor/**/*.html', 'index.html'],
        jsPattern = ['app/**/*.js', 'vendor/**/*.js'];

    grunt.initConfig({

        browserSync: {
            dev: {
                bsFiles: {
                    src: cssPattern.concat(htmlPattern).concat(jsPattern)
                },
                options: {
                    injectChanges: false,
                    ui: false,
                    server: {
                        baseDir: './',
                        middleware: [
                            log({format: '%date %status %method %url'}),
                            fallback({
                                index: '/index.html',
                                htmlAcceptHeaders: ['text/html', 'application/xhtml+xml'] // systemjs workaround
                            })
                        ]
                    },
                    browser: [
                        'chrome',
                        'google chrome' // For macOS
                    ],
                    watchTask: false
                }
            }
        }
    });

    // Dev run tools
    grunt.loadNpmTasks('grunt-browser-sync');


    // register at least this one task
    grunt.registerTask('default', ['browserSync']);
};