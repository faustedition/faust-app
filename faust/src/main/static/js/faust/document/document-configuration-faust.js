YUI.add('document-configuration-faust', function (Y) {

	// A configuration defines how markup is rendered by providing handler
	// functions in Y.Faust.DocumentConfiguration.

	Y.mix(Y.namespace("Faust"), {
        DocumentConfiguration: 	 {
			names: {
				
				'document': { 
					vc: function(node, text, layoutState) {
						return new Faust.Surface();
					}
				},

				'treeRoot': { 
					vc: function(node, text, layoutState) {
						return new Faust.Surface();
					}
				},		
				
				'surface': { 
					vc: function(node, text, layoutState) {
						return new Faust.Surface();
					}
				},
				
				'zone':  { 
					vc: function(node, text, layoutState) {
						var vc = new Faust.Zone();
						if ("rotate" in node.data()) 
							vc.rotation = parseInt(node.data()["rotate"]);
						if ("type" in node.data() && node.data()["type"] == "main") {
							if (layoutState.mainZone)
								throw (Faust.ENC_EXC_PREF + "More than one main zone specified!");
							else {
								layoutState.mainZone = vc;
								// main zone is absolutely anchored
								vc.setAlign('hAlign', new Faust.AbsoluteAlign(vc, 0, 0, Faust.Align.MAIN_ZONE));
								vc.setAlign('vAlign', new Faust.AbsoluteAlign(vc, 0, 0, Faust.Align.MAIN_ZONE));
							}
						} 
						return vc;
					}
				},

				'line': { 
					vc: function(node, text, layoutState) {

						var lineAttrs = {};
						var rendition = node.data()["rend"] || "";
						if (rendition.indexOf("centered") >= 0) {
							lineAttrs.center = true;
						} 
						else if (rendition.indexOf("indent-center") >=0) {
							var start = rendition.indexOf("indent-center-");
							lineAttrs.indentCenter = parseInt(rendition.substring(start + 14, rendition.length)) / 100.0;
						}
						else if (rendition.indexOf("indent") >= 0) {
							var start = rendition.indexOf("indent-");
							lineAttrs.indent = parseInt(rendition.substring(start + 7, rendition.length)) / 100.0;
						}

						var  position = node.data()["f:pos"] || "";

						if (position.indexOf("over") >= 0)
							lineAttrs.over = true;

						if (position.indexOf("between") >= 0)
							lineAttrs.between = true;

						return new Faust.Line(lineAttrs);
					}
				},

				'vspace': { 
					vc: function(node, text, layoutState) {

						//TODO real implementation, non-integer values
						switch (node.data()["unit"]) {
						case "lines":
							if (node.data()['quantity']) {
								return new Faust.VSpace(node.data()['quantity']);
							} else throw (Faust.ENC_EXC_PREF + "f:vspace: Please specify @qunatity");
							break;
						default: 
							throw (Faust.ENC_EXC_PREF + "Invalid unit for vspace element! Use 'lines'!");
						}
					}
				},
				
				'gap': { 
					vc:  function(node, text, layoutState) {

						var annotationStart = node.annotation.target().range.start;
						var annotationEnd = node.annotation.target().range.end;

						var gapChar = '\u00d7';
						var gapUncertainChar = '\u00d7';
						switch (node.data()["unit"]) {
						case "chars":
							if (node.data()['quantity'] && node.data()['precision'] && 
								node.data()['precision'] === 'medium') {
								var representation = gapChar;
								for (var nrChars=2; nrChars < node.data()["quantity"]; nrChars++) {
									representation += gapUncertainChar;  
								}
								return Y.Faust.DocumentLayout.createText (representation + gapChar, 
																		  annotationStart,
																		  annotationEnd, 
																		  text);
							} else if (node.data()['quantity']) {
								var representation = '';
								for (var nrChars=0; nrChars < node.data()["quantity"]; nrChars++) {
									//representation += '\u2715'; //capital X
									representation += gapChar; // small X
								}
								return Y.Faust.DocumentLayout.createText (representation, annotationStart, annotationEnd, text);
							} else if(node.data()['atLeast']) {
								var representation = gapChar;
								for (var nrChars=2; nrChars < node.data()["atLeast"]; nrChars++) {
									representation += gapUncertainChar;  
								}
								return Y.Faust.DocumentLayout.createText (representation + gapChar, annotationStart, 
																		  annotationEnd, text);

							} else {
								throw (Faust.ENC_EXC_PREF + "Please specify either @qunatity or @atLeast");
							}
							break;
						default: 
							throw (Faust.ENC_EXC_PREF + "Invalid unit for gap element! Use 'chars'!");
						}
					}
				},

				'hspace':  { 
					vc: function(node, text, layoutState) {
						switch (node.data()["unit"]) {
						case "chars":
							if (node.data()['quantity']) {
								var width = String(node.data()['quantity']);
								return new Faust.HSpace(width);
							} else throw (Faust.ENC_EXC_PREF + "f:hspace: Please specify @qunatity");
							break;
						default: 
							throw (Faust.ENC_EXC_PREF + "Invalid unit for hspace element! Use 'chars'!");
						}
					}
				},

				'grLine':  { 
					vc: function(node, text, layoutState) {
						if (node.data()['f:style'] == 'curly') {
							if (node.data()['f:orient'] == 'horiz') {
								return new Faust.SpanningVC('grLine',
									cp + '/static/img/transcript/grLineCurly.svg#img',
									100, 100, 100, null);
							} else if (node.data()['f:orient' == 'vert']) {

							}
						} else if (node.data()['f:style'] == 'linear') {
							if (node.data()['f:orient'] == 'horiz') {
								return new Faust.SpanningVC('grLine',
									cp + '/static/img/transcript/grLineStraightHorizontal.svg#img',
									100, 20, null, 20);
							}
						}
					}
				},
				'grBrace':  { 
					vc: function(node, text, layoutState) {
						//return new Faust.GBrace();
					}
				},
				'anchor': { 
					vc: function(node, text, layoutState) {
						//use empty text element as an anchor
						// FIXME make proper phrase/block context differentiation
						if (node.parent.name().localName === "zone")
							return new Faust.Line([]);
						else
							return new Faust.Text("", {});
					}
				},
				'rdg':  { 
					vc: function(node, text, layoutState) {
						// TODO make invisible
						return new Faust.Text("", {});					
					}
				},
				'ins': {
					vc: function(node, text, layoutState) {
						var annotationStart = node.annotation.target().range.start;
						var annotationEnd = node.annotation.target().range.end;
						var vc = new Faust.DefaultVC();						
						//Einweisungszeichen
						if (node.data()["f:orient"] === "right") {
							vc.add (Y.Faust.DocumentLayout.createText("\u2308", annotationStart, annotationEnd, text));
						}
						return vc;
					},
					end: function(node, text, layoutState) {
						if (node.data()["f:orient"] == "left") {
							// Einweisungszeichen
							var annotationStart = node.annotation.target().range.start;
							var annotationEnd = node.annotation.target().range.end;
							this.add (Y.Faust.DocumentLayout.createText("\u2309", annotationStart, annotationEnd, text));
						}
					}
				}
			}
		}
	});

}, '0.0', {
	requires: ['document-model', 'document-view-svg']
});


