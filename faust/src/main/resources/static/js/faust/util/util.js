YUI.add('util', function (Y) {

    var NS = Y.namespace("Faust");

    NS.encodePath = function (path) {
        var encoded = "";
        var pathComponents = path.split("/");
        for (var pc = 0; pc < pathComponents.length; pc++)
            encoded += (encoded.length == 0 ? "" : "/") + encodeURI(pathComponents[pc]);
        return encoded;
    };

    NS.URI = function (uri) {
        this.components = uri.match(/^faust:\/\/([^\/]+)\/(.*)/);
    };
    NS.URI.prototype.encodedPath = function () {
        return NS.encodePath(this.components[2]);
    };

    NS.IOStatus = Y.Base.create('io-status', Y.Widget, [], {
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

    NS.compareNumbers = function(a, b) { return (a - b); };

    var SortedArray = Y.namespace("Faust.SortedArray");

    SortedArray.mergeConcat = function(arr, startFirst, startSecond, end, cmp) {
        if (startSecond >= end) return arr;
        for(; startFirst < startSecond; startFirst++) {
            if (cmp(arr[startFirst], arr[startSecond]) > 0) {
                var val = arr[startFirst];
                arr[startFirst] = arr[startSecond];
                var start = startSecond;
                for (; (start + 1 < end) && cmp(arr[start + 1], val) < 0; start++) {
                    var tmp = arr[start];
                    arr[start] = arr[start + 1];
                    arr[start + 1] = tmp;
                }
                arr[start] = val;
            }
        }
        return arr;
    };

    SortedArray.merge = function(first, second, cmp) {
        return SortedArray.mergeConcat(first.concat(second), 0, first.length, first.length + second.length, cmp);
    };

    SortedArray.dedupe = function(arr, cmp) {
        var deduped = [];
        for (var ac = 0, al = arr.length; ac < al; ac++) {
            if (ac == 0 || (cmp(arr[ac - 1], arr[ac]) != 0)) deduped.push(arr[ac]);
        }
        return deduped;
    };

    SortedArray.search = function(arr, needle, cmp, start, end) {
        start = start || 0;
        end = (end || arr.length) - 1;

        while (start <= end) {
            var i = (start + end) / 2 | 0, val = arr[i], diff = cmp(val, needle);

            if (diff < 0) {
                start = ++i;
            } else if (diff > 0) {
                end = --i;
            } else {
                return i;
            }
        }

        return ~end;
    };

    NS.io = function (uri, callback, reviver) {
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
    };

    NS.xml = function (uri, callback) {
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
    };
}, '0.0', {
    requires:["io", "widget"]
});