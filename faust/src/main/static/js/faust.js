FaustYUI = function() { 
	return YUI({ base: cp + "/static/yui3/build/", combine: false }); 
};
FaustYUI.io = function(uri, callback, reviver) {
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
};

FaustURI = function(uri) { 
	this.components = uri.match(/^faust:\/\/([^\/]+)\/(.*)/);	
};
FaustURI.prototype.encodedPath = function() {
	var encoded = "";
	this.components[2].split("/").forEach(function(c) {
		encoded += (encoded.length == 0 ? "" : "/") + encodeURI(c);
	});
	return encoded;	
};