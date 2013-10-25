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

    var IOStatus = Y.Base.create('io-status', Y.Widget, [], {
        initializer: function() {
            this._transactions = {};

            this._ioStartHandle = Y.on("io:start", this._ioStart, this);
            this._ioEndHandle = Y.on("io:end", this._ioEnd, this);
        },
        destructor: function() {
            this._ioStartHandle && this._ioStartHandle.detach();
            this._ioEndHandle && this._ioEndHandle.detach();
        },
        renderUI: function() {
            this._status = this.get("contentBox").appendChild("<div class='io-status'/>");
            this._status.appendChild("<img/>").setAttrs({
                src: cp + "/static/img/spinner.gif",
                alt: "Loading"
            });
        },
        syncUI: function() {
            if ((Y.Object.size(this._transactions) > 0)) {
                this._status.show(true);
            } else {
                this._status.hide(true);
            }
        },
        _ioStart: function(tid) {
            this._transactions[tid] = Date.now();
            this.syncUI();
        },
        _ioEnd: function(tid) {
            delete this._transactions[tid];
            this.syncUI();
        }
    });

    Y.mix(Y.namespace("Faust"), {
        encodePath:encodePath,
        IOStatus: IOStatus,
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
    requires:["io", "widget"]
});