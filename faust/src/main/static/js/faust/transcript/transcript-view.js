Faust.LayoutPreferences = {
	
	overlay : "overlay"
	
};

YUI.add('transcript-view', function (Y) {


	var DiplomaticTranscriptView = Y.Base.create("diplomatic-transcript-view", Y.View, [], {

		displayError: function(error) {
			var msg = Y.Node.create('<p/>');
			msg.append(error.toString());
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

			var that = this;
			var aq = new Y.AsyncQueue();
			var layoutAndCenter = function() {
				// TODO: maybe fix and re-enable. it's not very importatnt and interferes with absolute alignment
				//that.intoView(innerContainer, svgRoot);
				//that.center(svgRoot, container);
				visComponent.layout();
			};

			aq.add(
				{
					fn : layoutAndCenter, //visComponent.layout,
					timeout: 10,
					iterations: 15,
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

			if (this.get('source')) {
				var transcriptSource = cp + '/' + this.get('source').components[1] + '/' + this.get('source').components[2];
				var imageLinkPath = Faust.imageLinkBase + '/' + this.get('pagenum');
				this.editortoolbar = Y.Node.create('<div id="editor-toolbar" >' +
					'<a href="' + transcriptSource + '">XML source </a>' +
					'<a href="' + imageLinkPath + '"> Text-Image-Links</a>' +
					'</div>');
				container.appendChild(this.editortoolbar);
			}
			if (this.get('transcript')) {
				this.errorDisplay = Y.Node.create('<div id="error-display"></div>');
				container.appendChild(this.errorDisplay);
				var svgRoot = document.createElementNS("http://www.w3.org/2000/svg", "svg");

				if (this.get('draggable')) {
					svgRoot.setAttribute("xmlns:drag", DRAG_NS);
					var that = this;
					addDragEventListener(DRAGMOVE, function(){
						var innerContainer = document.getElementById("transcript_container");
						that.intoView(innerContainer, that.svgRoot);
					});
				}
				svgRoot.setAttribute("class", "diplomatic");
				container.getDOMNode().appendChild(svgRoot);

				try {
					var documentBuilder = new Y.Faust.TranscriptAdhocTree();
					var visComponent = documentBuilder.transcriptVC(this.get('transcript'));
					var innerContainer = document.createElementNS(SVG_NS, "g");
					innerContainer.setAttribute("id", "transcript_container");
					visComponent.svgCont = innerContainer;
					svgRoot.appendChild(innerContainer);

					//this.alignMainZone();
					while (innerContainer.hasChildNodes())
						this.innerContainer.removeChild(innerContainer.firstChild);

					//FIXME calculate the required number of iterations
					visComponent.render();
					this.relayout(visComponent, svgRoot, innerContainer, container);
				} catch (error) {
					if (typeof error === 'string' && error.substring(0, Faust.ENC_EXC_PREF.length) === Faust.ENC_EXC_PREF)
						this.displayError(error);
					else
						throw (error);
				}
			} else {
				// no transcript available
				var noTranscriptDisplay = Y.Node.create('<div style="text-align: center; font-size: 400%">&#8709;</div>');
				container.appendChild(noTranscriptDisplay);
			}
		}
	} , {
		ATTRS: {
			rootVC: {},
			pagenum: { value: null},
			source: { value: null},
			draggable: { value: false}
		}
	});

	Y.mix(Y.namespace("Faust"), {
        DiplomaticTranscriptView: DiplomaticTranscriptView
	});
	
}, '0.0', {
	requires: ['view', 'node', 'transcript', 'transcript-adhoc-tree', 'transcript-configuration-faust',
			   'transcript-svg', 'async-queue']
});