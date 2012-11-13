

YUI.add('document-ranges', function (Y) {		
	console.log('document-ranges');

	
	Faust.Namespaces = function() {

		this.FAUST_NS = 'http://www.faustedition.net/ns';
		this.TEI_NS = 'http://www.tei-c.org/ns/1.0';
		this.SVG_NS = 'http://www.w3.org/2000/svg';
		this.XML_NS = 'http://www.w3.org/XML/1998/namespace';
		this.GE_NS = 'http://www.tei-c.org/ns/geneticEditions';
		this.nsMap = {};

		this.prefixMap = {};

		this.register = function(prefix, ns) {
			this.nsMap[ns] = prefix;
			this.prefixMap[prefix] = ns;
		};
		
		this.p = function(qualifiedName) {
			var nsEnd = qualifiedName.indexOf('}');
			var ns = qualifiedName.slice(1, nsEnd);
			var name = qualifiedName.slice(nsEnd);
			var prefix = this.nsMap[ns];
			return prefix + ':' + name;
		};

		this.q = function(prefixedName) {
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
		this.register('tei', this.TEI_NS);
		this.register('svg', this.SVG_NS);
		this.register('xml', this.XML_NS);
		this.register('ge', this.GE_NS);


	};

	Faust.DocumentRanges = {
		
		transcriptVC: function(jsonRepresentation) {

			var VCs = {};

			var annotationCounter = 0;
			
			//map XML id to view component
			var idMap = {};

			//execute these after the build
			var postBuildDeferred = [];

			var ns = new Faust.Namespaces();
			
			var mainZoneVC = null;

			function registerId(annotation, vc) {
				xmlId = annotation.data[ns.q('xml:id')];
				if (xmlId) {
					idMap[xmlId] = vc;
					vc.xmlId = xmlId;
				}
			}

			function align(node, vc) {
				
				var aligningAttributes = ["f:at", "f:left", "f:left-right", "f:right", "f:right-left",
					                      "f:top", "f:top-bottom", "f:bottom", "f:bottom-top"];

				Y.each(aligningAttributes, function(a){

					var qA = ns.q(a);
					
					
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

						if (ns.q("f:orient") in node.data)
							myJoint = node.data[ns.q("f:orient")] == "left" ? 1 : 0;

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
			
			function createTextVC(range, transcript){
				var content = range.of(transcript.content)
				var textAttrs = {};

				var hands = transcript.find(range.start, range.end, ['hand']);
				if (hands.length < 1)
					throw (Faust.ENC_EXC_PREF + "No hand specified!");
				if (hands.length > 1)
					throw (Faust.ENC_EXC_PREF + "More than one hand specified!");

				textAttrs.hand = hands[0].data['value'];
				
				var rewrites = transcript.find(range.start, range.end, [ns.q('ge:rewrite')]);
				if (rewrites.length > 0) {
					textAttrs.rewrite = rewrites[0].data['hand'];
				}

				var unders = transcript.find(range.start, range.end, [ns.q('f:under')]);
				if (unders.length > 0) 
					textAttrs.under = true;

				var overs = transcript.find(range.start, range.end, [ns.q('f:over')]);
				if (overs.length > 0) 
					textAttrs.over = true;

				var strikethroughs = transcript.find(range.start, range.end, [ns.q('f:st')]);
				if (strikethroughs.length > 0) 
					textAttrs.strikethrough = true;

				var his = transcript.find(range.start, range.end, [ns.q('tei:hi')]);
				Y.each(his, function(hi) {
					if (hi.data['rend'])
						if (hi.data['rend'].indexOf('underline') >= 0)
							textAttrs.underline = true;
				});
				
				var lines = transcript.find(range.start, range.end, ['line']);
				Y.each (lines, function(l){
					var line_type = l.data['type'] || '';
					textAttrs.fontsize = line_type.indexOf('inter') >=0 ? 'small' : 'normal';
				});
				
				

				// Y.each(node.ancestors(), function(a) {
				// 	var elem = a.node;
				// 	if (elem.name == "f:hand") {
				// 		textAttrs.hand = elem.attrs["f:id"];
				// 	} else if (elem.name == "ge:rewrite") {
				// 		textAttrs.rewrite = elem.attrs["ge:hand"];
				// 	} else if (elem.name == "f:under") {
				// 		textAttrs.under = true;
				// 	} else if (elem.name == "f:over") {
				// 		textAttrs.over = true;
				// 	} else if (elem.name == "f:st") {
				// 		textAttrs.strikethrough = true;
				// 	} else if (elem.name == "tei:hi" && elem.attrs["tei:rend"].indexOf("underline") >= 0) {
				// 		textAttrs.underline = true;
				// 	} else if (elem.name == "ge:line") {
				// 		textAttrs.fontsize = ((elem.attrs["ge:type"] || "").indexOf("inter") >= 0 ? "small" : "normal");
				// 	}
				// });				

				return new Faust.Text(content, textAttrs);		
			};
			
			function existsVC (annotation) {
				return VCs[annotation.__hash]; 
			};
			
			function registerVC (annotation, vc) {
				annotation.__hash = annotationCounter++;
				VCs[annotation.__hash] = vc;					
			};

			function getVC(annotation) {
				return VCs[annotation.__hash]; 
			};
			
			function createVC (annotation, parentVC, vcFactory) {
				var vc = vcFactory();
				registerVC(annotation, vc);
				parentVC.add(vc);
				align(annotation, vc, postBuildDeferred);
				registerId(annotation, vc);
				return vc;
			};

			function createZoneVC(zone, parentVC) {

				var vc = createVC (zone, parentVC, function() {return new Faust.Zone()});
				
				if (zone.data["type"] === "main")  {
					// A new main zone will be created, when one already exists
					if (mainZoneVC != null)
						throw (Faust.ENC_EXC_PREF + "More than one main zone specified!");
					else  
						mainZoneVC = vc;
				}

				if ("rotate" in zone.data) 
					vc.rotation = parseInt(zone.data["rotate"]);
				return vc;
			};
			
			function createLineVC(annotation, parentVC) {
				return createVC (annotation, parentVC, function() {return new Faust.Line({})});
			};
			
			transcript = Y.Faust.Text.create(jsonRepresentation);
			
			var surfaceVC = new Faust.Surface();
			
			//for all partitions
			Y.each(transcript.partition(), function(p) {
				console.log (p.start + ' --- ' + p.end);
				// only use content inside a line
				if (transcript.find(p.start, p.end, 'line')[0]) {						
					console.log(p.of(transcript.content));
					var textVC = createTextVC(p, transcript);
					
					var structuralHierarchy = [{name:'zone', builder: createZoneVC},
						                       {name:'line', builder: createLineVC}];
					var vc;
					var parent = surfaceVC;
					
					Y.each(structuralHierarchy, function(element) {
						var annotations = transcript.find(p.start, p.end, element.name);
						if (annotations.length > 0) {
							var annotation = annotations[0];
							if (existsVC(annotation))
								vc = getVC(annotation);
							else
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


