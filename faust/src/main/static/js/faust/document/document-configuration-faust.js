YUI.add('document-configuration-faust', function (Y) {

	// A configuration defines how markup is rendered by providing handler
	// functions in Y.Faust.DocumentConfiguration.

	console.log('document-configuration-faust');
	Y.mix(Y.namespace("Faust"), {
        DocumentConfiguration: 	 {
			names: {
				
				'document': function(node, text, layoutState) {
					return new Faust.Surface();
				},

				'treeRoot': function(node, text, layoutState) {
					return new Faust.Surface();
				},		
				
				'surface': function(node, text, layoutState) {
					return new Faust.Surface();
				},
				
				'zone': function(node, text, layoutState) {
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
				},

				'line': function(node, text, layoutState) {

					var lineAttrs = {};
					var rendition = node.data()["rend"] || "";
					if (rendition.indexOf("centered") >= 0) {
						lineAttrs.center = true;
					} else if (rendition.indexOf("indent") >= 0) {
						var start = rendition.indexOf("indent-");
						lineAttrs.indent = parseInt(rendition.substring(start + 7, rendition.length)) / 100.0;
					}

					var  position = node.data()["f:pos"] || "";

					if (position.indexOf("over") >= 0)
						lineAttrs.over = true;

					if (position.indexOf("between") >= 0)
						lineAttrs.between = true;

					return new Faust.Line(lineAttrs);

				},

				'vspace': function(node, text, layoutState) {

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
					
				},
				
				'gap': function(node, text, layoutState) {

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
			}
		}
	});

}, '0.0', {
	requires: ['document-model', 'document-view-svg']
});


