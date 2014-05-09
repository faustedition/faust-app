/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

	var aggregateEvents = function (event1, event2, targetEvent) {
		var eventsFired = [];
		Y.once(event1, function() {
			eventsFired.push(event1);
			if (eventsFired.indexOf(event2) >= 0) {
				eventsFired.splice(0);
				Y.fire(targetEvent);
			}
		});

		Y.once(event2, function() {
			eventsFired.push(event2);
			if (eventsFired.indexOf(event1) >= 0) {
				eventsFired.splice(0);
				Y.fire(targetEvent);
			}
		});
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
        },
		aggregateEvents: aggregateEvents
    });
}, '0.0', {
    requires:["io", "event"]
});