DocumentView = function(document) {
	this.document = document;
	
	var pages = [];
	this.document.descendants().forEach(function(unit) {
		if (Y.Object.hasKey(unit, "transcript") && unit.transcript.facsimiles.length > 0) {
			pages.push(unit);
		}
	});
	this.pages = pages;
	this.currentPage = 0;
};

DocumentView.prototype.setPage = function(page) {
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
};

DocumentView.prototype.renderPageNavigation = function() {
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
	var document = this;
	this.pages.forEach(function(page) {
		var img = Y.get(Y.DOM.create("<img>"));
		img.set("src", iip + "?FIF=" + page.transcript.facsimiles[0].encodedPath() + ".tif" + "&JTL=1,0");
		img.set("alt", page.order);
		img.on("dblclick", function(e) { document.setPage(page.order); });					
			
		var li = Y.Node.create("<li>");
		li.append(page.order);
		li.append("<br>");
		li.append(img);
		list.append(li);
	});

	gallery.setContent(list);	
	scrollView.render();	
};
	
DocumentView.prototype.renderPage = function() {
	if (this.pages.length <= this.currentPage) return;	
	this.renderFacsimiles();
	this.renderTranscript();
};

DocumentView.prototype.renderFacsimiles = function() {
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
};

DocumentView.prototype.renderTranscript = function() {
	var doc = Y.one("#transcript-document");
	doc.setContent("Loading ...");
	doc.addClass("transcript-loading");
	doc.scrollIntoView();
	
	this.pages[this.currentPage].loadTranscription(function(t) {
		doc.removeClass("transcript-loading");
		doc.setContent(t.trees[0].text());				
	});
};
