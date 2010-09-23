function FaustYUI() {  return YUI({ base: cp + "/static/yui3/build/", combine: false }); }

function get(uri, callback, reviver) {
	FaustYUI().use("io-xdr", "json-parse", function(Y) {
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
}