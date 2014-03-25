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

YUI.add('transcript-adhoc-tree', function (Y) {

	var TranscriptLayout = {
		// Text factory; the current model only delivers text nodes, some additional elements (gaps, insertion marks) need 
		// to be delivered to know their tree context (hands...) for visualisation
		createText : function(content, start, end, text, layoutState){
			if (content.length < 1) throw "Cannot create empty text!";
			var textAttrs = {};
			var annotations = text.find(start, end)
			//ignore empty annotations at the borders
				.filter(function(x){var r = x.target().range; return r.start !== r.end});

			var textVC = new Y.FaustTranscript.Text(content, textAttrs);

			// reset custom state of current text representation
			if (typeof layoutState !== 'undefined')
				layoutState.textState = {};
			Y.each(annotations, function(annotation) {
				if (annotation.name.localName in Y.Faust.TranscriptConfiguration.names
					&& Y.Faust.TranscriptConfiguration.names[annotation.name.localName].text) {
					Y.Faust.TranscriptConfiguration.names[annotation.name.localName].text(annotation, textVC, layoutState);
				}
			});

			return textVC;
		}
		
	};

	Y.mix(Y.namespace("Faust"), {
        TranscriptLayout: TranscriptLayout
    });
	

	var TranscriptAdhocTree = function() {
		this.mainZone = null;
		this.idMap = {};
		this.postBuildDeferred = [];
	};
	
	Y.extend(TranscriptAdhocTree, Object, {

		buildVC: function(parent, node, text, layoutState) {

			if (node == null) return null;
			var vc = null;


			var ancestorNames = node.ancestors().map(function(node){return node.annotation.name.localName});
			if ((node instanceof Y.Faust.TextNode) && (parent != null) && (ancestorNames.indexOf('line') >= 0)) {
				if (Y.Faust.TranscriptConfiguration.stripWhitespace.indexOf(node.parent.name().localName) >= 0 && node.content().trimRight() == "") {
					//only whitespace to be stripped, do not return a text representation
				} else {
					vc = Y.Faust.TranscriptLayout.createText(node.content(), node.range.start, node.range.end, text, layoutState);
				}
			} else if (node instanceof Y.Faust.AnnotationNode) {

				var annotationStart = node.annotation.target().range.start;
				var annotationEnd = node.annotation.target().range.end;

				//ioc: configurable modules handle the construction of the view

				// 'start' callback before the construction of any view components

				if (node.name().localName in Y.Faust.TranscriptConfiguration.names) {
					var nameHandler = Y.Faust.TranscriptConfiguration.names[node.name().localName];
					if (nameHandler.start) {
						nameHandler.start.call(node, text, layoutState);
					}
				}

				if (node.name().localName in Y.Faust.TranscriptConfiguration.names
					&& 'vc' in Y.Faust.TranscriptConfiguration.names[node.name().localName]) {
					var nameHandler = Y.Faust.TranscriptConfiguration.names[node.name().localName];
					if (nameHandler.vc) {
						vc = nameHandler.vc(node, text, layoutState);
					} else {
						vc = new Y.FaustTranscript.InlineViewComponent();
					}
				}

				var aligningAttributes = ["f:at", "f:left", "f:left-right", "f:right", "f:right-left", "f:top", "f:top-bottom", "f:bottom", "f:bottom-top"];
				
				var that = this;

				Y.each(aligningAttributes, function(a){
					if (a in node.data()) {
						if (!vc) {
							vc = new Y.FaustTranscript.InlineViewComponent();
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
									throw (Y.FaustTranscript.ENCODING_EXCEPTION_PREFIX + "Reference to #" + anchorId + " cannot be resolved!");
								var globalCoordRot = coordRot + anchor.globalRotation();
								vc.setAlign(alignName, new Y.FaustTranscript.Align(vc, anchor, globalCoordRot, myJoint, yourJoint, Y.FaustTranscript.Align.EXPLICIT));
							});
					}						
				});
				
				// TODO special treatment of zones
				if ("rend" in node.data()) {
					if (node.data()["rend"] == "right") {
				 		vc.setAlign("hAlign", new Y.FaustTranscript.Align(vc, parent, parent.globalRotation(), 1, 1, Y.FaustTranscript.Align.REND_ATTR));
					} else if (node.data()["rend"] == "left") {
				 		vc.setAlign("hAlign", new Y.FaustTranscript.Align(vc, parent, parent.globalRotation(), 0, 0, Y.FaustTranscript.Align.REND_ATTR));
					} else if (node.data()["rend"] == "centered") {
				 		vc.setAlign("hAlign", new Y.FaustTranscript.Align(vc, parent, parent.globalRotation(), 0.5, 0.5, Y.FaustTranscript.Align.REND_ATTR));
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
				var xmlId = node.data()["xml:id"];
				if (xmlId) {
					this.idMap[xmlId] = vc;
					vc.xmlId = xmlId;
				}
			}

			var that = this;

			Y.each(node.children(), function(c) { that.buildVC(parent, c, text, layoutState); });
			
			if (node instanceof Y.Faust.AnnotationNode) {

				// space at the beginning of each line, to give empty lines height
				if (node.name().localName == "line") {
					var emptyProp = new Y.FaustTranscript.Text("\u00a0", {noBackground: true});
					emptyProp.classes.push('noBackground');
					vc.add (emptyProp);
				}

				// 'end' callback after all children are constructed
				// with the vc for 'this'
				
				if (node.name().localName in Y.Faust.TranscriptConfiguration.names) {
					var nameHandler = Y.Faust.TranscriptConfiguration.names[node.name().localName];
					if (nameHandler.end) {
						nameHandler.end.call(vc, node, text, layoutState);
					}
				}
			}
			return vc;
		},
		
		transcriptVC: function(jsonRepresentation) {
			
			var structuralNames = Object.keys(Y.Faust.TranscriptConfiguration.names)
				.filter(function(name){return 'vc' in Y.Faust.TranscriptConfiguration.names[name]});

			var text = Y.Faust.Text.create(jsonRepresentation);

			var tree = new Y.Faust.AdhocTree(text, structuralNames,
											 Y.Faust.XMLNodeUtils.documentOrderSort,
											 Y.Faust.XMLNodeUtils.isDescendant
											);

			var surfaceVC = new Y.FaustTranscript.Surface();


			var layoutState = {
				rootVC : surfaceVC
			};

			Y.Faust.TranscriptConfiguration.initialize(layoutState);
			this.buildVC(surfaceVC, tree, text, layoutState);

			Y.each(this.postBuildDeferred, function(f) {f.apply(this)});

			return surfaceVC;
		}
	});
	
	Y.mix(Y.namespace("FaustTranscript"), {
        TranscriptAdhocTree: TranscriptAdhocTree
    });
	
}, '0.0', {
	requires: ["adhoc-tree", "transcript-svg", "text-annotation"]
});


