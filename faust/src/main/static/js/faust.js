if (typeof Faust === "undefined") Faust = {};

Faust.encodePath = function(path) {
	var encoded = "";
	var pathComponents = path.split("/");
	for (var pc = 0; pc < pathComponents.length; pc++)
		encoded += (encoded.length == 0 ? "" : "/") + encodeURI(pathComponents[pc]);
	return encoded;		
}

Faust.URI = function(uri) { 
	this.components = uri.match(/^faust:\/\/([^\/]+)\/(.*)/);
};

Faust.URI.prototype = {
	encodedPath: function() { return Faust.encodePath(this.components[2]); }
};

Faust.YUI = function() { 
	return YUI({ base: Faust.contextPath + "/static/yui3/build/", combine: false, gallery: "gallery-2011.06.15-19-18" }); 
};

Faust.io = function(uri, callback, reviver) {
	Faust.YUI().use("io", "json", function(Y) {
		Y.io(Faust.contextPath + "/" + uri, {
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

Faust.xml = function(uri, callback) {
	Faust.YUI().use("io", function(Y) {
		Y.io(Faust.contextPath + "/" + uri, {
			method: "GET",
			on: { 
				success: function(id, o, a) {
					callback(o.responseXML);
				}, 
				failure: function(id, o, a) { 
					Y.log("ERROR " + id + " " + a, "info", "Faust") }
				}
		});
	});
};