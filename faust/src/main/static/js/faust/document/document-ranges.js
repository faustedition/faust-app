SVG_NS = "http://www.w3.org/2000/svg";

YUI.add('document-ranges', function (Y) {		
	console.log('document-ranges');

	
	Faust.Namespaces = function() {

		this.FAUST_NS = 'http://www.faustedition.net/ns';

		this.nsMap = {};

		this.prefixMap = {};

		this.register = function(prefix, ns) {
			this.nsMap[ns] = prefix;
			this.prefixMap[prefix] = ns;
		};
		
		this.prefixForm = function(qualifiedName) {
			var nsEnd = qualifiedName.indexOf('}');
			var ns = qualifiedName.slice(1, nsEnd);
			var name = qualifiedName.slice(nsEnd);
			var prefix = this.nsMap[ns];
			return prefix + ':' + name;
		};

		this.qualifiedForm = function(prefixedName) {
			var prefixEnd = prefixedName.indexOf(':');
			var prefix = prefixedName.slice(0, prefixEnd);
			var name = prefixedName.slice(prefixEnd + 1);
			var ns = this.prefixMap[prefix];
			return '{' + ns + '}' + name;			
		};
		
		// qualifier: function(namespace) {
		// 	return function(name) {
		// 		return '{' + namespace + '}' + name;
		// 	}
		// }

		this.register('f', this.FAUST_NS);
	};

	Faust.DocumentRanges = {
		
		transcriptVC: function(jsonRepresentation) {

			var VCs = {};

			var annotationCounter = 0;
			
			//map XML id to view component
			var idMap = {};

			//execute these after the build
			var postBuildDeferred = [];

			var namespaces = new Faust.Namespaces();
			
			function registerId(annotation, vc) {
				xmlId = annotation.data["{http://www.w3.org/XML/1998/namespace}id"];
				if (xmlId) {
					idMap[xmlId] = vc;
					vc.xmlId = xmlId;
				}
			}

			function align(node, vc) {
				
				var aligningAttributes = ["f:at", "f:left", "f:left-right", "f:right", "f:right-left",
					                      "f:top", "f:top-bottom", "f:bottom", "f:bottom-top"];

				Y.each(aligningAttributes, function(a){					
					//var q = namespaces.qualifiedForm;
					var qA = namespaces.qualifiedForm(a);
					
					
					if (qA in node.data) {
						if (!vc) {
							vc = new Faust.DefaultVC();
						}
						//FIXME id hash hack; do real resolution of references
						var anchorId = node.data[qA].slice(1);
						var coordRot = a in {"f:at":1, "f:left":1, "f:left-right":1, "f:right":1, "f:right-left":1}? 0 : 90;
						var alignName = coordRot == 0 ? "hAlign" : "vAlign";
						var myJoint = a in {"f:left":1, "f:left-right":1, "f:top":1, "f:top-bottom":1}? 0 : 1;
						var yourJoint = a in {"f:at":1, "f:left":1, "f:right-left":1, "f:top":1, "f:bottom-top":1}? 0 : 1;

						if (namespaces.qualifiedForm("f:orient") in node.data)
							myJoint = node.data[namespaces.qualifiedForm("f:orient")] == "left" ? 1 : 0;

						postBuildDeferred.push(
							function(){
								var anchor = idMap[anchorId];
								if (!anchor)
									throw (Faust.ENC_EXC_PREF + "Reference to #" + anchorId + " cannot be resolved!");
								var globalCoordRot = coordRot + anchor.globalRotation();
								vc.setAlign(alignName, new Faust.Align(vc, anchor, globalCoordRot, myJoint, yourJoint, Faust.Align.EXPLICIT));
							});
					}						
				});
			};				
			
			function createTextVC(content){
				var textAttrs = {};
				return new Faust.Text(content, textAttrs);		
			};
			
			function existsVC (annotation) {
				return VCs[annotation.__hash]; 
			}
			
			function registerVC (annotation, vc) {
				annotation.__hash = annotationCounter++;
				VCs[annotation.__hash] = vc;					
			}
			
			function createVC (annotation, parentVC, vcFactory) {
				if (!existsVC (annotation)) {
					var vc = vcFactory();
					registerVC(annotation, vc);
					parentVC.add(vc);
					align(annotation, vc, postBuildDeferred);
					registerId(annotation, vc);
					return vc;
				} else 
					var vc = VCs[annotation.__hash]; 
				//parentVC.add(vc);
				return vc;
			};
			
			function createZoneVC(zone, parentVC) {
				return createVC (zone, parentVC, function() {return new Faust.Zone()});
			};
			
			function createLineVC(line, parentVC) {
				return createVC (line, parentVC, function() {return new Faust.Line({})});
			};
			
			transcript = Y.Faust.Text.create(jsonRepresentation);
			
			var surfaceVC = new Faust.Surface();
			
			
			
			//for all partitions
			Y.each(transcript.partition(), function(p) {
				console.log (p.start + ' --- ' + p.end);
				// only use content inside a zone
				if (transcript.find(p.start, p.end, 'line')[0]) {						
					console.log(p.of(transcript.content));
					var textVC = createTextVC(p.of(transcript.content));
					
					var structuralHierarchy = [{name:'zone', builder: createZoneVC},
						                       {name:'line', builder: createLineVC}];
					var vc;
					var parent = surfaceVC;
					
					Y.each(structuralHierarchy, function(element) {
						var annotations = transcript.find(p.start, p.end, element.name);
						if (annotations.length > 0) {
							var annotation = annotations[0];								
							vc = element.builder(annotation, parent);
							parent = vc;
							
						}
					});
					vc.add(textVC);
				}
				
			});
			
			
			//				Y.Array.each(transcript.find(null, null, ['line']), function(line) {				
			//					var linetextVC = createTextVC(line.target().textContent());
			//					var lineVC = new Faust.Line({});
			//					
			//					//find the zone of the line
			//					
			//					var linestart = line.target().range.start;
			//					var lineend = line.target().range.end;
			//					
			//					var zone = transcript.find(linestart, lineend, 'zone')[0];
			//					var zoneVC = createZoneVC(zone);			
			//					lineVC.add(linetextVC);
			//					zoneVC.add(lineVC);
			//					
			//				});
			
			
			Y.each(postBuildDeferred, function(f) {f.apply(this)});

			return surfaceVC;
		}
	};
	
}, '0.0', {
	requires: ["document-model", "text-annotation"]
});


