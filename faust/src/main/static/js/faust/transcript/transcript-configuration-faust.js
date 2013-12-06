YUI.add('transcript-configuration-faust', function (Y) {

	// A configuration defines how markup is rendered by providing handler
	// functions in Y.Faust.TranscriptConfiguration.

	Y.mix(Y.namespace("Faust"), {
		TranscriptConfiguration: 	 {
			overlay : "overlay",
			stripWhitespace : ['overw'],
	        names: {
		        'choice' : { vc: function(){return new Faust.DefaultVC();}},
		        'unclear' : {
					vc : function (node, text, layoutState) {

						var annotationStart = node.annotation.target().range.start;
						var annotationEnd = node.annotation.target().range.end;
						var vc = new Faust.DefaultVC();
						var startMarker = node.data()['cert'] == 'low' ? '{{' : '{';
						vc.add (Y.Faust.TranscriptLayout.createText(startMarker, annotationStart, annotationEnd, text));
						return vc;
					},
					end: function(node, text, layoutState) {
						var annotationStart = node.annotation.target().range.start;
						var annotationEnd = node.annotation.target().range.end;
						var endMarker = node.data()['cert'] == 'low' ? '}}' : '}';
						this.add (Y.Faust.TranscriptLayout.createText(endMarker, annotationStart, annotationEnd, text));

						// hide the component if it is a less probable alternative of a choice
						if (node.parent.name().localName == 'choice') {
							var sibling_cert_values = node.parent.children().map(
								function(annotation){return annotation.data()['cert']});
							if (node.data()['cert'] == 'low' && sibling_cert_values.indexOf('high') >= 0) {
								this.computeClasses = function(){
									return ['invisible'];
								};
							}
						}
					}
				},
				
				'document': { 
					vc: function(node, text, layoutState) {
						return new Faust.Surface();
					}
				},

				overw : {
					vc: function() {return new Faust.DefaultVC();}
				},

				under : {
					vc: function() {
						var vc =  new Faust.DefaultVC();
						vc.defaultAligns = function () {

							this.setAlign("vAlign", new Faust.Align(this, this.parent, this.rotY(), 1, 1, Faust.Align.IMPLICIT_BY_DOC_ORDER));

							if (this.previous())
								this.setAlign("hAlign", new Faust.Align(this, this.previous(), this.rotX(), 0, 1, Faust.Align.IMPLICIT_BY_DOC_ORDER));
							else
								this.setAlign("hAlign", new Faust.Align(this, this.parent, this.rotX(), 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));
						};
						return vc;
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
						var gapUncertainChar = '.';
						switch (node.data()["unit"]) {
						case "chars":
							if (node.data()['quantity'] && node.data()['precision'] && 
								node.data()['precision'] === 'medium') {
								var representation = gapChar;
								for (var nrChars=2; nrChars < node.data()["quantity"]; nrChars++) {
									representation += gapUncertainChar;  
								}
								return Y.Faust.TranscriptLayout.createText (representation + gapChar,
																		  annotationStart,
																		  annotationEnd, 
																		  text);
							} else if (node.data()['quantity']) {
								var representation = '';
								for (var nrChars=0; nrChars < node.data()["quantity"]; nrChars++) {
									//representation += '\u2715'; //capital X
									representation += gapChar; // small X
								}
								return Y.Faust.TranscriptLayout.createText (representation, annotationStart, annotationEnd, text);
							} else if(node.data()['atLeast']) {
								var representation = gapChar;
								for (var nrChars=2; nrChars < node.data()["atLeast"]; nrChars++) {
									representation += gapUncertainChar;  
								}
								return Y.Faust.TranscriptLayout.createText (representation + gapChar, annotationStart,
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
						var ancestorNames = node.ancestors().map(function(node){return node.annotation.name.localName});
						var inline = ancestorNames.indexOf('line') >= 0;
						// TODO figure outh the number of pixels per em dynamically
						var EM = 10;
						var width = node.data()['width'] ? Number(node.data()['width']) * EM: null;
						var height = node.data()['height'] ? Number(node.data()['height']) * EM : null;
						var imgPath = cp + '/static/img/transcript/';
						if (node.data()['f:style'] == 'curly') {
							if (node.data()['f:orient'] == 'horiz') {
								if (inline) {
									return new Faust.InlineGraphic('grLine', imgPath + 'grLineCurlyHorizontal.svg#img', 100, 100, 20 * EM, 2 * EM);
								} else {
									return new Faust.SpanningVC('grLine',imgPath + 'grLineCurlyHorizontal.svg#img',	100, 100, width, height);
								}
							} else if (node.data()['f:orient'] == 'vert') {
								if (inline) {
									return new Faust.InlineGraphic('grLine', imgPath + 'grLineCurlyVertical.svg#img', 100, 100, width, height);
								} else {
									return new Faust.SpanningVC('grLine', imgPath + 'grLineCurlyVertical.svg#img', 100, 100, width, height);
								}
							}
						} else if (node.data()['f:style'] == 'linear') {
							if (node.data()['f:orient'] == 'horiz') {
								if (inline) {
									return new Faust.InlineGraphic('grLine', imgPath + 'grLineStraightHorizontal.svg#img', 100, 20, 10 * EM, 2 * EM);
								} else {
									return new Faust.SpanningVC('grLine', imgPath + 'grLineStraightHorizontal.svg#img', 100, 20, null, 2 * EM);
								}
							} else if (node.data()['f:orient'] == 'vert') {
								if (inline) {
									return new Faust.InlineGraphic('grLine', imgPath + 'grLineStraightVertical.svg#img', 20, 100, 1 * EM, 2 * EM);
								} else {
									return new Faust.SpanningVC('grLine', imgPath + 'grLineStraightVertical.svg#img', 20, 100, null, 2 * EM);
								}
							}
						} else if (node.data()['f:style'] == 's-left-right') {
							if (inline) {
								throw (Faust.ENC_EXC_PREF + "S-curve can't be inline!");
							} else {
								return new Faust.SpanningVC('grLine', imgPath + 'grLineSLeftRight.svg#img', 100, 100, null, null);
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
						// insertion mark
						if (node.data()["f:orient"] === "right") {
							vc.add (Y.Faust.TranscriptLayout.createText("\u2308", annotationStart, annotationEnd, text));
						}
						return vc;
					},
					end: function(node, text, layoutState) {
						if (node.data()["f:orient"] == "left") {
							// insertion mark
							var annotationStart = node.annotation.target().range.start;
							var annotationEnd = node.annotation.target().range.end;
							this.add (Y.Faust.TranscriptLayout.createText("\u2309", annotationStart, annotationEnd, text));
						}
					}
				}

			}
		}
	});

}, '0.0', {
	requires: ['transcript', 'transcript-svg']
});


