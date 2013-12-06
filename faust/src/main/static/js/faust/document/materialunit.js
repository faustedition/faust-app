YUI.add('materialunit', function (Y) {		
	var MaterialUnit = function() {};
	MaterialUnit.prototype = {
		descendants: function() {
			return (function(list, mu) {
				for (var cc = 0; cc < mu.contents.length; cc++) {
					list.push(mu.contents[cc]);
					arguments.callee(list, mu.contents[cc]);
				}
				return list;
			})([], this);
		},
		transcription: function(callback) {
			if (this.transcript == null) { callback(); return; }
			Faust.io("goddag/" + this.transcript.source.encodedPath() + "?snapshot=true", function(data) {
				callback(new Goddag.Graph(data));
			});
		},
		
		transcriptionFromRanges: function(callback) {
			if (this.transcript == null) {
				callback(); 
				return; 
			}
			Faust.io("transcript/source/" + this.id, function(data) {
				callback(data);
			});
		}
		
	};

	Y.mix(Y.namespace("Faust"), {
		MaterialUnit: MaterialUnit
	});

	var Document = function() {};
	Document.load = function(uri, callback) {
		Faust.io(uri.encodedPath() /* + "/descriptor.json" */, callback, function(key, value) {
			if (key === "order") {
				Y.augment(this, MaterialUnit);
			}
			if (key === "source") {			
				return new Faust.URI(value);				
			}
			if (key === "facsimiles") {
				var facsimiles = []
				for (var vc = 0; vc < value.length; vc++)
					facsimiles.push(new Faust.URI(value[vc]));
				return facsimiles;
			}
			return value;
		});
	};

	Y.mix(Y.namespace("Faust"), {
		Document: Document
	});


}, '0.0', {
	requires: ['oop']
});


