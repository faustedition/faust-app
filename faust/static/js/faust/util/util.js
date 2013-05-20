YUI.add('util', function (Y) {
    var encodePath = function (path) {
        var encoded = "";
        var pathComponents = path.split("/");
        for (var pc = 0; pc < pathComponents.length; pc++)
            encoded += (encoded.length == 0 ? "" : "/") + encodeURI(pathComponents[pc]);
        return encoded;
    };

    var URI = function (uri) {
        this.components = uri.match(/^faust:\/\/([^\/]+)\/(.*)/);
    };
    URI.prototype.encodedPath = function () {
        return encodePath(this.components[2]);
    };

    Y.mix(Y.namespace("Faust"), {
        encodePath:encodePath,
        URI:URI,
        io:function (uri, callback, reviver) {
            Faust.YUI().use("io", "json", function (Y) {
                Y.io(cp + "/" + uri, {
                    method:"GET",
                    xdr:{ responseXML:false },
                    headers:{ "Accept":"application/json" },
                    on:{
                        success:function (id, o) {
                            callback(Y.JSON.parse(o.responseText, reviver));
                        },
                        failure:function (id, o, a) {
                            Y.log("ERROR " + id + " " + a, "info", "Faust")
                        }
                    }
                });
            });
        },
        xml:function (uri, callback) {
            Y.io(cp + "/" + uri, {
                method:"GET",
                on:{
                    success:function (id, o, a) {
                        callback(o.responseXML);
                    },
                    failure:function (id, o, a) {
                        Y.log("ERROR " + id + " " + a, "info", "Faust")
                    }
                }
            });
        }
    });
}, '0.0', {
    requires:["io"]
});