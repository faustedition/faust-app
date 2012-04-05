SVG_NS = "http://www.w3.org/2000/svg";

Faust.YUI().use("node", "dom", "dom-screen", "event", "overlay", "scrollview", "dump", "async-queue", "resize", function(Y) {

	//FIXME: cleanup: put this in view or model, etc.
	Faust.LayoutPreferences = {
			
			overlay : "overlay",
			
	};
	
	Faust.DocumentController = {
			
			idMap: {},
			
			postBuildDeferred : [],
			
			buildVC: function(parent, tree) {
				
				if (tree == null) return null;			
				var vc = null;
				var node = tree.node;
				var nodeIsInvisible = false;
				
				// Text factory; the current model only delivers text nodes, some additional elements (gaps, insertion marks) need 
				// to be delivered to know their tree context (hands...) for visualisation
				var createText = function(content, node){
					var textAttrs = {};
					Y.each(node.ancestors(), function(a) {
						var elem = a.node;
						if (elem.name == "f:hand") {
							textAttrs.hand = elem.attrs["f:id"];
						} else if (elem.name == "ge:rewrite") {
							textAttrs.rewrite = elem.attrs["ge:hand"];
						} else if (elem.name == "f:under") {
							textAttrs.under = true;
						} else if (elem.name == "f:over") {
							textAttrs.over = true;
						} else if (elem.name == "f:st") {
							textAttrs.strikethrough = true;
						} else if (elem.name == "tei:hi" && elem.attrs["tei:rend"].indexOf("underline") >= 0) {
							textAttrs.underline = true;
						} else if (elem.name == "ge:line") {
							textAttrs.fontsize = ((elem.attrs["ge:type"] || "").indexOf("inter") >= 0 ? "small" : "normal");
						}
					});				
					return new Faust.Text(content, textAttrs);				
				};

				if ((node instanceof Goddag.Text) && (parent != null)) { //&& (parent instanceof Faust.Line)) {
					vc = createText(node.content, node);
				} else if (node instanceof Goddag.Element) {
					if (node.name == "ge:document") {
						vc = new Faust.Surface();
					} else if (node.name == "tei:surface") {
						vc = new Faust.Surface();
					} else if (node.name == "tei:zone")	{
						vc = new Faust.Zone();
						if ("tei:rotate" in node.attrs) 
							vc.rotation = parseInt(node.attrs["tei:rotate"]);
						if ("tei:type" in node.attrs && node.attrs["tei:type"] == "main") {
							if (Faust.DocumentController.mainZone != null)
								throw (Faust.ENC_EXC_PREF + "More than one main zone specified!");
							else
								Faust.DocumentController.mainZone = vc;
						} 
							

					} else if (node.name == "ge:line") {
						var lineAttrs = {};
						var rendition = node.attrs["ge:rend"] || "";
						if (rendition.indexOf("center") >= 0) {
							lineAttrs.center = true;
						} else if (rendition.indexOf("indent") >= 0) {
							var start = rendition.indexOf("indent-");
							lineAttrs.indent = parseInt(rendition.substring(start + 7, rendition.length)) / 100.0;
						}

						var  position = node.attrs["f:pos"] || "";
						if (position.indexOf("over") >= 0)
							lineAttrs.over = true;

						var  position = node.attrs["f:pos"] || "";
						if (position.indexOf("between") >= 0)
							lineAttrs.between = true;

						vc = new Faust.Line(lineAttrs);
					} else if (node.name == "f:vspace") {
						//TODO real implementation, non-integer values

						switch (node.attrs["f:unit"]) {
						case "lines":
							if (node.attrs['f:quantity']) {
								vc = new Faust.VSpace(node.attrs['f:quantity']);
							} else throw (Faust.ENC_EXC_PREF + "f:vspace: Please specify @qunatity");
							break;
						default: 
							throw (Faust.ENC_EXC_PREF + "Invalid unit for vspace element! Use 'lines'!");
						}
						
					} else if (node.name == "tei:gap") {
						var gapChar = '\u00d7';
						var gapUncertainChar = '\u00d7'; //'.';
						switch (node.attrs["tei:unit"]) {
						case "chars":
							if (node.attrs['tei:quantity'] && node.attrs['tei:precision'] && 
									node.attrs['tei:precision'] === 'medium') {
								var representation = gapChar;
								for (var nrChars=2; nrChars < node.attrs["tei:quantity"]; nrChars++) {
									representation += gapUncertainChar;  
								}
								vc = createText (representation + gapChar, node);															
							} else if (node.attrs['tei:quantity']) {
								var representation = '';
								for (var nrChars=0; nrChars < node.attrs["tei:quantity"]; nrChars++) {
									//representation += '\u2715'; //capital X
									representation += gapChar; // small X
								}
								vc = createText (representation, node);							
							} else if(node.attrs['tei:atLeast']) {
								var representation = gapChar;
								for (var nrChars=2; nrChars < node.attrs["tei:atLeast"]; nrChars++) {
									representation += gapUncertainChar;  
								}
								vc = createText (representation + gapChar, node);							

							} else {
								throw (Faust.ENC_EXC_PREF + "Please specify either @qunatity or @atLeast");
							}
							break;
						default: 
							throw (Faust.ENC_EXC_PREF + "Invalid unit for gap element! Use 'chars'!");
						}
					} else if (node.name == "f:hspace") {
						switch (node.attrs["f:unit"]) {
						case "chars":
							if (node.attrs['f:quantity']) {
								var width = String(node.attrs['f:quantity']);
								vc = new Faust.HSpace(width);
							} else throw (Faust.ENC_EXC_PREF + "f:hspace: Please specify @qunatity");
							break;
						default: 
							throw (Faust.ENC_EXC_PREF + "Invalid unit for hspace element! Use 'chars'!");
						}
					}  else if (node.name == "f:grLine") {
						if (node.attrs['f:style'] == 'curly')
							vc = new Faust.GrLine();
					} else if (node.name == "f:grBrace") {
						//vc = new Faust.GBrace();
					} else if (node.name == "tei:anchor") {
						//use empty text element as an anchor
						// FIXME make proper phrase/block context differentiation
						if (parent.elementName === "tei:zone")
							vc = new Faust.Line([]);
						else
							vc = createText("", node);
						
					
					} else if (node.name == "f:ins" && node.attrs["f:orient"] == "right") {
						vc = new Faust.DefaultVC();
						//Einweisungszeichen
						vc.add (createText("\u2308", node));
					// Default Elements
					} else if (node.name in {}) {
						vc = new Faust.DefaultVC();
					//Invisible elements				
					} else if (node.name in {"tei:rdg":1}){
						nodeIsInvisible = true;
					}
					aligningAttributes = ["f:at", "f:left", "f:left-right", "f:right", "f:right-left", "f:top", "f:top-bottom", "f:bottom", "f:bottom-top"];
					
					var idMap = Faust.DocumentController.idMap;
					var postBuildDeferred = Faust.DocumentController.postBuildDeferred;
					Y.each(aligningAttributes, function(a){
						if (a in node.attrs) {
							if (!vc) {
								vc = new Faust.DefaultVC();
							}
							//FIXME id hash hack; do real resolution of references
							var anchorId = node.attrs[a].substring(1);
							var coordRot = a in {"f:at":1, "f:left":1, "f:left-right":1, "f:right":1, "f:right-left":1}? 0 : 90;
							var alignName = coordRot == 0 ? "hAlign" : "vAlign";
							var myJoint = a in {"f:left":1, "f:left-right":1, "f:top":1, "f:top-bottom":1}? 0 : 1;
							var yourJoint = a in {"f:at":1, "f:left":1, "f:right-left":1, "f:top":1, "f:bottom-top":1}? 0 : 1;
							
							if ("f:orient" in node.attrs)
								myJoint = node.attrs["f:orient"] == "left" ? 1 : 0;
							
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
					
					// TODO redundant with line properties
					// TODO special treatment of zones
					if ("ge:rend" in node.attrs) {
						if (node.attrs["ge:rend"] == "right") {
				 			vc.setAlign("hAlign", new Faust.Align(vc, parent, parent.globalRotation(), 1, 1, Faust.Align.REND_ATTR));
						} else if (node.attrs["ge:rend"] == "left") {
				 			vc.setAlign("hAlign", new Faust.Align(vc, parent, parent.globalRotation(), 0, 0, Faust.Align.REND_ATTR));
						} else if (node.attrs["ge:rend"] == "center") {
				 			vc.setAlign("hAlign", new Faust.Align(vc, parent, parent.globalRotation(), 0.5, 0.5, Faust.Align.REND_ATTR));
						}


					}
				}
				
				if (vc != null) {

					// annotate the vc with the original element name
		 			vc.elementName = node.name;
									
					if (parent != null ) { // && parent !== this) {
						parent.add(vc);
						vc.parent = parent;
					}
					parent = vc;
				}

					if (node instanceof Goddag.Element) {
						xmlId = node.attrs["xml:id"];
						if (xmlId) {
							Faust.DocumentController.idMap[xmlId] = vc;
							vc.xmlId = xmlId;
						}
				}

					if (!nodeIsInvisible)
						Y.each(tree.children, function(c) { Faust.DocumentController.buildVC(parent, c); });
					
					// After all children, TODO move this into appropriate classes
					if (node.name == "f:ins" && node.attrs["f:orient"] == "left") {
						// Einweisungszeichen
						vc.add (createText("\u2309", node));
					}

					return vc;
			}
	};
	
	
	
	


});