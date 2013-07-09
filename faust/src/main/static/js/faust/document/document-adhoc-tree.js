YUI.add('document-adhoc-tree', function (Y) {

	var DocumentLayout = {
		// Text factory; the current model only delivers text nodes, some additional elements (gaps, insertion marks) need 
		// to be delivered to know their tree context (hands...) for visualisation
		createText : function(content, start, end, text){
			if (content.length < 1) throw "Cannot create empty text!";
			var textAttrs = {};
			var annotations = text.find(start, end)
			//ignore empty annotations at the borders
				.filter(function(x){var r = x.target().range; return r.start !== r.end});

			Y.each(annotations, function(a) {
				if (a.name.localName == "hand") {
					textAttrs.hand = a.data["value"];
				} else if (a.name.localName == "rewrite") {
					textAttrs.rewrite = a.data["hand"];
				} else if (a.name.localName == "under") {
					textAttrs.under = true;
				} else if (a.name.localName == "over") {
					textAttrs.over = true;
				} else if (a.name.localName == "st") {
					textAttrs.strikethrough = true;
				} else if (a.name.localName == "hi" && a.data["rend"] && a.data["rend"].indexOf("underline") >= 0) {
					textAttrs.underline = true;
				} else if (a.name.localName == "line") {
					textAttrs.fontsize = ((a.data["type"] || "").indexOf("inter") >= 0 ? "small" : "normal");
				}

			});				
			return new Faust.Text(content, textAttrs);				
		},
		
	};

	Y.mix(Y.namespace("Faust"), {
        DocumentLayout: DocumentLayout,
    });
	

	var DocumentAdhocTree = function() {
		this.mainZone = null;
		this.idMap = {};
		this.postBuildDeferred = [];
	}
	
	Y.extend(DocumentAdhocTree, Object, {


		buildVC: function(parent, tree, text) {
			
			if (tree == null) return null;
			var vc = null;
			var node = tree;
			var nodeIsInvisible = false;


			if ((node instanceof Y.Faust.TextNode) && (parent != null)) { //&& (parent instanceof Faust.Line)) {
				vc = Y.Faust.DocumentLayout.createText(node.content(), node.range.start, node.range.end, text);
			} else if (node instanceof Y.Faust.AnnotationNode) {
				var annotationStart = node.annotation.target().range.start;
				var annotationEnd = node.annotation.target().range.end;

				//ioc: configurable modules handle the construction of the view
				if (node.name().localName in Y.Faust.DocumentConfiguration.names) {
					vc = Y.Faust.DocumentConfiguration.names[node.name().localName](node, text, this);
				}

				//if (node.name().localName in {"document":1, "treeRoot":1} ) {
				//vc = new Faust.Surface();
				//} else
				//if (node.name().localName === "surface") {
				//vc = new Faust.Surface();
				//} else
				
				// if (node.name().localName === "zone")	{
				// 	vc = new Faust.Zone();
				// 	if ("rotate" in node.data()) 
				// 		vc.rotation = parseInt(node.data()["rotate"]);
				// 	if ("type" in node.data() && node.data()["type"] == "main") {
				// 		if (this.mainZone != null)
				// 			throw (Faust.ENC_EXC_PREF + "More than one main zone specified!");
				// 		else {
				// 			this.mainZone = vc;
				// 			// main zone is absolutely anchored
				// 			vc.setAlign('hAlign', new Faust.AbsoluteAlign(vc, 0, 0, Faust.Align.MAIN_ZONE));
				// 			vc.setAlign('vAlign', new Faust.AbsoluteAlign(vc, 0, 0, Faust.Align.MAIN_ZONE));
				// 		}
				// 	} 
				

				//} else

				// if (node.name().localName === "line") {
				// 	var lineAttrs = {};
				// 	var rendition = node.data()["rend"] || "";
				// 	if (rendition.indexOf("centered") >= 0) {
				// 		lineAttrs.center = true;
				// 	} else if (rendition.indexOf("indent") >= 0) {
				// 		var start = rendition.indexOf("indent-");
				// 		lineAttrs.indent = parseInt(rendition.substring(start + 7, rendition.length)) / 100.0;
				// 	}

				// 	var  position = node.data()["f:pos"] || "";

				// 	if (position.indexOf("over") >= 0)
				// 		lineAttrs.over = true;

				// 	if (position.indexOf("between") >= 0)
				// 		lineAttrs.between = true;

				// 	vc = new Faust.Line(lineAttrs);
				

				// } else 
				
				// if (node.name().localName == "vspace") {
				// 	//TODO real implementation, non-integer values

				// 	switch (node.data()["unit"]) {
				// 	case "lines":
				// 		if (node.data()['quantity']) {
				// 			vc = new Faust.VSpace(node.data()['quantity']);
				// 		} else throw (Faust.ENC_EXC_PREF + "f:vspace: Please specify @qunatity");
				// 		break;
				// 	default: 
				// 		throw (Faust.ENC_EXC_PREF + "Invalid unit for vspace element! Use 'lines'!");
				// 	}
				
				//				} else
				// if (node.name().localName === "gap") {
				// 	var gapChar = '\u00d7';
				// 	var gapUncertainChar = '\u00d7'; //'.';
				// 	switch (node.data()["unit"]) {
				// 	case "chars":
				// 		if (node.data()['quantity'] && node.data()['precision'] && 
				// 			node.data()['precision'] === 'medium') {
				// 			var representation = gapChar;
				// 			for (var nrChars=2; nrChars < node.data()["quantity"]; nrChars++) {
				// 				representation += gapUncertainChar;  
				// 			}
				// 			vc = Y.Faust.DocumentLayout.createText (representation + gapChar, 
				// 							 annotationStart,
				// 							 annotationEnd);															
				// 		} else if (node.data()['quantity']) {
				// 			var representation = '';
				// 			for (var nrChars=0; nrChars < node.data()["quantity"]; nrChars++) {
				// 				//representation += '\u2715'; //capital X
				// 				representation += gapChar; // small X
				// 			}
				// 			vc = Y.Faust.DocumentLayout.createText (representation, annotationStart, annotationEnd);							
				// 		} else if(node.data()['atLeast']) {
				// 			var representation = gapChar;
				// 			for (var nrChars=2; nrChars < node.data()["atLeast"]; nrChars++) {
				// 				representation += gapUncertainChar;  
				// 			}
				// 			vc = Y.Faust.DocumentLayout.createText (representation + gapChar, annotationStart, annotationEnd);							

				// 		} else {
				// 			throw (Faust.ENC_EXC_PREF + "Please specify either @qunatity or @atLeast");
				// 		}
				// 		break;
				// 	default: 
				// 		throw (Faust.ENC_EXC_PREF + "Invalid unit for gap element! Use 'chars'!");
				// 	}
				//} else
				
				if (node.name().localName == "hspace") {
					switch (node.data()["unit"]) {
					case "chars":
						if (node.data()['quantity']) {
							var width = String(node.data()['quantity']);
							vc = new Faust.HSpace(width);
						} else throw (Faust.ENC_EXC_PREF + "f:hspace: Please specify @qunatity");
						break;
					default: 
						throw (Faust.ENC_EXC_PREF + "Invalid unit for hspace element! Use 'chars'!");
					}
				}  else if (node.name().localName == "grLine") {
					if (node.data()['f:style'] == 'curly')
						vc = new Faust.GrLine();
				} else if (node.name().localName == "grBrace") {
					//vc = new Faust.GBrace();
				} else if (node.name().localName == "anchor") {
					//use empty text element as an anchor
					// FIXME make proper phrase/block context differentiation
					if (parent.elementName === "zone")
						vc = new Faust.Line([]);
					else
						vc = new Faust.Text("", {});
					
					
				} else if (node.name().localName == "ins" && node.data()["f:orient"] == "right") {
					vc = new Faust.DefaultVC();
					//Einweisungszeichen
					vc.add (Y.Faust.DocumentLayout.createText("\u2308", annotationStart, annotationEnd, text));
					// Default Elements
				} else if (node.name().localName in {'treeRoot':1, 'anchor':1}) {
					vc = new Faust.DefaultVC();
					//Invisible elements				
				} else if (node.name().localName in {"rdg":1}){
					nodeIsInvisible = true;
				}
				aligningAttributes = ["f:at", "f:left", "f:left-right", "f:right", "f:right-left", "f:top", "f:top-bottom", "f:bottom", "f:bottom-top"];
				
				var that = this;

				Y.each(aligningAttributes, function(a){
					if (a in node.data()) {
						if (!vc) {
							vc = new Faust.DefaultVC();
						}
						//FIXME id hash hack; do real resolution of references
						var anchorId = node.data()[a].substring(1);
						var coordRot = a in {"f:at":1, "f:left":1, "f:left-right":1, "f:right":1, "f:right-left":1}? 0 : 90;
						var alignName = coordRot == 0 ? "hAlign" : "vAlign";
						var myJoint = a in {"f:left":1, "f:left-right":1, "f:top":1, "f:top-bottom":1}? 0 : 1;
						var yourJoint = a in {"f:at":1, "f:left":1, "f:right-left":1, "f:top":1, "f:bottom-top":1}? 0 : 1;
						
						if ("f:orient" in node.data())
							myJoint = node.data()["f:orient"] == "left" ? 1 : 0;
						
						that.postBuildDeferred.push(
							function(){
								var anchor = that.idMap[anchorId];
								if (!anchor)
									throw (Faust.ENC_EXC_PREF + "Reference to #" + anchorId + " cannot be resolved!");
								var globalCoordRot = coordRot + anchor.globalRotation();
								vc.setAlign(alignName, new Faust.Align(vc, anchor, globalCoordRot, myJoint, yourJoint, Faust.Align.EXPLICIT));
							});
					}						
				});
				
				// TODO special treatment of zones
				if ("rend" in node.data()) {
					if (node.data()["rend"] == "right") {
				 		vc.setAlign("hAlign", new Faust.Align(vc, parent, parent.globalRotation(), 1, 1, Faust.Align.REND_ATTR));
					} else if (node.data()["rend"] == "left") {
				 		vc.setAlign("hAlign", new Faust.Align(vc, parent, parent.globalRotation(), 0, 0, Faust.Align.REND_ATTR));
					} else if (node.data()["rend"] == "centered") {
				 		vc.setAlign("hAlign", new Faust.Align(vc, parent, parent.globalRotation(), 0.5, 0.5, Faust.Align.REND_ATTR));
					}


				}
			}
			
			if (vc != null) {

				// annotate the vc with the original element name
		 		vc.elementName = node.name ? node.name().localName : "";
				
				if (parent != null ) { // && parent !== this) {
					parent.add(vc);
					vc.parent = parent;
				}
				parent = vc;
			}

			if (node instanceof Y.Faust.AnnotationNode) {
				xmlId = node.data()["xml:id"];
				if (xmlId) {
					this.idMap[xmlId] = vc;
					vc.xmlId = xmlId;
				}
			}

			var that = this;
			if (!nodeIsInvisible)
				Y.each(tree.children(), function(c) { that.buildVC(parent, c, text); });
			
			// After all children, TODO move this into appropriate classes
			if (node.name && node.name().localName == "ins" && node.data()["f:orient"] == "left") {
				// Einweisungszeichen
				var annotationStart = node.annotation.target().range.start;
				var annotationEnd = node.annotation.target().range.end;
				
				vc.add (Y.Faust.DocumentLayout.createText("\u2309", annotationStart, annotationEnd, text));
			}

			// space at the beginning of each line, to give empty lines height
			if (node.name && node.name().localName == "line") {
				vc.add (Y.Faust.DocumentLayout.createText("\u00a0", annotationStart, annotationEnd, text));
			}

			return vc;
		},
		
		transcriptVC: function(jsonRepresentation) {
			
			var structuralNames = ['surface',
								   'vspace', 
								   'div',
								   'seg',
								   'hspace',
								   'patch',
								   'figure',
								   'figDesc',
								   'zone',
								   'space',
								   'line',
								   'app',
								   'rdg',
								   'lem',
								   'anchor',
								   'note',
								   'ins',
								   'grBrace',
								   'gap'
								  ]

			var text = Y.Faust.Text.create(jsonRepresentation);

			var tree = new Y.Faust.AdhocTree(text, structuralNames,
											 Y.Faust.XMLNodeUtils.documentOrderSort,
											 Y.Faust.XMLNodeUtils.isDescendant
											);

			var surfaceVC = new Faust.Surface();
			this.buildVC(surfaceVC, tree, text);

			//global for debugging
			debugText = text;

			Y.each(this.postBuildDeferred, function(f) {f.apply(this)});

			return surfaceVC;
		}
	});
	
	Y.mix(Y.namespace("Faust"), {
        DocumentAdhocTree: DocumentAdhocTree,
    });
	
}, '0.0', {
	requires: ["adhoc-tree", "document-model", "text-annotation"]
});


