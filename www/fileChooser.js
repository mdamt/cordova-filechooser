module.exports = {
    open: function (params, success, failure) {
        cordova.exec(success, failure, "FileChooser", "open", [ params ]);
    },
    resolve: function(uri, success, failure) {
        cordova.exec(success, failure, "FileChooser", "resolve", [uri]);
    },
};
