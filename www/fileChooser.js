module.exports = {
    open: function () {
        var success, failure;
        var resolvePath = false;
        if (typeof(arguments[0]) == "boolean") {
            resolvePath = arguments[0];
            success = arguments[1];
            failure = arguments[2];
        } else {
            success = arguments[0];
            failure = arguments[1];
        }
        cordova.exec(success, failure, "FileChooser", "open", [resolvePath]);
    },
    resolve: function(uri, success, failure) {
        cordova.exec(success, failure, "FileChooser", "resolve", [uri]);
    },
};
