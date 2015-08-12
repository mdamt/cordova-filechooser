module.exports = {
    open: function (success, failure) {
        cordova.exec(success, failure, "FileChooser", "open", []);
    },
    resolve: function(uri, success, failure) {
        cordova.exec(success, failure, "FileChooser", "resolve", [uri]);
    },
};
