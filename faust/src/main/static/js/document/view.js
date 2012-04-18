SVG_NS = "http://www.w3.org/2000/svg";
DRAG_NS = "http://www.codedread.com/dragsvg";
var faust_svg_root = null;

Faust.YUI().use("node", "dom", "dom-screen", "event", "overlay", "scrollview", "dump", "async-queue", "resize", function(Y) {
		
	// TODO: cut dependencies to controller
	
	Faust.DocumentTranscriptCanvas =  function(node) {
		this.svgRoot = document.createElementNS("http://www.w3.org/2000/svg", "svg");
		faust_svg_root = this.svgRoot;
		this.svgRoot.setAttribute("xmlns:drag", DRAG_NS);
		var that = this;
		addDragEventListener(DRAGMOVE, function(){
			var containerElement = document.getElementById("transcript_container");
			that.intoView(containerElement, that.svgRoot);
		});
		this.svgRoot.setAttribute("class", "diplomatic");
		this.appendPatternDefs(this.svgRoot);
		Y.Node.getDOMNode(node).appendChild(this.svgRoot);
	};
	

	Faust.DocumentTranscriptCanvas.prototype = {
			
		intoView: function (containerElement, svgCont) {
			var rootBBox = containerElement.getBBox();
			containerElement.setAttribute("transform", "translate(" + (- rootBBox.x) + "," + (- rootBBox.y) + ")");
			svgCont.setAttribute("width", rootBBox.width);
			svgCont.setAttribute("height", rootBBox.height);
		},

		alignMainZone: function() {
			if (!Faust.DocumentController.mainZone)
				throw (Faust.ENC_EXC_PREF + "No main zone specified!");
			//position absolutely
			Faust.DocumentController.mainZone.setAlign("hAlign", new Faust.AbsoluteAlign(Faust.DocumentController.mainZone, 0, 0,Faust.Align.EXPLICIT));
			Faust.DocumentController.mainZone.setAlign("vAlign", new Faust.AbsoluteAlign(Faust.DocumentController.mainZone, 90, 0, Faust.Align.EXPLICIT));
		},
		
		displayError: function(error) {
			var msg = Y.Node.create('<p/>');
			msg.append(error.toString());
			var errorDisplay = Y.one('#error-display');
			errorDisplay.append(msg);
			errorDisplay.show();
			
		},
		add: function(vc) 
		{},
		relayout: function() {

			view = this.view;
			that = this;
			var containerElement = document.getElementById("transcript_container");
			aq = new Y.AsyncQueue();
			aq.add({
					fn : view.layout,
					timeout: 10,
					iterations: 5,
					context: view
				},
				{
					fn : function() {Faust.DocumentTranscriptCanvas.prototype.intoView(containerElement, that.svgRoot)},
					timeout: 0,
					iterations: 1,
					context: view						
				});
			aq.run();
		},
		render: function(transcript) {
			Faust.DocumentController.idMap = {};
			Faust.DocumentController.postBuildDeferred = [];
			Faust.DocumentController.mainZone = null;
			try {
				this.view = Faust.DocumentController.buildVC(null, transcript.root("ge:document"));
				var containerElement = document.createElementNS(SVG_NS, "g");
				containerElement.setAttribute("id", "transcript_container");
				this.svgRoot.appendChild(containerElement);
				Y.each(Faust.DocumentController.postBuildDeferred, function(f) {f.apply(this)});
				this.alignMainZone();
			} catch(error) {
				if (typeof error === 'string' && error.substring(0, Faust.ENC_EXC_PREF.length) === Faust.ENC_EXC_PREF)
					this.displayError(error);
				else
					throw (error);
			}
			
			while (containerElement.hasChildNodes()) this.containerElement.removeChild(containerElement.firstChild);
			
			if (this.view) {
				//FIXME calculate the required number of iterations
				this.view.render();
				this.relayout();

			}
						
			Faust.DocumentTranscriptCanvas.prototype.intoView(containerElement, this.svgRoot);
			
			var setHeight = function() {
				var transcriptNavHeight =
					parseInt(Y.one('#transcript-navigation').getComputedStyle('height')) +
					parseInt(Y.one('#transcript-navigation').getComputedStyle('marginTop')) +
					parseInt(Y.one('#transcript-navigation').getComputedStyle('marginBottom'));

				var transcriptHeight = Y.DOM.winHeight() - transcriptNavHeight;
				Y.one('#transcript').setStyle('height', transcriptHeight + "px");
			};
			
			setHeight();
			Y.on('resize', setHeight);
			
			Y.one('#transcript-facsimile').scrollIntoView(); 
			initializeDraggableElements();
		}
		
	};
	
	Faust.DocumentTranscriptCanvas.prototype.appendPatternDefs = function(svgRoot) {
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
	};


	Faust.DocumentView = function(fd) {
		this.fd = fd;

		var pages = [];
		var units = fd.descendants();
		for (var uc = 0; uc < units.length; uc++)
			if (units[uc].transcript && units[uc].transcript.facsimiles.length > 0)
				pages.push(units[uc]);

		this.pages = pages;
		this.currentPage = 0;
		this.viewMode = "text-facsimile";
		this.initUI();
		this.setPage(parseInt(window.location.hash.substring(1)));	
	};

	Faust.DocumentView.prototype = {
		initUI: function() {
			this.browserOverlay = new Y.Overlay({
				srcNode: "#transcript-browser",
				width: "800px",
				height: "280px",
				align: { node: null},
				centered: true,
				visible: false,			
			});

			Y.on("click", function(e) {
				e.preventDefault();
				this.renderPageBrowser();
				this.browserOverlay.show();
			}, "#transcript-browse", this);
			Y.on("click", function(e) {
				e.preventDefault(); 
				this.browserOverlay.hide();
			}, "#transcript-hide-browser", this);

			Y.on("click", function(e) {
				e.preventDefault();
				if (this.currentPage > 0) {
					this.currentPage = this.currentPage - 1;
					this.renderPage();			
				}		
			}, "#transcript-prev-page", this);

			Y.on("click", function(e) {
				e.preventDefault();
				if ((this.currentPage + 1) < this.pages.length) {
					this.currentPage = this.currentPage + 1;
					this.renderPage();	
				}		
			}, "#transcript-next-page", this);

			Y.on("click", function(e) {
				//e.preventDefault();
				//alert(this.pages[this.currentPage].transcript.facsimiles[0].encodedPath() + ".tif");
				//window.location = '/static/js/ext-imageannotation/svg-editor.html';
			}, "#image-link", this);
			
			var viewModeSelector = Y.one("#transcript-view-mode");
			viewModeSelector.get("options").each(function(o) {
				if (this.viewMode == o.get("value")) {
					o.set("selected", "selected");
				} else {
					o.set("selected", null);
				}
			}, this);
			Y.on("change", function(e) {
				viewModeSelector.get("options").each(function(o) {
					if (o.get("selected")) {
						this.changeViewMode(o.get("value"));
					}
				}, this);
			}, viewModeSelector, this);
			
			var overlaySelector = Y.one("#transcript-preference-overlay");
			overlaySelector.get("options").each(function(o) {
				if (Faust.LayoutPreferences.overlay == o.get("value")) {
					o.set("selected", "selected");
				} else {
					o.set("selected", null);
				}
			}, this);
			Y.on("change", function(e) {
				overlaySelector.get("options").each(function(o) {
					if (o.get("selected")) {
						Faust.LayoutPreferences.overlay = o.get("value");
					}
				}, this);
				//TODO just do a relayout
				//this.canvas.relayout();
				this.renderPage();
			}, overlaySelector, this);	
		},
		changeViewMode: function(mode) {
			if ("text" == mode || "facsimile" == mode) {
				this.viewMode = mode;
			} else {
				this.viewMode = "text-facsimile";
			}
			this.renderPage();
		},
		setPage: function(page) {
			this.currentPage = 0;
			if (Y.Lang.isNumber(page)) {
				for (var pc = 0; pc < this.pages.length; pc++) {
					if (this.pages[pc].order == page) {
						this.currentPage = pc;
						break;
					}
				}
			}
			this.renderPage();	
		},
		renderPageBrowser: function() {
			if (this.browseView) return;

			var browser = Y.one("#transcript-browser .yui3-widget-bd");

			this.browseView = new Y.ScrollView({ 
				srcNode: browser, 
				width: 780,
				flick: { 
					preventDefault: function(e) { return (Y.UA.gecko); },
					minDistance: 10,
					minVelocity: 0.3				
				}
			});

			var list = Y.Node.create("<ul>");
			for (var pc = 0; pc < this.pages.length; pc++) {
				var page = this.pages[pc];
				var img = Y.Node.create("<img>");
				img.set("src", Faust.FacsimileServer + "?FIF=" + page.transcript.facsimiles[0].encodedPath() + ".tif" + "&JTL=1,0");
				img.set("alt", page.order);
				Y.on("dblclick", function(e, page) {
					this.setPage(page.order);
					this.browserOverlay.hide();				
				}, img, this, page);

				var li = Y.Node.create("<li>");
				li.append(page.order);
				li.append("<br>");
				li.append(img);
				list.append(li);
			}

			browser.setContent(list);
			this.browseView.render();
			this.browserOverlay.render();		
			Y.one("#transcript-browser").show();
		},
		renderPage: function() {
			if (this.pages.length <= this.currentPage) return;
			window.location.hash = ("#" + this.pages[this.currentPage].order);

			this.updateNavigation();
			this.clearMessages();
			
			var navigation = Y.one("#transcript-navigation");
			var transcript = Y.one("#transcript");
			if (transcript != null) {
				transcript.remove();
				transcript.destroy();
			} 
			transcript = Y.Node.create('<div id="transcript"></div>');
			if (this.viewMode == 'text') {
				transcript.append ('<div id="transcript-text" style="width: 100%"></div>');
			} else if (this.viewMode == 'facsimile') {
				transcript.append('<div id="transcript-facsimile" style="width: 100%"></div>');				
			} else {
				//transcript.addClass("yui3-g"); doesn't work with resize in firefox
				transcript.setStyle("position", "relative");
				transcript.append('<div id="transcript-facsimile" style="width: 50%; position: relative; left: 0px"></div>');				
				transcript.append('<div id="transcript-text" style="width: 50%; position: absolute; top: 0px;right: 0px"></div>');
			}
			
			navigation.insert(transcript, "after");
			this.renderTranscript();
			this.renderFacsimiles();

			// Make the view resizable
			if (this.viewMode != 'text' && this.viewMode != 'facsimile') {
				var handle = Y.Node.create('<div>HANDLE</div>');
				var resize = new Y.Resize({
					node: '#transcript-facsimile',
					handles: 'r',
					autoHide: false
				});
				var transcript_text = Y.one('#transcript-text');
				resize.on('resize:resize', function(ev){
					var width =	parseInt(Y.one("#transcript").getStyle('width'));
					transcript_text.setStyle("width", (width - ev.info.offsetWidth) + "px");
				})
			}
		},
		updateNavigation: function() {
			var browsePages = Y.one("#transcript-browse");
			if (this.pages.length > 1 && this.pages.length < 40) 
				browsePages.removeClass("disabled");
			else
				browsePages.addClass("disabled");

			var prevPage = Y.one("#transcript-prev-page");
			if (this.pages.length > 0 && this.currentPage > 0)
				prevPage.removeClass("disabled");
			else
				prevPage.addClass("disabled");

			var nextPage = Y.one("#transcript-next-page");
			if ((this.currentPage + 1) < this.pages.length)
				nextPage.removeClass("disabled");
			else
				nextPage.addClass("disabled");
			
			if (Y.one('#image-link'))
				Y.one('#image-link').set('href', imageLinkBase + '/' + this.currentPage);

			var schemeAuth = location.href.slice(0, location.href.search(/[^\/]\/[^\/]/) + 1);
			//var editxml = 'editxml://' + authority + '/xml/' + this.pages[this.currentPage].transcript.source.components[2];
			
			var sourceComponents = this.pages[this.currentPage].transcript.source.components;
			var xmlsource = schemeAuth + '/' + sourceComponents[1] + '/' + sourceComponents[2]; 
			if (Y.one('#edit-source'))
				Y.one('#edit-source').set('href', xmlsource);
			

		},
		clearMessages: function(){
			var errorDisplay = Y.one("#error-display");
			errorDisplay.empty();
			errorDisplay.hide();
			
		},
		renderFacsimiles: function() {
			var container = Y.one("#transcript-facsimile");
			if (container == null) return;
			
//			container.append('<div id="transcript-swf"></div>');
//			swfobject.embedSWF(Faust.contextPath + "/static/swf/IIPZoom.swf", 
//				"transcript-swf",
//				"100%", "100%",
//				"9.0.0", Faust.contextPath + "/static/swf/expressInstall.swf", {
//					server: Faust.FacsimileServer,
//					image: this.pages[this.currentPage].transcript.facsimiles[0].encodedPath() + ".tif",
//					navigation: true,
//					credit: "Copyright Digitale Faust-Edition"
//				}, {
//					scale: "noscale",
//					bgcolor: "#000000",
//					allowfullscreen: "true",
//					allowscriptaccess: "always",
//					wmode: "opaque"
//				});
			
			container.append('<div id="transcript-ajax" style="width:100%; height: 100%;"></div>');
		    var server = Faust.FacsimileServer;
		    var images = this.pages[this.currentPage].transcript.facsimiles[0].encodedPath() + ".tif";
		    var credit = "Copyright Digitale Faust-Edition";
		    var iipmooviewer = new IIPMooViewer( "transcript-ajax", {
				image: images,
				server: server,
				credit: credit, 
				scale: 100.0,
				showNavWindow: true,
				showNavButtons: true,
				winResize: true,
				protocol: 'iip',
				prefix: '../../../static/js/imageviewer/images/'
		    });

		},
		renderTranscript: function() {
			var container = Y.one("#transcript-text");
			if (container == null) return;
			var that = this;
			this.pages[this.currentPage].transcription(function(t) {				
				that.canvas = new Faust.DocumentTranscriptCanvas(container);
				that.canvas.render(t);
			});
		}
	};
});