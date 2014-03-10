/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

YUI.add('transcript-configuration-faust', function (Y) {


	function classesFromHandValue(hand) {
		var tokens = hand.substring(1).split('_');

		var writer = tokens[0];
		var material = tokens[1];
		var script = tokens[2];

		var classes = [];
		if (writer)
			classes.push("hand-" + writer);
		if (material)
			classes.push("material-" + material);
		if (script)
			classes.push("script-" + script);
		return classes;
	}

	// A configuration defines how markup is rendered by providing handler
	// functions in Y.Faust.TranscriptConfiguration.

	Y.mix(Y.namespace("Faust"), {
		TranscriptConfiguration: 	 {
			overlay : "overlay",
			stripWhitespace : ['overw'],
			initialize : function (layoutState) {
				layoutState.idMap = {};
			},
	        names: {
				'anchor': {
					vc: function(node, text, layoutState) {
						//use empty text element as an anchor
						// FIXME make proper phrase/block context differentiation
						if (node.parent.name().localName === "zone")
							return new Y.FaustTranscript.Line([]);
						else
							return new Y.FaustTranscript.Text("", {});
					}
				},

				'choice' : { vc: function(){return new Y.FaustTranscript.InlineViewComponent();}},

				'document': {

					vc: function(node, text, layoutState) {
						return new Y.FaustTranscript.Surface();
					}
				},

				'ex' : { vc: function(){return new Y.FaustTranscript.InlineViewComponent();}},

				'expan' : { vc: function(){return new Y.FaustTranscript.InlineViewComponent();}},

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
										annotationStart, annotationEnd,	text, layoutState);
								} else if (node.data()['quantity']) {
									var representation = '';
									for (var nrChars=0; nrChars < node.data()["quantity"]; nrChars++) {
										//representation += '\u2715'; //capital X
										representation += gapChar; // small X
									}
									return Y.Faust.TranscriptLayout.createText (representation, annotationStart,
										annotationEnd, text, layoutState);
								} else if(node.data()['atLeast']) {
									var representation = gapChar;
									for (var nrChars=2; nrChars < node.data()["atLeast"]; nrChars++) {
										representation += gapUncertainChar;
									}
									return Y.Faust.TranscriptLayout.createText (representation + gapChar, annotationStart,
										annotationEnd, text, layoutState);

								} else {
									throw (Y.FaustTranscript.ENCODING_EXCEPTION_PREFIX + "Please specify either @qunatity or @atLeast");
								}
								break;
							default:
								throw (Y.FaustTranscript.ENCODING_EXCEPTION_PREFIX + "Invalid unit for gap element! Use 'chars'!");
						}
					}
				},

				'grBrace':  {

					vc: function() {}
				},
//					vc: function(node, text, layoutState) {
//						var imgPath = cp + '/static/img/transcript/';
//						var imgFilename = 'grLineDiagonalFalling.svg#img';
//						var float = new Y.FaustTranscript.CoveringImage('grBrace', [], imgPath + imgFilename,
//							100, 100, layoutState.rootVC);
//						layoutState.currentZone.addFloat(float);
//						return null;
//					}
//				},

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
									return new Y.FaustTranscript.InlineGraphic('grLine', imgPath + 'grLineCurlyHorizontal.svg#img', 100, 100, 20 * EM, 2 * EM);
								} else {
									return new Y.FaustTranscript.SpanningVC('grLine',imgPath + 'grLineCurlyHorizontal.svg#img',	100, 100, width, height);
								}
							} else if (node.data()['f:orient'] == 'vert') {
								if (inline) {
									return new Y.FaustTranscript.InlineGraphic('grLine', imgPath + 'grLineCurlyVertical.svg#img', 100, 100, width, height);
								} else {
									return new Y.FaustTranscript.SpanningVC('grLine', imgPath + 'grLineCurlyVertical.svg#img', 100, 100, width, height);
								}
							}
						} else if (node.data()['f:style'] == 'linear') {
							if (node.data()['f:orient'] == 'horiz') {
								if (inline) {
									return new Y.FaustTranscript.InlineGraphic('grLine', imgPath + 'grLineStraightHorizontal.svg#img', 100, 20, 10 * EM, 2 * EM);
								} else {
									return new Y.FaustTranscript.SpanningVC('grLine', imgPath + 'grLineStraightHorizontal.svg#img', 100, 20, null, 2 * EM);
								}
							} else if (node.data()['f:orient'] == 'vert') {
								if (inline) {
									return new Y.FaustTranscript.InlineGraphic('grLine', imgPath + 'grLineStraightVertical.svg#img', 20, 100, 1 * EM, 2 * EM);
								} else {
									return new Y.FaustTranscript.SpanningVC('grLine', imgPath + 'grLineStraightVertical.svg#img', 20, 100, null, 2 * EM);
								}
							}
						} else if (node.data()['f:style'] == 's-left-right') {
							if (inline) {
								throw (Y.FaustTranscript.ENCODING_EXCEPTION_PREFIX + "S-curve can't be inline!");
							} else {
								return new Y.FaustTranscript.SpanningVC('grLine', imgPath + 'grLineSLeftRight.svg#img', 100, 100, null, null);
							}
						}
					}
				},

				'hand' : {

					text: function(annotation, textVC, layoutState) {
						var hand = annotation.data["value"];
						var classes = classesFromHandValue(hand);
						textVC.classes = textVC.classes.concat(classes);
					}
				},

				'hi' : {
					vc: function(node, text, layoutState) {

						var handClasses = [];
						if (node.data["hand"]) {
							var hand = node.data["hand"];
							handClasses = classesFromHandValue(hand);
						}

						if (node.data()["rend"]) {
							var rendTokens = node.data()["rend"].split(' ');
							if (rendTokens.indexOf("frame") >= 0) {
								return new Y.FaustTranscript.RectInlineDecoration(handClasses);
							} else if (rendTokens.indexOf("circle") >= 0) {
								return new Y.FaustTranscript.CircleInlineDecoration(handClasses);
							}
						}
					},

					text: function (annotation, textVC, layoutState) {
						if (annotation.data["rend"]) {
							var rendTokens = annotation.data["rend"].split(' ');

							var handClasses = [];
							if (annotation.data["hand"]) {
								var hand = annotation.data["hand"];
								handClasses = classesFromHandValue(hand);
							}

							if (rendTokens.indexOf("underline") >= 0) {
								var underline = new Y.FaustTranscript.LineDecoration(textVC, handClasses, 'underline', 0.1);
								textVC.decorations.push(underline);
							}
							if (rendTokens.indexOf("underdots") >= 0) {
								var underdots = new Y.FaustTranscript.LineDecoration(textVC, handClasses, 'underdots', 0.2);
								textVC.decorations.push(underdots);
							}
							if (rendTokens.indexOf("overline") >= 0) {
								var overline = new Y.FaustTranscript.LineDecoration(textVC, handClasses, 'overline', -0.7);
								textVC.decorations.push(overline);
							}

							if (rendTokens.indexOf('sup') >= 0) {
								textVC.classes.push('sup');
							}
							if (rendTokens.indexOf('sub') >= 0) {
								textVC.classes.push('sub');
							}
						}
					}
				},

				'hspace':  {
					vc: function(node, text, layoutState) {
						switch (node.data()["unit"]) {
							case "chars":
								if (node.data()['quantity']) {
									var width = String(node.data()['quantity']);
									return new Y.FaustTranscript.HSpace(width);
								} else throw (Y.FaustTranscript.ENCODING_EXCEPTION_PREFIX + "f:hspace: Please specify @qunatity");
								break;
							default:
								throw (Y.FaustTranscript.ENCODING_EXCEPTION_PREFIX + "Invalid unit for hspace element! Use 'chars'!");
						}
					}
				},

				'ins': {
					vc: function(node, text, layoutState) {
						var annotationStart = node.annotation.target().range.start;
						var annotationEnd = node.annotation.target().range.end;
						var vc = new Y.FaustTranscript.InlineViewComponent();
						// insertion mark
						if (node.data()["f:orient"] === "right") {
							var insertionSign = node.data()['f:at'].substring(1) in layoutState.idMap ? "\u230a" : "\u2308";
							vc.add (Y.Faust.TranscriptLayout.createText(insertionSign, annotationStart, annotationEnd,
								text, layoutState));
						}
						return vc;
					},
					end: function(node, text, layoutState) {
						if (node.data()["f:orient"] == "left") {
							// insertion mark
							var annotationStart = node.annotation.target().range.start;
							var annotationEnd = node.annotation.target().range.end;
							var insertionSign = node.data()['f:at'].substring(1) in layoutState.idMap ? "\u230b" : "\u2309";
							this.add (Y.Faust.TranscriptLayout.createText(insertionSign, annotationStart, annotationEnd,
								text, layoutState));
						}
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

						return new Y.FaustTranscript.Line(lineAttrs);
					},
					text: function(annotation, textVC) {
						var fontsize = ((annotation.data["type"] || "").indexOf("inter") >= 0 ? "interline" : "normal");
						textVC.classes.push(fontsize);
					}
				},

				'over' : {text: function(annotation, textVC){ textVC.classes.push('over'); }},

				'overw' : {vc: function() {return new Y.FaustTranscript.InlineViewComponent();}},

				'patch' : { vc: function(){return new Y.FaustTranscript.Patch();}},

				'point' : {
					vc: function (node, text, layoutState) {

						var grBraceAnnotation = node.ancestors().filter(function(ancestor){
							return ancestor.annotation.name.localName === 'grBrace'}
						)[0].annotation;
						if (typeof layoutState.grBraceVCs === 'undefined') {layoutState.grBraceVCs = {};}
						if (!(grBraceAnnotation.id in layoutState.grBraceVCs)) {
							var imgPath = cp + '/static/img/transcript/';
							var grBraceVC = new Y.FaustTranscript.CoveringImage('grLine',[], imgPath + 'usedMarker.svg#svgroot',
								100, 100, layoutState.rootVC);
							layoutState.grBraceVCs[grBraceAnnotation.id] = grBraceVC;
							layoutState.currentZone.addFloat(grBraceVC);
						}

						var pointVC = new Y.FaustTranscript.Text('O', {});
						layoutState.grBraceVCs[grBraceAnnotation.id].coveredVCs.push(pointVC);
						layoutState.currentZone.addFloat(pointVC);

						return null;
					}
				},

				'rdg' : { vc: function(){return new Y.FaustTranscript.InlineViewComponent();}},

				'rewrite' : {

					text: function (annotation, textVC, layoutState) {
						var rewrite = new Y.FaustTranscript.CloneDecoration(textVC, [], 'rewrite', 0.005, -0.005);
						textVC.decorations.push(rewrite);

					}
				},

				'seg' : {

					vc: function() {return new Y.FaustTranscript.InlineViewComponent();},

					text : function (annotation, textVC, layoutState) {
						if (annotation.data['rend']) {
							var rendTokens = annotation.data['rend'] ? annotation.data['rend'].split(' ') : [];
							if (rendTokens.indexOf('inbetween') >= 0 || rendTokens.indexOf('between') >= 0) {
								textVC.classes.push('inbetween');
							}
						}
					}
				},

				'st' : {
					text : function (annotation, textVC, layoutState) {

						var rendTokens = annotation.data['rend'] ? annotation.data['rend'].split(' ') : [];
						var classes = [];


						if (annotation.data["hand"]) {
							var hand = annotation.data["hand"];
							classes = classes.concat(classesFromHandValue(hand));
						}

						if (rendTokens.indexOf('vertical') >= 0 || rendTokens.indexOf('diagonal') >= 0 ) {
							if (typeof layoutState.stVertVCs === 'undefined') {layoutState.stVertVCs = {};}
							if (!(annotation.id in layoutState.stVertVCs)) {
								var imgPath = cp + '/static/img/transcript/';
								var imgFilename = rendTokens.indexOf('vertical') >= 0 ? 'grLineStraightVertical.svg#img'
									:'grLineDiagonalFalling.svg#img';
								var stVertVC = new Y.FaustTranscript.CoveringImage('grLine', classes, imgPath + imgFilename,
									100, 100, layoutState.rootVC);

								layoutState.stVertVCs[annotation.id] = stVertVC;
								layoutState.currentZone.addFloat(stVertVC);

								var decoration = new Y.FaustTranscript.NullDecoration(textVC, classes, 'strikethrough');
								textVC.decorations.push(decoration);
							}
							layoutState.stVertVCs[annotation.id].coveredVCs.push(textVC);
							textVC.classes.push('st-vertical');
						} else {
							// count the number of strikethroughs
							layoutState.textState.numSt = 'numSt' in layoutState.textState ?
								layoutState.textState.numSt : 0;

							var yOffsetPerStrikethrough = 0.15;
							var yOffset = yOffsetPerStrikethrough * layoutState.textState.numSt;


							if (rendTokens.indexOf('erase') >= 0) {
								textVC.classes.push('erase');
								var decoration = new Y.FaustTranscript.CloneDecoration(textVC, [], 'erase', 0, 0);
								textVC.decorations.push(decoration);

							} else {
								var decoration = new Y.FaustTranscript.LineDecoration(textVC, classes, 'strikethrough', -0.2 - yOffset);
								textVC.decorations.push(decoration);
							}

							layoutState.textState.numSt = layoutState.textState.numSt + 1;
						}

					}
				},

				'supplied' : {
					vc : function (node, text, layoutState) {

						var annotationStart = node.annotation.target().range.start;
						var annotationEnd = node.annotation.target().range.end;
						var vc = new Y.FaustTranscript.InlineViewComponent();
						vc.add (Y.Faust.TranscriptLayout.createText('[', annotationStart, annotationEnd, text,
							layoutState));
						return vc;
					},
					end: function(node, text, layoutState) {
						var annotationStart = node.annotation.target().range.start;
						var annotationEnd = node.annotation.target().range.end;
						this.add (Y.Faust.TranscriptLayout.createText(']', annotationStart, annotationEnd, text,
							layoutState));
					}
				},

				'surface': {
					vc: function(node, text, layoutState) {
						return new Y.FaustTranscript.Surface();
					}
				},

				'treeRoot': {
					vc: function(node, text, layoutState) {
						return new Y.FaustTranscript.Surface();
					}
				},


				'unclear' : {
					vc : function (node, text, layoutState) {

						var annotationStart = node.annotation.target().range.start;
						var annotationEnd = node.annotation.target().range.end;
						var vc = new Y.FaustTranscript.InlineViewComponent();
						var startMarker = node.data()['cert'] == 'low' ? '{{' : '{';
						var certainty = node.data()['cert'] == 'low' ? 'low' : 'high';
						vc.classes.push('unclear-cert-' + certainty);
						vc.add (Y.Faust.TranscriptLayout.createText(startMarker, annotationStart, annotationEnd, text,
							layoutState));
						return vc;
					},
					end: function(node, text, layoutState) {
						var annotationStart = node.annotation.target().range.start;
						var annotationEnd = node.annotation.target().range.end;
						var endMarker = node.data()['cert'] == 'low' ? '}}' : '}';
						this.add (Y.Faust.TranscriptLayout.createText(endMarker, annotationStart, annotationEnd, text,
							layoutState));

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



				'under' : {
					text: function(annotation, textVC, layoutState) {
						textVC.classes.push('under');
					}
				},

				'used' : {
					text: function(annotation, textVC, layoutState) {
						if (typeof layoutState.usedVCs === 'undefined') {layoutState.usedVCs = {};}
						if (!(annotation.id in layoutState.usedVCs)) {
							var imgPath = cp + '/static/img/transcript/';
							var usedVC = new Y.FaustTranscript.CoveringImage('grLine',[], imgPath + 'usedMarker.svg#svgroot',
								100, 100, layoutState.rootVC);
							layoutState.usedVCs[annotation.id] = usedVC;
							layoutState.currentZone.addFloat(usedVC);
						}
						layoutState.usedVCs[annotation.id].coveredVCs.push(textVC);
						textVC.classes.push('used');
					}
				},

				'vspace': {
					vc: function(node, text, layoutState) {

						//TODO real implementation, non-integer values
						switch (node.data()["unit"]) {
							case "lines":
								if (node.data()['quantity']) {
									return new Y.FaustTranscript.VSpace(node.data()['quantity']);
								} else throw (Y.FaustTranscript.ENCODING_EXCEPTION_PREFIX + "f:vspace: Please specify @qunatity");
								break;
							default:
								throw (Y.FaustTranscript.ENCODING_EXCEPTION_PREFIX + "Invalid unit for vspace element! Use 'lines'!");
						}
					}
				},

				'zone':  { 
					vc: function(node, text, layoutState) {
						var vc = new Y.FaustTranscript.Zone();
						if ("rotate" in node.data()) 
							vc.rotation = parseInt(node.data()["rotate"]);
						if ("type" in node.data() && node.data()["type"] == "main") {
							if (layoutState.mainZone)
								throw (Y.FaustTranscript.ENCODING_EXCEPTION_PREFIX + "More than one main zone specified!");
							else {
								layoutState.mainZone = vc;
								// main zone is absolutely anchored
								vc.setAlign('hAlign', new Y.FaustTranscript.AbsoluteAlign(vc, 0, 0, Y.FaustTranscript.Align.MAIN_ZONE));
								vc.setAlign('vAlign', new Y.FaustTranscript.AbsoluteAlign(vc, 0, 0, Y.FaustTranscript.Align.MAIN_ZONE));
							}
						}
						layoutState.currentZone = vc;
						return vc;
					}
				}
			},


		}
	});

}, '0.0', {
	requires: ['transcript', 'transcript-svg']
});


