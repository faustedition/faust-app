SVG_NS = "http://www.w3.org/2000/svg";

Faust.YUI().use("oop", "dump", function(Y) {
	Faust.ViewComponent = function() {};
	Faust.ViewComponent.prototype = {
		initViewComponent: function(svg) {
			this.parent = null;
			this.pos = -1;
			this.children = [];			
			this.svg = svg;
			
			this.x = null;
			this.y = null;
			this.width = null;
			this.height = null;
		},
		add: function(vc) {
			vc.parent = this;
			vc.pos = this.children.length;
			this.children.push(vc);
			return vc;
		},
		previous: function() {
			return (this.parent == null || this.pos <= 0) ? null : this.parent.children[this.pos - 1];
		},
		next: function() {
			return (this.parent == null || (this.pos + 1) >= this.parent.children.length) ? null : this.parent.children[this.pos + 1];			
		},
		svgContainer: function() {
			return (this.parent == null ? this.svg.documentElement : this.parent.svgNode);
		},
		layout: function() {
			this.computeDimension();			
			this.computePosition();
		},
		render: function() {
			this.svgNode = this.createSvgNode();
			this.svgContainer().appendChild(this.svgNode);
			Y.each(this.children, function(c) { c.render(); });
		},
		computeDimension: function() {
			Y.each(this.children, function(c) { c.computeDimension(); });
			this.dimension();
		},
		dimension: function() {
			this.width = 0;
			this.height = 0;
			Y.each(this.children, function(c) {
				if (c.width > this.width) this.width = c.width;
				this.height += c.height;
			}, this);			
		},
		computePosition: function() {
			this.position();
			Y.each(this.children, function(c) { c.computePosition(); });
		},
		position: function() {
			this.x = 0;
			this.y = 0;			
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
	
	Faust.Surface = function(svg) {
		this.initViewComponent(svg);
	};
	Faust.Surface.prototype.position = function() {
		var prev = this.previous();
		if (prev) {
			this.x = prev.x;
			this.y = prev.y + prev.height + 40;
		} else if (this.parent) {
			this.x = this.parent.x;
			this.y = this.parent.y;
		} else {			
			this.x = 0;
			this.y = 0;
		}		
		// TODO: surface-specific layout
	};
	Faust.Surface.prototype.createSvgNode = function() {
		return this.svg.createElementNS(SVG_NS, "g");
	};
	Y.augment(Faust.Surface, Faust.ViewComponent);
			
	Faust.Zone = function(svg) {
		this.initViewComponent(svg);
	};
	Faust.Zone.prototype.position = function() {
		var prev = this.previous();
		if (prev) {
			this.x = prev.x;
			this.y = prev.y + prev.height + 40;
		} else {
			this.x = this.parent.x + 20;
			this.y = this.parent.y + 40;
		}
	};
	Faust.Zone.prototype.createSvgNode = function() {
		var rect = this.svg.createElementNS(SVG_NS, "rect");
		rect.setAttribute("x", this.x - 10);
		rect.setAttribute("y", this.y - 10);
		rect.setAttribute("rx", 5);
		rect.setAttribute("ry", 5);
		rect.setAttribute("width", this.width + 20);
		rect.setAttribute("height", this.height + 20);
		rect.setAttribute("style", "fill: #fff; stroke-width: 1px; stroke: #ccc; stroke-dasharray: 2");

		var g = this.svg.createElementNS(SVG_NS, "g");
		g.appendChild(rect);
		return g;
	};
	Y.augment(Faust.Zone, Faust.ViewComponent);
	
	Faust.Line = function(svg) {
		this.initViewComponent(svg);
	};
	Faust.Line.prototype.position = function() {
		var prev = this.previous();
		if (prev) {
			this.x = prev.x;
			this.y = prev.y + 20;
		} else {
			this.x = this.parent.x;
			this.y = this.parent.y + 10;
		}		
	},
	Faust.Line.prototype.dimension = function() {
		this.height = 20;
		this.width = 0;
		Y.each(this.children, function(c) { this.width += c.width; }, this);
	};
	Faust.Line.prototype.createSvgNode = function() {
		var line = this.svg.createElementNS(SVG_NS, "g");
		return line;
	};
	Y.augment(Faust.Line, Faust.ViewComponent);
	
	Faust.Text = function(svg, text, textAttrs) {
		this.initViewComponent(svg);
		this.text = text.replace(/\s+/g, "\u00a0");
		this.textAttrs = textAttrs;
	};
	Faust.Text.prototype.dimension = function() {		
		var measured = Faust.Text.measure(this);
		this.width = measured.width;
		this.height = measured.height;		
	};
	Faust.Text.measure = function(text) {
		var measureText = text.svg.createElementNS(SVG_NS, "text");
		text.setStyles(measureText);
		measureText.setAttribute("x", "-10000");
		measureText.setAttribute("y", "-10000");
		measureText.appendChild(text.svg.createTextNode(text.text));
		
		text.svg.documentElement.appendChild(measureText);
		var bbox = measureText.getBBox();
		text.svg.documentElement.removeChild(measureText);
		return { width: Math.round(bbox.width), height: Math.round(bbox.height)};		
	},
	Faust.Text.prototype.position = function() {
		var prev = this.previous();
		if (prev) {
			this.x = prev.x + prev.width;
			this.y = prev.y;
		} else {
			this.x = this.parent.x;
			this.y = this.parent.y;
		}
	};
	Faust.Text.prototype.createSvgNode = function() {
		var text = this.svg.createElementNS(SVG_NS, "text");
		this.setStyles(text);
		text.setAttribute("x", this.x.toString());
		text.setAttribute("y", this.y.toString());
		text.appendChild(this.svg.createTextNode(this.text));
		return text;
	};
	Faust.Text.prototype.computeStyles = function() {
		var styles = { "font-size": "11pt" };
		for (attr in this.textAttrs) {
			if (attr == "hand") {
				var hand = this.textAttrs["hand"];
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
			} else if (attr == "rewrite") {
				styles["font-weight"] = "bold";
			} else if (attr == "under") {
				styles["opacity"] = "0.5";
			} else if (attr == "over") {
				styles["font-weight"] = "bold";
			} else if (attr == "size") {
				var size = this.textAttrs["size"];
				if (size == "small") {
					styles["font-size"] = "9pt";
				}
			}
		}
		return styles;
	};
	Y.augment(Faust.Text, Faust.ViewComponent);
	
	Faust.GLine = function(svg) {
		this.initViewComponent(svg);
	};
	Faust.GLine.prototype.dimension = function() {
		this.width = 40;
		this.height = 20;
	};
	Faust.GLine.prototype.position = function() {
		var prev = this.previous();
		if (prev) {
			if (prev instanceof Faust.Text) {
				this.x = prev.x + prev.width;
				this.y = prev.y;				
			} else {
				this.x = prev.x;
				this.y = prev.y + prev.height;
			}
		} else {
			this.x = this.parent.x;
			this.y = this.parent.y;			
		}
	};
	Faust.GLine.prototype.createSvgNode = function() {
		var line = this.svg.createElementNS(SVG_NS, "line");
		line.setAttribute("x1", this.x);
		line.setAttribute("y1", this.y - 10);
		line.setAttribute("x2", this.x + 40);
		line.setAttribute("y2", this.y - 10);
		line.setAttribute("stroke-width", 1);
		line.setAttribute("stroke", "black");
		return line;
	};
	Y.augment(Faust.GLine, Faust.ViewComponent);

	Faust.GBrace = function(svg) {
		this.initViewComponent(svg);
	};
	Faust.GBrace.prototype.dimension = function() {
		this.width = 40;
		this.height = 20;
	};
	Faust.GBrace.prototype.position = function() {
		var prev = this.previous();
		if (prev) {
			if (prev instanceof Faust.Text) {
				this.x = prev.x + prev.width;
				this.y = prev.y;				
			} else {
				this.x = prev.x;
				this.y = prev.y + prev.height;
			}
		} else {
			this.x = this.parent.x;
			this.y = this.parent.y;			
		}
	};
	Faust.GBrace.prototype.createSvgNode = function() {
		var path = this.svg.createElementNS(SVG_NS, "path");
		path.setAttribute("d", "M " + (this.x) + " " + (this.y) + " q 5,-10 20,-5 q 5,0 10,-10 q -5,0 10,10 q 10,-5 20,5");
		path.setAttribute("stroke-width", 1);
		path.setAttribute("stroke", "black");
		path.setAttribute("fill", "transparent");
		return path;
	};
	Y.augment(Faust.GBrace, Faust.ViewComponent);
});
