Faust.LayoutPreferences = {
	
	overlay : "overlay",
	
};


YUI.add('document-yui-view', function (Y) {


	var DiplomaticTranscriptView = Y.Base.create("diplomatic-transcript-view", Y.View, [], {


		
		destructor: function() {
			
		},
		
		_appendPatternDefs: function(svgRoot) {
			var defs = document.createElementNS(SVG_NS, 'defs');
			svgRoot.appendChild(defs);
			var grLinePattern = document.createElementNS(SVG_NS, 'pattern');
			grLinePattern.setAttribute('id', 'curlyLinePattern');
			grLinePattern.setAttribute('x', '0');
			grLinePattern.setAttribute('y', '0');
			grLinePattern.setAttribute('width', '100');
			grLinePattern.setAttribute('height', '80');
			grLinePattern.setAttribute('patternUnits', 'userSpaceOnUse');
			defs.appendChild(grLinePattern);
			var grLinePath = document.createElementNS(SVG_NS, 'path');
			grLinePath.setAttribute('d', 'M50,0 a40,20 0 0,1 0,40 a40,20, 0 0,0 0,40');
			grLinePath.setAttribute('fill', 'none');
			grLinePath.setAttribute('stroke', 'black');
			grLinePattern.appendChild(grLinePath);
		},

		displayError: function(error) {
			var msg = Y.Node.create('<p/>');
			msg.append(error.toString());
			//var errorDisplay = Y.one('#error-display');
			this.errorDisplay.append(msg);
			this.errorDisplay.show();
			
		},

		intoView: function (innerContainer, svgCont) {
			var rootBBox = innerContainer.getBBox();
			innerContainer.setAttribute("transform", "translate(" + (- rootBBox.x) + "," + (- rootBBox.y) + ")");
			svgCont.setAttribute("width", rootBBox.width);
			svgCont.setAttribute("height", rootBBox.height);
		},

		center: function(svgRoot, container){
			var cw = Y.one(container).getComputedStyle('width');
			var rw = Y.one(svgRoot).getComputedStyle('width');
			Y.one(svgRoot).setStyles({
				'left': (parseInt(cw) - parseInt(rw)) / 2,
				'position': 'relative'
			});
			
		},

		relayout: function(visComponent, svgRoot, innerContainer, container) {

			that = this;
			aq = new Y.AsyncQueue();
			var layoutAndCenter = function() {
				that.intoView(innerContainer, svgRoot);
				that.center(svgRoot, container);
				visComponent.layout();
			};

			aq.add(
				{
					fn : layoutAndCenter, //visComponent.layout,
					timeout: 10,
					iterations: 5,
					context: visComponent
				},
				{
					fn : function() {
						that.intoView(innerContainer, svgRoot);
						that.center(svgRoot, container);
					},
					timeout: 10,
					iterations: 1,
					context: visComponent
				},
				{
					fn: function() {
						Y.fire('faust:transcript-layout-done', {});						
					},
					timeout: 10,
					iterations: 1
				}
			);
			aq.run();
		},
		

		render: function() {
			var container = this.get('container');
						
			var transcriptSource = cp + '/' + this.get('source').components[1] + '/' + this.get('source').components[2];
			this.editortoolbar = Y.Node.create('<div id="editor-toolbar"><a href="' + transcriptSource  + '">(XML source)</a></div>');
			container.appendChild(this.editortoolbar);

			this.errorDisplay = Y.Node.create('<div id="error-display"></div>');
			container.appendChild(this.errorDisplay);
			var svgRoot = document.createElementNS("http://www.w3.org/2000/svg", "svg");
			//faust_svg_root = this.svgRoot;
			//svgRoot.setAttribute("xmlns:drag", DRAG_NS);
			var that = this;
			//addDragEventListener(DRAGMOVE, function(){
			//	var innerContainer = document.getElementById("transcript_container");
			//	that.intoView(innerContainer, that.svgRoot);
			//});
			svgRoot.setAttribute("class", "diplomatic");
			this._appendPatternDefs(svgRoot);
			container.getDOMNode().appendChild(svgRoot);
			

			//var visComponent = this.get('visComponent');

			try {
				//var visComponent = Faust.DocumentRanges.transcriptVC(this.get('transcript'));
				var documentBuilder = new Y.Faust.DocumentAdhocTree();
				var visComponent = documentBuilder.transcriptVC(this.get('transcript'));
				var innerContainer = document.createElementNS(SVG_NS, "g");
				innerContainer.setAttribute("id", "transcript_container");
				visComponent.svgCont = innerContainer;
				svgRoot.appendChild(innerContainer);

				//this.alignMainZone();
				while (innerContainer.hasChildNodes()) 
					this.innerContainer.removeChild(innerContainer.firstChild);

				// 	//FIXME calculate the required number of iterations
				visComponent.render();
				this.relayout(visComponent, svgRoot, innerContainer, container);
				
				//this.center(svgRoot, container);
				
				// }
				
				//Faust.DocumentTranscriptCanvas.prototype.intoView(innerContainer, this.svgRoot);
				
				// var setHeight = function() {
				// 	var transcriptNavHeight =
				// 		parseInt(Y.one('#transcript-navigation').getComputedStyle('height')) +
				// 		parseInt(Y.one('#transcript-navigation').getComputedStyle('marginTop')) +
				// 		parseInt(Y.one('#transcript-navigation').getComputedStyle('marginBottom'));
				
				// 	var transcriptHeight = Y.DOM.winHeight() - transcriptNavHeight;
				// 	Y.one('#transcript').setStyle('height', transcriptHeight + "px");
				// };
				
				// setHeight();
				// Y.on('resize', setHeight);
				
				// Y.one('#transcript-facsimile').scrollIntoView(); 
				// initializeDraggableElements();
				
			} catch(error) {
			 	if (typeof error === 'string' && error.substring(0, Faust.ENC_EXC_PREF.length) === Faust.ENC_EXC_PREF)
			 		this.displayError(error);
			 	else
			 		throw (error);
			}
			
			
			

		},
		
	} , {
		ATTRS: {
			rootVC: { validator: function(v) { return true; } }
		}
	});

	Y.mix(Y.namespace("Faust"), {
        DiplomaticTranscriptView: DiplomaticTranscriptView,
	});
	
}, '0.0', {
	requires: ['view', 'node', 'document-model', 'document-adhoc-tree', 'document-configuration-faust',
			   'document-view-svg', 'async-queue']
});

/*


	

	Faust.DocumentTranscriptCanvas.prototype = {
			

		alignMainZone: function() {
			if (!Faust.DocumentController.mainZone)
				throw (Faust.ENC_EXC_PREF + "No main zone specified!");
			//position absolutely
			Faust.DocumentController.mainZone.setAlign("hAlign", new Faust.AbsoluteAlign(Faust.DocumentController.mainZone, 0, 0,Faust.Align.EXPLICIT));
			Faust.DocumentController.mainZone.setAlign("vAlign", new Faust.AbsoluteAlign(Faust.DocumentController.mainZone, 90, 0, Faust.Align.EXPLICIT));
		},
		
		add: function(vc) 
		{},
		render: function(transcript) {
		}
		
	};
	
*/
