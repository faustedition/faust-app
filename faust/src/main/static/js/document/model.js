
Faust.YUI().use("oop", "dump", function(Y) {
	Faust.ENC_EXC_PREF = "ENCODING ERROR: ";
	
	Faust.ViewComponent = function() {};
	Faust.ViewComponent.prototype = {
		rotation: 0,
		globalRotation: function () {
			var e = this;
			var result = 0;
			while (e.parent) {
				result += e.rotation;
				e = e.parent;
			}
			result += e.rotation;
			return result;
		},
		initViewComponent: function() {
			this.parent = null;
			this.pos = -1;
			this.children = [];			
			
			this.x = null;
			this.y = null;
			this.width = null;
			this.height = null;
			//this.hAlign = null;
			//this.vAlign = null;
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
			
			this.onRelayout();

			return dimensions;
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
		setStyles: function(view) {
			var styles = this.computeStyles();
			if (styles) {
				var stylesStr = "";
				for (style in styles) {
					stylesStr += (stylesStr.length == 0 ? "" : "; ") + (style + ": " + styles[style]);
				}
				view.setAttribute("style", stylesStr);
			}			
		},
		rotX: function() {return 0 + this.globalRotation()},
		rotY: function() {return 90 + this.globalRotation()},
		
 		defaultAligns: function () {
 		
 			this.setAlign("vAlign", new Faust.Align(this, this.parent, this.rotY(), 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));
 			
 			if (this.previous())
 				this.setAlign("hAlign", new Faust.Align(this, this.previous(), this.rotX(), 0, 1, Faust.Align.IMPLICIT_BY_DOC_ORDER));
 			else
 				this.setAlign("hAlign", new Faust.Align(this, this.parent, this.rotX(), 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));

		},
		setAlign: function (name, align) {
			if (this[name]) {
				
				if (align.priority === this[name].priority){
					var xmlId = this.xmlId ? this.xmlId : '';
					throw(Faust.ENC_EXC_PREF + "Conflicting alignment instructions for element " 
							+ this.elementName + " #" + xmlId + " (" + name + ", " 
							+ Faust.Align[align.priority] + " )"); 
				}
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

	Y.augment (Faust.DefaultVC, Faust.ViewComponent);

	Faust.BreakingVC = function() {
		this.initViewComponent();		
	};
	
//	Faust.BreakingVC.prototype.render = function() {
//
//		Y.each(this.children, function(c) { c.render(); });
//	};
 	
	Faust.BreakingVC.prototype.defaultAligns = function () {
		
			this.setAlign("hAlign", new Faust.Align(this, this.parent, this.rotX(), 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));
		
		if (this.previous())
			this.setAlign("vAlign", new Faust.Align(this, this.previous(), this.rotY(), 0, 1, Faust.Align.IMPLICIT_BY_DOC_ORDER));
		else
			this.setAlign("vAlign", new Faust.Align(this, this.parent, this.rotY(), 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));
 	};
	Y.augment (Faust.BreakingVC, Faust.ViewComponent);
	
	Faust.VSpace = function(height) {
		this.initViewComponent();
		this.vSpaceHeight = height;
	};

	Y.augment (Faust.VSpace, Faust.BreakingVC);

	Faust.HSpace = function(width) {
		this.initViewComponent();
		this.hSpaceWidth = width;
	};
	
	Y.augment (Faust.HSpace, Faust.ViewComponent);
	
	Faust.Surface = function() {
		this.initViewComponent();
	};
	Faust.Surface.prototype.position = function() {
		this.x = 0;
		this.y = 0;
		// TODO: surface-specific layout
	};

	Y.augment(Faust.Surface, Faust.ViewComponent);
			
	Faust.Zone = function() {
		this.initViewComponent();
	};
	
	Y.augment(Faust.Zone, Faust.ViewComponent);
	
	Faust.Line = function(lineAttrs) {
		this.initViewComponent();
		this.lineAttrs = lineAttrs;
	};
	Faust.Line.prototype.dimension = function() {
		//this.height = 20;
		//this.width = 0;
		//Y.each(this.children, function(c) { this.width += c.width; }, this);
	};
 	Faust.Line.prototype.defaultAligns = function () {
			
 		if ("center" in this.lineAttrs) 
 			this.setAlign("hAlign", new Faust.Align(this, this.parent, this.rotX(), 0.5, 0.5, Faust.Align.REND_ATTR));
 		else if ("indent" in this.lineAttrs) 
 			this.setAlign("hAlign", new Faust.Align(this, this.parent, this.rotX(), 0, this.lineAttrs["indent"], Faust.Align.INDENT_ATTR));
		else
 			this.setAlign("hAlign", new Faust.Align(this, this.parent, this.rotX(), 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));

 		
		if (this.previous()) {
			var yourJoint = 1.5;
			if (Faust.LayoutPreferences.overlay === "overlay") {
				//yourJoint = ("between" in this.lineAttrs)? 1 : 1;				
				yourJoint = ("over" in this.lineAttrs)? 0.1 : yourJoint;
			}
			else {
				yourJoint = ("between" in this.lineAttrs)? 0.7 : yourJoint;
				yourJoint = ("over" in this.lineAttrs)? 0.5 : yourJoint;
			}
									
			this.setAlign("vAlign", new Faust.Align(this, this.previous(), this.rotY(), 0, yourJoint, Faust.Align.IMPLICIT_BY_DOC_ORDER));
		}
		else
			this.setAlign("vAlign", new Faust.Align(this, this.parent, this.rotY(), 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));
	};
	Y.augment(Faust.Line, Faust.ViewComponent);
	
	Faust.Text = function(text, textAttrs) {
		this.initViewComponent();
		this.text = text.replace(/\s+/g, "\u00a0");
		this.textAttrs = textAttrs;
	};
	Faust.Text.prototype.dimension = function() {		
		var measured = this.measure();
		this.width = measured.width;
		this.height = measured.height;		
	};
	Faust.Text.prototype.getHand = function() {
		if (this.textAttrs.rewrite)
			return this.textAttrs.rewrite;
		else
			return this.textAttrs["hand"];
	};
	Faust.Text.prototype.handColor = function() {
		var hand = this.getHand();
		if (hand.indexOf("_bl") >= 0) {
			return "darkgrey";
		} else if (hand.indexOf("_t") >= 0) {
			return "black";
		} else {
			return "black";
		}
	};
	Faust.Text.prototype.computeStyles = function() {
		var styles = { "font-size": "11pt" };
		for (attr in this.textAttrs) {
			if (attr == "hand") {
				styles["fill"] = this.handColor();
				var hand = this.getHand();
				if (hand.indexOf("g_") >= 0) {
					// TODO temp solution, Firefox can only display italics,
					// Webkit only small-caps
					//styles["font-variant"] = "small-caps";
					//styles["font-style"] = "italic";
					styles["font-family"] = "faust-serif";
				} else if (hand.indexOf("xx_") >= 0) {
					styles["font-family"] = "faust-monospace";
				} else {
					styles["font-family"] = "faust-sans-serif";
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
	
	Faust.GrLine = function() {
		this.initViewComponent();
	};
	
	Y.augment (Faust.GrLine, Faust.ViewComponent);
	
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
	Y.augment(Faust.GBrace, Faust.ViewComponent);

	Faust.Align = function(me, you, coordRotation, myJoint, yourJoint, priority) {
		this.me = me;
		this.you = you;
		this.coordRotation = coordRotation;
		this.myJoint = myJoint;
		this.yourJoint = yourJoint;
		this.priority = priority;
	};
	
	Faust.Align.IMPLICIT_BY_DOC_ORDER = 0;
	Faust.Align['0'] = 'IMPLICIT_BY_DOC_ORDER';
	Faust.Align.REND_ATTR = 5;
	Faust.Align['5'] = 'REND_ATTR';
	Faust.Align.INDENT_ATTR = 7;
	Faust.Align['7'] = 'INDENT_ATTR';
	Faust.Align.EXPLICIT = 10;	
	Faust.Align['10'] = 'EXPLICIT';

	
	Faust.Align.prototype.align = function() {
		var value = this.you.getCoord(this.coordRotation);
		value -= this.myJoint * this.me.getExt(this.coordRotation);
		value += this.yourJoint * this.you.getExt(this.coordRotation);
		this.me.setCoord(value, this.coordRotation);
	};

	Faust.AbsoluteAlign = function (me, coordRotation, coordinate, priority) {
		this.me = me;
		this.coordRotation = coordRotation;
		this.coordinate = coordinate;
		this.priority = priority;
	};
	
	Faust.AbsoluteAlign.prototype.align = function() {
		this.me.setCoord(this.coordinate, this.coordRotation);
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
