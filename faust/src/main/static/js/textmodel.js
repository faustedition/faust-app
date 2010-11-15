SVG_NS = "http://www.w3.org/2000/svg";

Faust.YUI().use("oop", "dump", function(Y) {
	Faust.ViewComponent = function() {};
	Faust.ViewComponent.prototype = {
		initViewComponent: function(node) {
			this.parent = null;
			this.pos = -1;
			this.children = [];
			this.node = node;					
			this.svg = null;
			this.x = null;
			this.y = null;
		},
		add: function(vc) {
			vc.parent = this;
			vc.pos = this.children.length;
			this.children.push(vc);
		},
		previous: function() {
			return (this.parent == null || this.pos <= 0) ? null : this.parent.children[this.pos - 1];
		},
		next: function() {
			return (this.parent == null || (this.pos + 1) >= this.parent.children.length) ? null : this.parent.children[this.pos + 1];			
		},
		dimensions: function() {
			var width = 0;
			var height = 0;
			Y.each(this.children, function(c) { 
				var cd = c.dimensions();
				if (cd.width > width) width = cd.width;
				height += cd.height;
			});
			return { width: width, height: height };
		},
		svgContainer: function() {
			return (this.parent == null ? this.svg.documentElement : this.parent.svgNode);
		},
		render: function() {
			this.svgNode = this.createSvgNode();
			this.svgContainer().appendChild(this.svgNode);
			Y.each(this.children, function(c) { c.render(); });
		},
		computeStyles: function() { 
			return {}; 
		},
		setStyles: function(svgNode) {
			var styles = this.computeStyles();
			if (styles) {
				var stylesStr = "";
				for (style in styles) {
					stylesStr += (stylesStr.length == 0 ? "" : "; ") + (style + ": " + styles[style]);
				}
				svgNode.setAttribute("style", stylesStr);
			}			
		}
	};
	
	Faust.Surface = function() {};
	Faust.Surface.prototype.createSvgNode = function() {
		var prev = this.previous();
		if (prev) {
			this.x = prev.x;
			this.y = prev.y + prev.dimensions() + 100;
		} else if (this.parent) {
			this.x = this.parent.x;
			this.y = this.parent.y;
		} else {			
			this.x = 0;
			this.y = 0;
		}
		// surface-specific layout
		return this.svg.createElementNS(SVG_NS, "g");
	};
	Y.augment(Faust.Surface, Faust.ViewComponent);
			
	Faust.Zone = function() {};
	Faust.Zone.prototype.createSvgNode = function() {
		var prev = this.previous();
		if (prev) {
			this.x = prev.x;
			this.y = prev.y + prev.dimensions().height + 40;
		} else {
			this.x = this.parent.x + 20;
			this.y = this.parent.y + 40;
		}
		
		var dim = this.dimensions();
		var rect = this.svg.createElementNS(SVG_NS, "rect");
		rect.setAttribute("x", this.x - 10);
		rect.setAttribute("y", this.y - 10);
		rect.setAttribute("rx", 5);
		rect.setAttribute("ry", 5);
		rect.setAttribute("width", dim.width + 20);
		rect.setAttribute("height", dim.height + 20);
		rect.setAttribute("style", "fill: snow; stroke: lightgrey; stroke-dasharray: 5");

		var g = this.svg.createElementNS(SVG_NS, "g");
		g.appendChild(rect);
		return g;
	};
	Y.augment(Faust.Zone, Faust.ViewComponent);
	
	Faust.Line = function() {};
	Faust.Line.prototype.createSvgNode = function() {
		var prev = this.previous();
		if (prev) {
			this.x = prev.x;
			this.y = prev.y + 20;
		} else {
			this.x = this.parent.x;
			this.y = this.parent.y + 20;
		}
		return this.svg.createElementNS(SVG_NS, "g");
	};
	Faust.Line.prototype.dimensions = function() {
		var width = 0;
		Y.each(this.children, function(c) { width += c.dimensions().width; });
		return { width: width, height: 20 };
	};
	Y.augment(Faust.Line, Faust.ViewComponent);
	
	Faust.Text = function() {};
	Faust.Text.prototype.text = function() {
		return this.node.text().replace(/\s+/, "\u00a0");
	}
	Faust.Text.prototype.createSvgNode = function() {
		var prev = this.previous();
		if (prev) {
			this.x = prev.x + prev.dimensions().width;
			this.y = prev.y;
		} else {
			this.x = this.parent.x;
			this.y = this.parent.y;
		}

		var view = this;
		var node = this.node;
		var text = this.svg.createElementNS(SVG_NS, "text");
		var contents = this.text();
		this.setStyles(text);
		text.setAttribute("x", this.x.toString());
		text.setAttribute("y", this.y.toString());
		text.appendChild(this.svg.createTextNode(contents));
		text.addEventListener("click", function(e) { 
			var tags = "";
			Y.each(node.ancestors(), function(a) { tags += (tags.length == 0 ? "" : ", ") + a.node.name });
			alert("Text: \"" + contents + "\"\n\nTags: " + tags + "\n\nPixels: " + Y.dump(view.dimensions())); 
		}, false);
		return text;
	};
	Faust.Text.prototype.computeStyles = function() {
		var styles = {};
		Y.each(this.node.ancestors(), function(a) {
			var elem = a.node;
			if (elem.name == "f:hand") {
				var hand = elem.attrs["f:id"];
				if (hand) {
					if (hand.indexOf("g_") >= 0) {
						styles["font-style"] = "italic";
					} else {
						styles["font-style"] = "normal";
					}
					if (hand.indexOf("_bl") >= 0) {
						styles["fill"] = "darkgrey";
					} else if (hand.indexOf("_t") >= 0) {
						styles["fill"] = "sienna";
					}
				}
				
			} else if (elem.name == "ge:rewrite") {
				styles["font-weight"] = "bold";
			} else if (elem.name == "f:under") {
				styles["opacity"] = "0.5";
			} else if (elem.name == "f:over") {
				styles["font-weight"] = "bold";
			}
		});
		return styles;
	}
	Faust.Text.prototype.dimensions = function() {
		var measureText = this.svg.createElementNS(SVG_NS, "text");
		this.setStyles(measureText);
		measureText.setAttribute("x", "-10000");
		measureText.setAttribute("y", "-10000");
		measureText.appendChild(this.svg.createTextNode(this.text()));
		this.svg.documentElement.appendChild(measureText);
		var bbox = measureText.getBBox();
		this.svg.documentElement.removeChild(measureText);
		return { width: Math.round(bbox.width), height: Math.round(bbox.height) };			
	};
	Y.augment(Faust.Text, Faust.ViewComponent);
	
	Faust.GLine = function() {};
	Y.augment(Faust.GLine, Faust.ViewComponent);
	Faust.GLine.prototype.dimensions = function() {
		return { width: 40, height: 20 };
	};
	Faust.GLine.prototype.createSvgNode = function() {
		var prev = this.previous();
		if (prev) {
			var pd = prev.dimensions();
			if (prev instanceof Faust.Text) {
				this.x = prev.x + pd.width;
				this.y = prev.y;				
			} else {
				this.x = prev.x;
				this.y = prev.y + pd.height;
			}
		} else {
			this.x = this.parent.x;
			this.y = this.parent.y;			
		}
		var line = this.svg.createElementNS(SVG_NS, "line");
		line.setAttribute("x1", this.x);
		line.setAttribute("y1", this.y - 10);
		line.setAttribute("x2", this.x + 40);
		line.setAttribute("y2", this.y - 10);
		line.setAttribute("stroke-width", 1);
		line.setAttribute("stroke", "black");
		return line;
	};
	Faust.GBrace = function() {};
	Y.augment(Faust.GBrace, Faust.ViewComponent);
	Faust.GBrace.prototype.dimensions = function() {
		return { width: 40, height: 20 };
	};
	Faust.GBrace.prototype.createSvgNode = function() {
		var prev = this.previous();
		if (prev) {
			var pd = prev.dimensions();
			if (prev instanceof Faust.Text) {
				this.x = prev.x + pd.width;
				this.y = prev.y;				
			} else {
				this.x = prev.x;
				this.y = prev.y + pd.height;
			}
		} else {
			this.x = this.parent.x;
			this.y = this.parent.y;			
		}
		var path = this.svg.createElementNS(SVG_NS, "path");
		path.setAttribute("d", "M " + (this.x) + " " + (this.y) + " q 5,-10 20,-5 q 5,0 10,-10 q -5,0 10,10 q 10,-5 20,5");
		path.setAttribute("stroke-width", 1);
		path.setAttribute("stroke", "black");
		path.setAttribute("fill", "transparent");
		return path;
	};
	
	
});
