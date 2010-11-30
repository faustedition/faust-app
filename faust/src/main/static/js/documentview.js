Faust.YUI().use("node", "dom", "event", "overlay", "scrollview", "dump", function(Y) {
	Faust.DocumentTranscriptCanvas = function(frame) {
		this.frame = window.frames[frame];
		this.svg = this.frame.document;		
	};

	Faust.DocumentTranscriptCanvas.prototype = {
		init: function(transcript) {
			this.view = this.build(null, transcript.root("ge:document"));	
		},
		render: function() {
			var root = this.svg.documentElement;
			while (root.hasChildNodes()) root.removeChild(root.firstChild);
			this.frame.scrollTo(0, 0);			
			if (this.view) {
				this.view.layout();
				this.view.render();
			}
		},
		build: function(parent, tree) {
			if (tree == null) return null;			
			var vc = null;
			var node = tree.node;
			
			if ((node instanceof Goddag.Text) && (parent != null) && (parent instanceof Faust.Line)) {
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
					}
				});				
				vc = new Faust.Text(this.svg, node.text(), textAttrs);
			} else if (node instanceof Goddag.Element) {
				if (node.name == "ge:document") {
					vc = new Faust.Surface(this.svg);
				} else if (node.name == "tei:surface") {
					vc = new Faust.Surface(this.svg);
				} else if (node.name == "tei:zone")	{
					vc = new Faust.Zone(this.svg);
				} else if (node.name == "ge:line") {
					vc = new Faust.Line(this.svg);
				} else if (node.name == "f:grLine") {
					vc = new Faust.GLine(this.svg);
				} else if (node.name == "f:grBrace") {
					vc = new Faust.GBrace(this.svg);
				}
			}
			
			if (vc != null) {
				if (parent != null) parent.add(vc);
				parent = vc;
			}
			
			Y.each(tree.children, function(c) { this.build(parent, c); }, this);
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
			this.renderFacsimiles();
			this.renderTranscript();
		},
		updateNavigation: function() {
			var browsePages = Y.one("#transcript-browse");
			if (this.pages.length > 1) 
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
			Y.one("#transcript-facsimile").setContent("");
			swfobject.embedSWF(Faust.contextPath + "/static/swf/IIPZoom.swf", 
				"transcript-facsimile", "450px", "600px", 
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
			this.pages[this.currentPage].transcription(function(t) {
				var canvas = new Faust.DocumentTranscriptCanvas("transcript-canvas");
				canvas.init(t);
				canvas.render();
			});
		}
	};
});