Faust = function() {};

Faust.URI = function(uri) { 
	this.components = uri.match(/^faust:\/\/([^\/]+)\/(.*)/);
};

Faust.URI.prototype = {
	encodedPath: function() {
		var encoded = "";
		var pathComponents = this.components[2].split("/");
		for (var pc = 0; pc < pathComponents.length; pc++)
			encoded += (encoded.length == 0 ? "" : "/") + encodeURI(pathComponents[pc]);
		return encoded;	
	}
};

Faust.YUI = function() { 
	return YUI({ base: cp + "/static/yui3/build/", combine: false }); 
};

Faust.io = function(uri, callback, reviver) {
	Faust.YUI().use("io", "json", function(Y) {
		Y.io(uri, {
			method: "GET",
			xdr: { responseXML: false },
			headers: { "Accept": "application/json" },
			on: { 
				success: function(id, o, a) {
					callback(Y.JSON.parse(o.responseText, reviver));
				}, 
				failure: function(id, o, a) { 
					Y.log("ERROR " + id + " " + a, "info", "Faust") }
				}
		});
	});
};