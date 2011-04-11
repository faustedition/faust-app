SVG_NS = "http://www.w3.org/2000/svg";

Faust.YUI().use("oop", "dump", function(Y) {
	Faust.ViewComponent = function() {};
	Faust.ViewComponent.prototype = {
		initViewComponent: function() {
			this.parent = null;
			this.pos = -1;
			this.children = [];			
			
			this.x = null;
			this.y = null;
			this.width = null;
			this.height = null;
			this.hAlign = null;
			this.vAlign = null;
		},
		add: function(vc) {
			vc.parent = this;
			vc.pos = this.children.length;
			this.children.push(vc);
			vc.defaultAligns();
			return vc;
		},
		previous: function() {
			return (this.parent == null || this.pos <= 0) ? null : this.parent.children[this.pos - 1];
		},
		next: function() {
			return (this.parent == null || (this.pos + 1) >= this.parent.children.length) ? null : this.parent.children[this.pos + 1];			
		},
		svgContainer: function() {
		return this.parent == null ? this.svgDocument().getElementsByTagName("svg")[0].firstChild : this.parent.svgContainer();
		},
		svgDocument: function() {
			return document;
		},
		layout: function() {
			this.computeDimension();			
			this.computePosition();
			var dimensions = new Faust.Dimensions();
			if (this.children.length <= 0) 
				dimensions.update(this.x, this.y, this.x + this.width, this.y + this.height);
			else {
				
				Y.each(this.children, function(c) {
					//if (!c.layoutSatisfied) {
						c.layout();
						dimensions.update (c.x, c.y, c.x + c.width, c.y + c.height);
					//}
				});	
			}
			return dimensions;
		},
		render: function() {
			this.svgNode = this.createSvgNode();
			this.svgContainer().appendChild(this.svgNode);
			Y.each(this.children, function(c) { c.render(); });
		},
		checkLayoutDiff: function(old, nu) {
			var epsilon = 0.01;
			this.layoutSatisfied = this.layoutSatisfied && abs(old - nu) < epsilon;  
		},
		computeDimension: function() {
			var oldWidth = this.width;
			var oldHeight = this.height;
			//Y.each(this.children, function(c) { c.computeDimension(); });
			this.dimension();
			this.checkLayoutDiff(oldWidth, this.width);
			this.checkLayoutDiff(oldHeight, this.height);
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
			var oldX = this.x;
			var oldY = this.y;
			this.position();
			//Y.each(this.children, function(c) { c.computePosition(); });
			this.checkLayoutDiff(oldX, this.x);
			this.checkLayoutDiff(oldY, this.y);
		},
		position: function() {
			this.hAlign.align();
			this.vAlign.align();
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
		},
 		defaultAligns: function () {
 			
 			this.setAlign("vAlign", new Faust.Align(this, this.parent, "y", "height", 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));
 			
 			if (this.previous())
 				this.setAlign("hAlign", new Faust.Align(this, this.previous(), "x", "width", 0, 1, Faust.Align.IMPLICIT_BY_DOC_ORDER));
 			else
 				this.setAlign("hAlign", new Faust.Align(this, this.parent, "x", "width", 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));

		},
		setAlign: function (name, align) {
			if (this[name]) {
				if (align.priority === this[name].priority)
					throw("Conflicting alignment instructions");
				else if (align.priority > this[name].priority)
					this[name] = align;
			}
			else 
				this[name] = align;
		}
	};
	
	Faust.DefaultVC = function() {
		this.initViewComponent();		
	};
	Faust.DefaultVC.prototype.render = function() {
			Y.each(this.children, function(c) { c.render(); });
	};
	Y.augment (Faust.DefaultVC, Faust.ViewComponent);

	Faust.BreakingVC = function() {
		this.initViewComponent();		
	};
	
	Faust.BreakingVC.prototype.render = function() {

			Y.each(this.children, function(c) { c.render(); });
	};
 	
	Faust.BreakingVC.prototype.defaultAligns = function () {
		
			this.setAlign("hAlign", new Faust.Align(this, this.parent, "x", "width", 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));
		
		if (this.previous())
			this.setAlign("vAlign", new Faust.Align(this, this.previous(), "y", "height", 0, 1, Faust.Align.IMPLICIT_BY_DOC_ORDER));
		else
			this.setAlign("vAlign", new Faust.Align(this, this.parent, "y", "height", 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));
 	};
	Y.augment (Faust.BreakingVC, Faust.ViewComponent);
	
	Faust.Surface = function() {
		this.initViewComponent();
	};
	Faust.Surface.prototype.position = function() {
		this.x = 0;
		this.y = 0;
		// TODO: surface-specific layout
	};
	Faust.Surface.prototype.createSvgNode = function() {
		return this.svgDocument().createElementNS(SVG_NS, "g");
	};
	Y.augment(Faust.Surface, Faust.ViewComponent);
			
	Faust.Zone = function() {
		this.initViewComponent();
	};

	Faust.Zone.prototype.createSvgNode = function() {
		return this.svgDocument().createElementNS(SVG_NS, "g");
	};
	Y.augment(Faust.Zone, Faust.ViewComponent);
	
	Faust.Line = function(lineAttrs) {
		this.initViewComponent();
		this.lineAttrs = lineAttrs;
	};
	Faust.Line.prototype.dimension = function() {
		this.height = 20;
		this.width = 0;
		Y.each(this.children, function(c) { this.width += c.width; }, this);
	};
	Faust.Line.prototype.createSvgNode = function() {
		var line = this.svgDocument().createElementNS(SVG_NS, "g");
		return line;
	};
 	Faust.Line.prototype.defaultAligns = function () {
			
 		if ("center" in this.lineAttrs) 
 			this.setAlign("hAlign", new Faust.Align(this, this.parent, "x", "width", 0.5, 0.5, Faust.Align.IMPLICIT_BY_DOC_ORDER));
 		else if ("indent" in this.lineAttrs) 
 			this.setAlign("hAlign", new Faust.Align(this, this.parent, "x", "width", 0, this.lineAttrs["indent"], Faust.Align.IMPLICIT_BY_DOC_ORDER));
		else
 			this.setAlign("hAlign", new Faust.Align(this, this.parent, "x", "width", 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));

 		
		if (this.previous())
			this.setAlign("vAlign", new Faust.Align(this, this.previous(), "y", "height", 0, 1, Faust.Align.IMPLICIT_BY_DOC_ORDER));
		else
			this.setAlign("vAlign", new Faust.Align(this, this.parent, "y", "height", 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));
	};
	Y.augment(Faust.Line, Faust.ViewComponent);
	
	Faust.Text = function(text, textAttrs) {
		this.initViewComponent();
		this.text = text.replace(/\s+/g, "\u00a0");
		this.textAttrs = textAttrs;
	};
	Faust.Text.prototype.dimension = function() {		
		var measured = Faust.Text.measure(this);
		this.width = measured.width;
		this.height = measured.height;		
	};
//	Faust.Text.prototype.defaultAligns = function () {
//		
//	};

	Faust.Text.measure = function(text) {
		var measureText = text.svgDocument().createElementNS(SVG_NS, "text");
		text.setStyles(measureText);
		measureText.setAttribute("x", "-10000");
		measureText.setAttribute("y", "-10000");
		measureText.appendChild(text.svgDocument().createTextNode(text.text));
		
		var svgRoot = text.svgDocument().getElementsByTagName("svg")[0];
		svgRoot.appendChild(measureText);
		var bbox = measureText.getBBox();
		svgRoot.removeChild(measureText);
		return { width: Math.round(bbox.width), height: Math.round(bbox.height)};		
	};

	Faust.Text.prototype.createSvgNode = function() {
		var text = this.svgDocument().createElementNS(SVG_NS, "text");
		this.setStyles(text);
		text.setAttribute("x", this.x.toString());
		text.setAttribute("y", this.y.toString());
		text.appendChild(this.svgDocument().createTextNode(this.text));
		return text;
	};
	Faust.Text.prototype.render = function() {
		this.svgNode = this.createSvgNode();
		this.svgContainer().appendChild(this.svgNode);
		var textBox = this.svgNode.getBBox();
		if ("strikethrough" in this.textAttrs) {
			var st = this.svgDocument().createElementNS(SVG_NS, "line");
			st.setAttribute("x1", textBox.x);
			st.setAttribute("x2", textBox.x + textBox.width);
			st.setAttribute("y1", textBox.y + textBox.height / 2);
			st.setAttribute("y2", textBox.y + textBox.height / 2);
			st.setAttribute("stroke", "#333");
			this.svgContainer().appendChild(st);
		}
		if ("underline" in this.textAttrs) {
			var st = this.svgDocument().createElementNS(SVG_NS, "line");
			st.setAttribute("x1", textBox.x);
			st.setAttribute("x2", textBox.x + textBox.width);
			st.setAttribute("y1", textBox.y + textBox.height);
			st.setAttribute("y2", textBox.y + textBox.height);
			st.setAttribute("stroke", this.handColor());
			this.svgContainer().appendChild(st);
		}
		
		Y.each(this.children, function(c) { c.render(); });		
	};
	Faust.Text.prototype.handColor = function() {
		var hand = this.textAttrs["hand"];
		if (hand.indexOf("_bl") >= 0) {
			return "darkgrey";
		} else if (hand.indexOf("_t") >= 0) {
			return "sienna";
		} else {
			return "black";
		}
	};
	Faust.Text.prototype.computeStyles = function() {
		var styles = { "font-size": "11pt" };
		for (attr in this.textAttrs) {
			if (attr == "hand") {
				styles["fill"] = this.handColor();
				var hand = this.textAttrs["hand"];
				if (hand.indexOf("g_") >= 0) {
					styles["font-style"] = "italic";
				} else {
					styles["font-style"] = "normal";
				}
			} else if (attr == "rewrite") {
				styles["font-weight"] = "bold";
			} else if (attr == "under") {
				styles["opacity"] = "0.5";
			} else if (attr == "over") {
				styles["font-weight"] = "bold";
			} else if (attr == "fontsize") {
				var size = this.textAttrs["fontsize"];
				if (size == "small") {
					styles["font-size"] = "9pt";
				}
			}
		}
		return styles;
	};
	Y.augment(Faust.Text, Faust.ViewComponent);
	
	Faust.GLine = function() {
		this.initViewComponent();
	};
	Faust.GLine.prototype.dimension = function() {
		this.width = 40;
		this.height = 20;
	};
//	Faust.GLine.prototype.position = function() {
//		var prev = this.previous();
//		if (prev) {
//			if (prev instanceof Faust.Text) {
//				this.x = prev.x + prev.width;
//				this.y = prev.y;				
//			} else {
//				this.x = prev.x;
//				this.y = prev.y + prev.height;
//			}
//		} else {
//			this.x = this.parent.x;
//			this.y = this.parent.y;			
//		}
//	};
	Faust.GLine.prototype.createSvgNode = function() {
		var line = this.svgDocument().createElementNS(SVG_NS, "line");
		line.setAttribute("x1", this.x);
		line.setAttribute("y1", this.y - 10);
		line.setAttribute("x2", this.x + 40);
		line.setAttribute("y2", this.y - 10);
		line.setAttribute("stroke-width", 1);
		line.setAttribute("stroke", "black");
		return line;
	};
	Y.augment(Faust.GLine, Faust.ViewComponent);

	Faust.GBrace = function() {
		this.initViewComponent();
	};
	Faust.GBrace.prototype.dimension = function() {
		this.width = 40;
		this.height = 20;
	};
//	Faust.GBrace.prototype.position = function() {
//		var prev = this.previous();
//		if (prev) {
//			if (prev instanceof Faust.Text) {
//				this.x = prev.x + prev.width;
//				this.y = prev.y;				
//			} else {
//				this.x = prev.x;
//				this.y = prev.y + prev.height;
//			}
//		} else {
//			this.x = this.parent.x;
//			this.y = this.parent.y;			
//		}
//	};
	Faust.GBrace.prototype.createSvgNode = function() {
		var path = this.svgDocument().createElementNS(SVG_NS, "path");
		path.setAttribute("d", "M " + (this.x) + " " + (this.y) + " q 5,-10 20,-5 q 5,0 10,-10 q -5,0 10,10 q 10,-5 20,5");
		path.setAttribute("stroke-width", 1);
		path.setAttribute("stroke", "black");
		path.setAttribute("fill", "transparent");
		return path;
	};
	Y.augment(Faust.GBrace, Faust.ViewComponent);

	Faust.Align = function(me, you, coordName, extName, myJoint, yourJoint, priority) {
		this.me = me;
		this.you = you;
		this.coordName = coordName;
		this.extName = extName;
		this.myJoint = myJoint;
		this.yourJoint = yourJoint;
		this.priority = priority;
	};
	
	Faust.Align.IMPLICIT_BY_DOC_ORDER = 0;
	Faust.Align.EXPLICIT = 10;
	
	Faust.Align.prototype.align = function() {
		this.me[this.coordName] = this.you[this.coordName];
		this.me[this.coordName] -= this.myJoint * this.me[this.extName];
		this.me[this.coordName] += this.yourJoint * this.you[this.extName];
	}

	Faust.Dimensions = function() {};

	Faust.Dimensions.prototype = function() {};
	
	Faust.Dimensions.prototype.update = function(xMin, yMin, xMax, yMax) {

		if (!this.xMin || this.xMin > xMin )
			this.xMin = xMin;
		
		if (!this.yMin || this.yMin > yMin )
			this.yMin = yMin;

 		if (!this.xMax || this.xMax < xMax )
			this.xMax = xMax;

 		if (!this.yMax || this.yMax < yMax )
			this.yMax = yMax;
	}
});
