YUI.add('transcript', function (Y) {

	Faust.ENC_EXC_PREF = "ENCODING ERROR: ";
	
	Faust.ViewComponent = function() {
		this.classes = [];
		this.initViewComponent();
	};
	Faust.ViewComponent.prototype = {
		rotation: 0,
		elementName: '',
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
			//this.parent = null;
			this.pos = -1;
			this.children = [];			
			
			this.x = 0;
			this.y = 0;
			this.width = 0;
			this.height = 0;
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
		computeClasses: function() { 
			return (this.elementName ? ['element-' + this.elementName] : []).concat(this.classes);
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

	Faust.BlockViewComponent = function() {
		Faust.BlockViewComponent.superclass.constructor.call(this);
	};

	Y.extend (Faust.BlockViewComponent, Faust.ViewComponent);

	Faust.BlockViewComponent.prototype.defaultAligns = function () {
		
		this.setAlign("hAlign", new Faust.Align(this, this.parent, this.rotX(), 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));
		
		if (this.previous())
			this.setAlign("vAlign", new Faust.Align(this, this.previous(), this.rotY(), 0, 1, Faust.Align.IMPLICIT_BY_DOC_ORDER));
		else
			this.setAlign("vAlign", new Faust.Align(this, this.parent, this.rotY(), 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));
 	};

	Faust.InlineViewComponent = function() {
		Faust.InlineViewComponent.superclass.constructor.call(this);
	};

	Y.extend (Faust.InlineViewComponent, Faust.ViewComponent);

	Faust.InlineViewComponent.prototype.defaultAligns = function () {

		this.setAlign("vAlign", new Faust.NullAlign());

		if (this.previous())
			this.setAlign("hAlign", new Faust.Align(this, this.previous(), this.rotX(), 0, 1, Faust.Align.IMPLICIT_BY_DOC_ORDER));
		else
			this.setAlign("hAlign", new Faust.Align(this, this.parent, this.rotX(), 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));
	};

	Faust.VSpace = function(height) {
		Faust.VSpace.superclass.constructor.call(this);
		this.vSpaceHeight = height;
	};

	Y.extend (Faust.VSpace, Faust.BlockViewComponent);


	Faust.Patch = function() {
		Faust.Patch.superclass.constructor.call(this);
	};

	Y.extend (Faust.Patch, Faust.BlockViewComponent);


	Faust.HSpace = function(width) {
		Faust.HSpace.superclass.constructor.call(this);
		this.hSpaceWidth = width;
	};
	
	Y.extend (Faust.HSpace, Faust.InlineViewComponent);
	
	Faust.Surface = function() {
		Faust.Surface.superclass.constructor.call(this);
	};

	Y.extend(Faust.Surface, Faust.BlockViewComponent);

	Faust.Surface.prototype.position = function() {
		this.x = 0;
		this.y = 0;
		// TODO: surface-specific layout
	};


			
	Faust.Zone = function() {
		Faust.Zone.superclass.constructor.call(this);
	};

	Y.extend(Faust.Zone, Faust.BlockViewComponent);
	
	Faust.Line = function(lineAttrs) {
		Faust.Line.superclass.constructor.call(this);
		this.lineAttrs = lineAttrs;
	};

	Y.extend(Faust.Line, Faust.ViewComponent);

	Faust.Line.prototype.dimension = function() {
	};

 	Faust.Line.prototype.defaultAligns = function () {
			
		if ("indent" in this.lineAttrs) 
 			this.setAlign("hAlign", new Faust.Align(this, this.parent, this.rotX(), 0, this.lineAttrs["indent"], Faust.Align.INDENT_ATTR));
		else if ("indentCenter" in this.lineAttrs)
			this.setAlign("hAlign", new Faust.Align(this, this.parent, this.rotX(), 0.5, this.lineAttrs["indentCenter"], Faust.Align.INDENT_CENTER_ATTR));
		else
 			this.setAlign("hAlign", new Faust.Align(this, this.parent, this.rotX(), 0, 0, Faust.Align.IMPLICIT_BY_DOC_ORDER));

 		
		if (this.previous()) {
			var yourJoint = 1.5;
			if (Y.Faust.TranscriptConfiguration.overlay === "overlay") {
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

	
	Faust.Text = function(text, textAttrs) {
		Faust.Text.superclass.constructor.call(this);
		this.text = text.replace(/\s+/g, "\u00a0");
		this.textAttrs = textAttrs;
		for (var attr in this.textAttrs) {
			if (attr == "hand") {
				var handClasses = this.computeHandClasses(this.getHand());
				this.classes = this.classes.concat(handClasses);
			} else if (attr == "rewrite") {
				this.classes.push("rewrite");
			} else if (attr == "under") {
				this.classes.push("under");
			} else if (attr == "over") {
				this.classes.push("over");
			} else if (attr == "fontsize") {
				var size = this.textAttrs["fontsize"];
				if (size == "small") {
					this.classes.push("small");
				}
			} else if (attr == 'sup') {
				this.classes.push('sup');
			} else if (attr == 'sub') {
				this.classes.push('sub');
		}

		}

	};

	Y.extend(Faust.Text, Faust.InlineViewComponent);

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

	Faust.Text.prototype.writer = function (hand) {
		return hand.split('_')[0];
	};

	Faust.Text.prototype.material = function (hand) {
		return hand.split('_')[1];
	};

	Faust.Text.prototype.script = function (hand) {
		return hand.split('_')[2];
	};

	Faust.Text.prototype.computeHandClasses = function(hand) {
		var classes = [""];
		if (this.writer(hand))
			classes.push("hand-" + this.writer(hand));
		if (this.material(this.getHand()))
			classes.push("material-" + this.material(hand));
		if (this.script(this.getHand()))
			classes.push("script-" + this.script(hand));
		return classes;
	};

	Faust.SpanningVC = function(type, imageUrl, imageWidth, imageHeight, fixedWidth, fixedHeight) {
		Faust.SpanningVC.superclass.constructor.call(this);
		this.type =  type;
		this.imageUrl = imageUrl;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.fixedWidth = fixedWidth;
		this.fixedHeight = fixedHeight;
	};

	Y.extend (Faust.SpanningVC, Faust.ViewComponent);

	Faust.InlineGraphic = function(type, imageUrl, imageWidth, imageHeight, displayWidth, displayHeight) {
		Faust.InlineGraphic.superclass.constructor.call(this);
		this.type =  type;
		this.imageUrl = imageUrl;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.displayWidth = displayWidth;
		this.displayHeight = displayHeight;
	};

	Y.extend (Faust.InlineGraphic, Faust.InlineViewComponent);

	Faust.GLine = function() {
		Faust.GLine.superclass.constructor.call(this);
	};

	Y.extend(Faust.GLine, Faust.ViewComponent);

	Faust.GLine.prototype.dimension = function() {
		this.width = 40;
		this.height = 20;
	};


	Faust.GBrace = function() {
		Faust.GBrace.superclass.constructor.call(this);
	};

	Y.extend(Faust.GBrace, Faust.ViewComponent);

	Faust.GBrace.prototype.dimension = function() {
		this.width = 40;
		this.height = 20;
	};

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
	Faust.Align.INDENT_ATTR = 7;
	Faust.Align['8'] = 'INDENT_CENTER_ATTR';
	Faust.Align.EXPLICIT = 10;	
	Faust.Align['10'] = 'EXPLICIT';
	Faust.Align.MAIN_ZONE = 15;	
	Faust.Align['15'] = 'MAIN_ZONE';

	
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
	};

	Faust.NullAlign = function (priority) {
		this.priority = priority;
	};

	Faust.NullAlign.prototype.align = function() {
	};

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
}, '0.0', {
	requires: ["oop", "dump", "materialunit"]
});
