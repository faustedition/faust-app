Faust.YUI().use("node", "dom", "event", "overlay", "scrollview", "dump", function(Y) {
	Faust.DocumentTranscriptCanvas = function(node) {
		this.svgNode = document.createElementNS("http://www.w3.org/2000/svg", "svg");
		this.svgNode.setAttribute("width", "1000");
		this.svgNode.setAttribute("height", "1500");
		Y.Node.getDOMNode(node).appendChild(this.svgNode);
	};

	Faust.DocumentTranscriptCanvas.prototype = {
			
		idMap: {},
		
		postBuildDeferred: [],
		
		render: function(transcript) {
			this.idMap = {};
			this.postBuildDeferred = [];
			this.view = this.build(this, transcript.root("ge:document"));
			var rootElement = document.createElementNS("http://www.w3.org/2000/svg", "g");
			rootElement.setAttribute("transform", "translate(100,100)");
			this.svgNode.appendChild(rootElement);
			Y.each(this.postBuildDeferred, function(f) {f.apply(this)});
			while (rootElement.hasChildNodes()) this.rootElement.removeChild(rootElement.firstChild);
			if (this.view) {
				//FIXME calculate the required number of iterations
				for (var i=0; i < 7; i++)
					this.view.layout();
				this.view.render();
			}				
		},
		build: function(parent, tree) {
			if (tree == null) return null;			
			var vc = null;
			var node = tree.node;
			var nodeIsInvisible = false;
			
			if ((node instanceof Goddag.Text) && (parent != null)) { //&& (parent instanceof Faust.Line)) {
				var textAttrs = {};
				Y.each(node.ancestors(), function(a) {
					var elem = a.node;
					if (elem.name == "f:hand") {
						textAttrs.hand = elem.attrs["f:id"];
					} else if (elem.name == "ge:rewrite") {
						textAttrs.rewrite = elem.attrs["tei:hand"];
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
				vc = new Faust.Text(node.text(), textAttrs);
			} else if (node instanceof Goddag.Element) {
				if (node.name == "ge:document") {
					vc = new Faust.Surface();
				} else if (node.name == "tei:surface") {
					vc = new Faust.Surface();
				} else if (node.name == "tei:zone")	{
					vc = new Faust.Zone();
				} else if (node.name == "ge:line") {
					var lineAttrs = {};
					var rendition = node.attrs["ge:rend"] || "";
					if (rendition.indexOf("center") >= 0) {
						lineAttrs.center = true;
					} else if (rendition.indexOf("indent") >= 0) {
						var start = rendition.indexOf("indent-");
						lineAttrs.indent = parseInt(rendition.substring(start + 7, rendition.length)) / 100.0;
					}
					vc = new Faust.Line(lineAttrs);
				} else if (node.name == "f:vspace") {
					if (node.attrs["f:unit"]=="lines")
						{
							vc = new Faust.BreakingVC();
							var quantity = node.attrs["f:quantity"];
							for (var ins_spc = 0; ins_spc < quantity; ins_spc++)
								vc.add(new Faust.Line({}));
						}
				} else if (node.name == "f:grLine") {
					//vc = new Faust.GLine();
				} else if (node.name == "f:grBrace") {
					//vc = new Faust.GBrace();
				} else if (node.name == "tei:anchor") {
					//use empty text element as an anchor
					vc = new Faust.Text("", {});
				} else if (node.name == "f:ins" && node.attrs["f:orient"] == "right") {
					vc = new Faust.DefaultVC();
					//Einweisungszeichen
					vc.add (new Faust.Text("\u2308", {}));
				// Default Elements
				} else if (node.name in {}) {
					vc = new Faust.DefaultVC();
				//Invisible elements				
				} else if (node.name in {"tei:rdg":1}){
					nodeIsInvisible = true;
				}
				aligningAttributes = ["f:at", "f:left", "f:left-right", "f:right", "f:right-left", "f:top", "f:top-bottom", "f:bottom", "f:bottom-top"];
				
				var idMap = this.idMap;
				var postBuildDeferred = this.postBuildDeferred;
				Y.each(aligningAttributes, function(a){
					if (a in node.attrs) {
						if (!vc) {
							vc = new Faust.DefaultVC();
						}
						//FIXME id hash hack; do real resolution of references
						var anchorId = node.attrs[a].substring(1);
						var coordName = a in {"f:at":1, "f:left":1, "f:left-right":1, "f:right":1, "f:right-left":1}? "x" : "y";
						var extName = coordName == "x" ? "width" : "height";
						var alignName = coordName == "x" ? "hAlign" : "vAlign";
						var myJoint = a in {"f:left":1, "f:left-right":1, "f:top":1, "f:top-bottom":1}? 0 : 1;
						var yourJoint = a in {"f:at":1, "f:left":1, "f:right-left":1, "f:top":1, "f:bottom-top":1}? 0 : 1;
						
						if ("f:orient" in node.attrs)
							myJoint = node.attrs["f:orient"] == "left" ? 1 : 0;
						
						postBuildDeferred.push(
								function(){
									var anchor = idMap[anchorId];
									vc.setAlign(alignName, new Faust.Align(vc, anchor, coordName, extName, myJoint, yourJoint, Faust.Align.EXPLICIT));
								});
					}						
				});
				
				// TODO redundant with line properties
				// TODO special treatment of zones
				if ("ge:rend" in node.attrs) {
					if (node.attrs["ge:rend"] == "right") {
			 			vc.setAlign("hAlign", new Faust.Align(vc, parent, "x", "width", 1, 1, Faust.Align.REND_ATTR));
					} else if (node.attrs["ge:rend"] == "left") {
			 			vc.setAlign("hAlign", new Faust.Align(vc, parent, "x", "width", 0, 0, Faust.Align.REND_ATTR));
					} else if (node.attrs["ge:rend"] == "center") {
			 			vc.setAlign("hAlign", new Faust.Align(vc, parent, "x", "width", 0.5, 0.5, Faust.Align.REND_ATTR));
					}


				}
			}
			
			if (vc != null) {
				if (parent != null && parent !== this) parent.add(vc);
				parent = vc;
			}

 			if (node instanceof Goddag.Element) {
 				xmlId = node.attrs["xml:id"];
 				if (xmlId) {
 					this.idMap[xmlId] = vc;
 				}
			}

 			if (!nodeIsInvisible)
 				Y.each(tree.children, function(c) { this.build(parent, c); }, this);
 			
 			// After all children, TODO move this into appropriate classes
 			if (node.name == "f:ins" && node.attrs["f:orient"] == "left") {
 				// Einweisungszeichen
 				vc.add (new Faust.Text("\u2309", {}));
 			}
			return vc;
		}
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
			
			var viewModeSelector = Y.get("#transcript-view-mode");
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
			Y.one("#transcript-browser").removeClass("hidden");
		},
		renderPage: function() {
			if (this.pages.length <= this.currentPage) return;
			window.location.hash = ("#" + this.pages[this.currentPage].order);

			this.updateNavigation();
			
			var navigation = Y.get("#transcript-navigation");
			var transcript = Y.get("#transcript");
			if (transcript != null) {
				transcript.remove();
				transcript.destroy();
			} 
			transcript = Y.Node.create('<div id="transcript"></div>');
			if (this.viewMode == 'text') {
				transcript.append('<div id="transcript-text" style="width: 900px"></div>');
			} else if (this.viewMode == 'facsimile') {
				transcript.append('<div id="transcript-facsimile" style="width: 900px"></div>');				
			} else {
				transcript.addClass("yui3-g");
				transcript.append('<div class="yui3-u-1-2" id="transcript-facsimile"></div>');				
				transcript.append('<div class="yui3-u-1-2" id="transcript-text"></div>');
			}
			
			navigation.insert(transcript, "after");
			this.renderTranscript();
			this.renderFacsimiles();				
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

		},
		renderFacsimiles: function() {
			var container = Y.get("#transcript-facsimile");
			if (container == null) return;
			container.append('<div id="transcript-swf"></div>')
			swfobject.embedSWF(Faust.contextPath + "/static/swf/IIPZoom.swf", 
				"transcript-swf",
				(container.get("offsetWidth") - 20) + "px", "600px",
				"9.0.0", Faust.contextPath + "/static/swf/expressInstall.swf", {
					server: Faust.FacsimileServer,
					image: this.pages[this.currentPage].transcript.facsimiles[0].encodedPath() + ".tif",
					navigation: true,
					credit: "Copyright Digitale Faust-Edition"
				}, {
					scale: "noscale",
					bgcolor: "#000000",
					allowfullscreen: "true",
					allowscriptaccess: "always"
				});	
		},
		renderTranscript: function() {
			var container = Y.get("#transcript-text");
			if (container == null) return;
			this.pages[this.currentPage].transcription(function(t) {				
				var canvas = new Faust.DocumentTranscriptCanvas(container);
				canvas.render(t);
			});
		}
	};
});