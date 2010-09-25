Faust.DocumentView = function(fd) {
	this.fd = fd;
	
	var pages = [];
	var units = fd.descendants();
	for (var uc = 0; uc < units.length; uc++)
		if (units[uc].transcript && units[uc].transcript.facsimiles.length > 0)
			pages.push(units[uc]);
			
	this.pages = pages;
	this.currentPage = 0;
};

Faust.DocumentView.prototype = {
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
	renderPageNavigation: function() {
		var gallery = Y.one("#page-gallery");
		gallery.setContent("Loading ...");

		var scrollView = new Y.ScrollView({ 
			srcNode: gallery, 
			width: 900,
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
			img.set("src", iip + "?FIF=" + page.transcript.facsimiles[0].encodedPath() + ".tif" + "&JTL=1,0");
			img.set("alt", page.order);
			Y.on("dblclick", function(e, page) { this.setPage(page.order); }, img, this, page);

			var li = Y.Node.create("<li>");
			li.append(page.order);
			li.append("<br>");
			li.append(img);
			list.append(li);
		}
		
		gallery.setContent(list);	
		scrollView.render();	
	},
	renderPage: function() {
		if (this.pages.length <= this.currentPage) return;	
		this.renderFacsimiles();
		this.renderTranscript();
	},
	renderFacsimiles: function() {
		Y.one("#transcript-facsimile").setContent("");
		swfobject.embedSWF(cp + "/static/swf/IIPZoom.swf", 
			"transcript-facsimile", "450px", "600px", 
			"9.0.0", cp + "/static/swf/expressInstall.swf", {
				server: iip,
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
		Y.one("#transcript-document").scrollIntoView();	
		this.pages[this.currentPage].transcription(function(t) { transcript = t; });
	}
};