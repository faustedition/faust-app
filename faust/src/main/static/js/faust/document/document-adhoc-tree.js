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
					textAttrs.hand = a.data["value"].substring(1);
				} else if (a.name.localName == "rewrite" && a.data["hand"]) {
					textAttrs.rewrite = a.data["hand"].substring(1);
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
		}
		
	};

	Y.mix(Y.namespace("Faust"), {
        DocumentLayout: DocumentLayout
    });
	

	var DocumentAdhocTree = function() {
		this.mainZone = null;
		this.idMap = {};
		this.postBuildDeferred = [];
	}
	
	Y.extend(DocumentAdhocTree, Object, {

		buildVC: function(parent, node, text) {
			
			if (node == null) return null;
			var vc = null;



			if ((node instanceof Y.Faust.TextNode) && (parent != null)) { //&& (parent instanceof Faust.Line)) {
				if (Y.Faust.DocumentConfiguration.stripWhitespace.indexOf(node.parent.name().localName) >= 0 && node.content().trimRight() == "") {
					//only whitespace to be stripped, do not return a text representation
				} else {
					vc = Y.Faust.DocumentLayout.createText(node.content(), node.range.start, node.range.end, text);
				}
			} else if (node instanceof Y.Faust.AnnotationNode) {

				var annotationStart = node.annotation.target().range.start;
				var annotationEnd = node.annotation.target().range.end;

				//ioc: configurable modules handle the construction of the view
				if (node.name().localName in Y.Faust.DocumentConfiguration.names) {
					var nameHandler = Y.Faust.DocumentConfiguration.names[node.name().localName];
					if (nameHandler.vc) {
						vc = nameHandler.vc(node, text, this);
					} else {
						vc = new Faust.DefaultVC();
					}
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

			Y.each(node.children(), function(c) { that.buildVC(parent, c, text); });
			
			if (node instanceof Y.Faust.AnnotationNode) {

				// space at the beginning of each line, to give empty lines height
				if (node.name().localName == "line") {
					vc.add (Y.Faust.DocumentLayout.createText("\u00a0", annotationStart, annotationEnd, text));
				}

				// 'end' callback after all children are constructed
				// with the vc for 'this'
				
				if (node.name().localName in Y.Faust.DocumentConfiguration.names) {
					var nameHandler = Y.Faust.DocumentConfiguration.names[node.name().localName];
					if (nameHandler.end) {
						nameHandler.end.call(vc, node, text, this);
					}
				}
			}
			return vc;
		},
		
		transcriptVC: function(jsonRepresentation) {
			
			var structuralNames = Object.keys(Y.Faust.DocumentConfiguration.names);

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
        DocumentAdhocTree: DocumentAdhocTree
    });
	
}, '0.0', {
	requires: ["adhoc-tree", "document-model", "text-annotation"]
});


