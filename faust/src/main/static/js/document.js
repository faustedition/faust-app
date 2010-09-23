MaterialUnit = function() {};
MaterialUnit.prototype.descendants = function() {
	aggregate = function(aggregator, mu) {
		mu.contents.forEach(function(child) { 
			aggregator.push(child);
			aggregate(aggregator, child); 
		});
	}
	aggregator = [];
	aggregate(aggregator, this);
	return aggregator; 
};
MaterialUnit.prototype.render = function(Y) {
	swfobject.embedSWF(cp + "/static/swf/IIPZoom.swf", 
		"transcript-facsimile", "450px", "600px", 
		"9.0.0", cp + "/static/swf/expressInstall.swf", {
			server: iipSrv,
			image: this.transcript.facsimiles[0].replace("faust://facsimile/", "") + ".tif",
			navigation: true,
			credit: "Copyright Digitale Faust-Edition"
		}, {
			scale: "noscale",
			bgcolor: "#000000",
			allowfullscreen: "true",
			allowscriptaccess: "always"
		});
	
	doc = Y.one("#transcript-document");
	doc.setContent(Y.dump(this));
	doc.scrollIntoView();
};

FaustDocument = function() {};
FaustDocument.prototype.show = function(Y) {
	var list = Y.Node.create("<ul>");
	var firstPage = null;
	this.descendants().forEach(function(unit) {
		if ("transcript" in unit) {
			img = unit.transcript.thumbnail(Y, (unit.type + " " + unit.order));
			if (img != null) {
				if (firstPage == null) firstPage = unit;
				img.on("dblclick", function(e) { unit.render(Y); });					
				
				li = Y.Node.create("<li>");
				li.append(unit.order);
				li.append("<br>");
				li.append(img);
				list.append(li);
			}
		}
	});

	if (firstPage != null) firstPage.render(Y);

	var gallery = Y.one("#page-gallery");
	gallery.append(list);

	this.scrollView = new Y.ScrollView({ 
		srcNode: gallery, 
		width: 900,
		flick: { 
			preventDefault: function(e) { return (Y.UA.gecko); },
			minDistance: 10,
			minVelocity: 0.3				
		}
	});
	this.scrollView.render();	
};

Transcript = function() {};
Transcript.prototype.thumbnail = function(Y, alt) {
	if (this.facsimiles.length == 0) return null;
	img = Y.get(Y.DOM.create("<img>"));
	img.set("src", iipSrv + "?FIF=" + encodeURIComponent(this.facsimiles[0].replace("faust://facsimile/", "") + ".tif") + "&JTL=1,0");
	img.set("alt", alt);
	return img;
};

FaustYUI().use("node", "dom", "dump", "event", "scrollview", function(Y) {
	get(window.location, function(doc) { 
		doc.show(Y); 
	}, function(key, value) {
		if (key === "order") {
			if (this.type === "document" || this.type === "archival_unit") {
				Y.augment(this, FaustDocument);
			}
			Y.augment(this, MaterialUnit);
		}
		if (key === "source") {			
			Y.augment(this, Transcript);
		}
		return value;
	});
});
