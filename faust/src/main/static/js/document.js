MaterialUnit = function() {};
MaterialUnit.prototype.descendants = function() {
	var collect = function(list, mu) {
		mu.contents.forEach(function(child) { 
			list.push(child);
			collect(list, child); 
		});
	};

	var descendants = [];
	collect(descendants, this);
	return descendants; 
};
MaterialUnit.prototype.loadTranscription = function(callback) {
	if (this.transcript == null) return;
	FaustYUI.io(cp + "/" + this.transcript.source.encodedPath(), function(data) {
		callback(new MultiRootedTree(data));
	});
};

FaustDocument = function() {};
FaustDocument.load = function(uri, callback) {
	FaustYUI.io(cp + "/" + uri.encodedPath() + "/descriptor.json", callback, transformDocumentModel);
};

transformDocumentModel = function(key, value) {
	if (key === "order") {
		Y.augment(this, MaterialUnit);
	}
	if (key === "source") {			
		return new FaustURI(value);				
	}
	if (key === "facsimiles") {
		var facsimiles = []
		value.forEach(function(f) { facsimiles.push(new FaustURI(f)); });
		return facsimiles;
	}
	return value;
};

