/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

YUI.add('transcript', function (Y) {

	Y.mix(Y.namespace("FaustTranscript"), {
		ENCODING_EXCEPTION_PREFIX : "ENCODING ERROR: "
	});

	Y.FaustTranscript.ViewComponent = function() {
		this.classes = [];
		this.initViewComponent();
	};
	Y.FaustTranscript.ViewComponent.prototype = {
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
			var dimensions = new Y.FaustTranscript.Dimensions();
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

			this.setAlign("vAlign", new Y.FaustTranscript.Align(this, this.parent, this.rotY(), 0, 0, Y.FaustTranscript.Align.IMPLICIT_BY_DOC_ORDER));

			if (this.previous())
				this.setAlign("hAlign", new Y.FaustTranscript.Align(this, this.previous(), this.rotX(), 0, 1, Y.FaustTranscript.Align.IMPLICIT_BY_DOC_ORDER));
			else
				this.setAlign("hAlign", new Y.FaustTranscript.Align(this, this.parent, this.rotX(), 0, 0, Y.FaustTranscript.Align.IMPLICIT_BY_DOC_ORDER));

		},
		setAlign: function (name, align) {
			if (this[name]) {
				
				if (align.priority === this[name].priority){
					var xmlId = this.xmlId ? this.xmlId : '';
					throw(Y.FaustTranscript.ENCODING_EXCEPTION_PREFIX + "Conflicting alignment instructions for element "
							+ this.elementName + " #" + xmlId + " (" + name + ", " 
							+ Y.FaustTranscript.Align[align.priority] + " )");
				}
				else if (align.priority > this[name].priority)
					this[name] = align;
			}
			else 
				this[name] = align;
		}
	};

	Y.FaustTranscript.BlockViewComponent = function() {
		Y.FaustTranscript.BlockViewComponent.superclass.constructor.call(this);
	};

	Y.extend (Y.FaustTranscript.BlockViewComponent, Y.FaustTranscript.ViewComponent);

	Y.FaustTranscript.BlockViewComponent.prototype.defaultAligns = function () {
		
		this.setAlign("hAlign", new Y.FaustTranscript.Align(this, this.parent, this.rotX(), 0, 0, Y.FaustTranscript.Align.IMPLICIT_BY_DOC_ORDER));
		
		if (this.previous())
			this.setAlign("vAlign", new Y.FaustTranscript.Align(this, this.previous(), this.rotY(), 0, 1, Y.FaustTranscript.Align.IMPLICIT_BY_DOC_ORDER));
		else
			this.setAlign("vAlign", new Y.FaustTranscript.Align(this, this.parent, this.rotY(), 0, 0, Y.FaustTranscript.Align.IMPLICIT_BY_DOC_ORDER));
	};

	Y.FaustTranscript.InlineViewComponent = function() {
		Y.FaustTranscript.InlineViewComponent.superclass.constructor.call(this);
	};

	Y.extend (Y.FaustTranscript.InlineViewComponent, Y.FaustTranscript.ViewComponent);

	Y.FaustTranscript.InlineViewComponent.prototype.defaultAligns = function () {

		this.setAlign("vAlign", new Y.FaustTranscript.NullAlign());

		if (this.previous())
			this.setAlign("hAlign", new Y.FaustTranscript.Align(this, this.previous(), this.rotX(), 0, 1, Y.FaustTranscript.Align.IMPLICIT_BY_DOC_ORDER));
		else
			this.setAlign("hAlign", new Y.FaustTranscript.Align(this, this.parent, this.rotX(), 0, 0, Y.FaustTranscript.Align.IMPLICIT_BY_DOC_ORDER));
	};

	Y.FaustTranscript.VSpace = function(height) {
		Y.FaustTranscript.VSpace.superclass.constructor.call(this);
		this.vSpaceHeight = height;
	};

	Y.extend (Y.FaustTranscript.VSpace, Y.FaustTranscript.BlockViewComponent);


	Y.FaustTranscript.Patch = function() {
		Y.FaustTranscript.Patch.superclass.constructor.call(this);
	};

	Y.extend (Y.FaustTranscript.Patch, Y.FaustTranscript.BlockViewComponent);


	Y.FaustTranscript.HSpace = function(width) {
		Y.FaustTranscript.HSpace.superclass.constructor.call(this);
		this.hSpaceWidth = width;
	};
	
	Y.extend (Y.FaustTranscript.HSpace, Y.FaustTranscript.InlineViewComponent);

	Y.FaustTranscript.Surface = function() {
		Y.FaustTranscript.Surface.superclass.constructor.call(this);
	};

	Y.extend(Y.FaustTranscript.Surface, Y.FaustTranscript.BlockViewComponent);

	Y.FaustTranscript.Surface.prototype.position = function() {
		this.x = 0;
		this.y = 0;
		// TODO: surface-specific layout
	};


			
	Y.FaustTranscript.Zone = function() {
		Y.FaustTranscript.Zone.superclass.constructor.call(this);
		this.floats = [];
	};

	Y.extend(Y.FaustTranscript.Zone, Y.FaustTranscript.BlockViewComponent);

	Y.FaustTranscript.Zone.prototype.addFloat = function (vc) {
		vc.parent = this;
		vc.pos = this.children.length;
		this.floats.push(vc);
		vc.defaultAligns();
		return vc;
	};

	Y.FaustTranscript.Zone.prototype.layout = function () {
		Y.FaustTranscript.Zone.superclass.layout.call(this);
		Y.each(this.floats, function (float) {
			float.layout();
		});
	};
	
	Y.FaustTranscript.Line = function(lineAttrs) {
		Y.FaustTranscript.Line.superclass.constructor.call(this);
		this.lineAttrs = lineAttrs;
	};

	Y.extend(Y.FaustTranscript.Line, Y.FaustTranscript.ViewComponent);

	Y.FaustTranscript.Line.prototype.dimension = function() {
	};

	Y.FaustTranscript.Line.prototype.previous = function() {
			if (this.parent == null || this.pos <= 0)
				return null;
			pre = this.parent.children[this.pos - 1];

			if (typeof pre.lineAttrs !== 'undefined' && pre.lineAttrs['interline'] === true)
				return pre.previous()
			else
				return pre
		},


	Y.FaustTranscript.Line.prototype.defaultAligns = function () {
			
		if ("indent" in this.lineAttrs) 
			this.setAlign("hAlign", new Y.FaustTranscript.Align(this, this.parent, this.rotX(), 0, this.lineAttrs["indent"], Y.FaustTranscript.Align.INDENT_ATTR));
		else if ("indentCenter" in this.lineAttrs)
			this.setAlign("hAlign", new Y.FaustTranscript.Align(this, this.parent, this.rotX(), 0.5, this.lineAttrs["indentCenter"], Y.FaustTranscript.Align.INDENT_CENTER_ATTR));
		else
			this.setAlign("hAlign", new Y.FaustTranscript.Align(this, this.parent, this.rotX(), 0, 0, Y.FaustTranscript.Align.IMPLICIT_BY_DOC_ORDER));


		if (this.previous()) {
			var yourJoint = this.lineAttrs['interline'] ? 0.75 : 1.5;
			if (Y.Faust.TranscriptConfiguration.overlay === "overlay") {
				//yourJoint = ("between" in this.lineAttrs)? 1 : 1;				
				yourJoint = ("over" in this.lineAttrs)? 0.1 : yourJoint;
			}
			else {
				yourJoint = ("between" in this.lineAttrs)? 0.7 : yourJoint;
				yourJoint = ("over" in this.lineAttrs)? 0.5 : yourJoint;
			}
									
			this.setAlign("vAlign", new Y.FaustTranscript.Align(this, this.previous(), this.rotY(), 0, yourJoint, Y.FaustTranscript.Align.IMPLICIT_BY_DOC_ORDER));
		}
		else
			this.setAlign("vAlign", new Y.FaustTranscript.Align(this, this.parent, this.rotY(), 0, 0, Y.FaustTranscript.Align.IMPLICIT_BY_DOC_ORDER));
	};

	Y.FaustTranscript.Text = function(text) {
		Y.FaustTranscript.Text.superclass.constructor.call(this);
		this.decorations = [];
		this.text = text.replace(/\s+/g, "\u00a0");
		this.textElement = null;
	};

	Y.extend(Y.FaustTranscript.Text, Y.FaustTranscript.InlineViewComponent);

	Y.FaustTranscript.Text.prototype.dimension = function() {
		var measured = this.measure();
		this.width = measured.width;
		this.height = measured.height;
	};

	Y.FaustTranscript.FloatVC = function(classes, floatParent) {
		Y.FaustTranscript.FloatVC.superclass.constructor.call(this);
		this.classes = this.classes.concat(classes);
		this.floatParent = floatParent;
	};

	Y.extend (Y.FaustTranscript.FloatVC, Y.FaustTranscript.ViewComponent);

	Y.FaustTranscript.FloatVC.prototype.globalRotation = function() {
		// Floats are always global
		return this.rotation;
	};

	Y.FaustTranscript.CoveringImage = function(type, classes, imageUrl, fixedWidth, fixedHeight, floatParent) {
		Y.FaustTranscript.CoveringImage.superclass.constructor.call(this, classes, floatParent);
		this.type =  type;
		this.imageUrl = imageUrl;
		this.fixedWidth = fixedWidth;
		this.fixedHeight = fixedHeight;
		this.coveredVCs = [];
		this.classes.push('use-image');
	};

	Y.extend (Y.FaustTranscript.CoveringImage, Y.FaustTranscript.FloatVC);

	Y.FaustTranscript.StretchingImage = function(type, classes, imageUrl, fixedWidth, fixedHeight, floatParent) {
			Y.FaustTranscript.StretchingImage.superclass.constructor.call(this, classes, floatParent);
			this.type =  type;
			this.imageUrl = imageUrl;
			this.fixedWidth = fixedWidth;
			this.fixedHeight = fixedHeight;
			this.coveredVCs = [];
			this.classes.push('use-image');
	};

	Y.extend(Y.FaustTranscript.StretchingImage, Y.FaustTranscript.FloatVC);


	Y.FaustTranscript.SpanningVC = function(type, imageUrl, imageWidth, imageHeight, fixedWidth, fixedHeight) {
		Y.FaustTranscript.SpanningVC.superclass.constructor.call(this);
		this.type =  type;
		this.imageUrl = imageUrl;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.fixedWidth = fixedWidth;
		this.fixedHeight = fixedHeight;
		this.classes.push('use-image');
	};

	Y.extend (Y.FaustTranscript.SpanningVC, Y.FaustTranscript.ViewComponent);

	Y.FaustTranscript.InlineDecoration = function(classes) {
		Y.FaustTranscript.InlineDecoration.superclass.constructor.call(this);
		this.classes = this.classes.concat(classes);
		this.classes.push('inline-decoration');
	};

	Y.extend (Y.FaustTranscript.InlineDecoration, Y.FaustTranscript.InlineViewComponent);

	Y.FaustTranscript.RectInlineDecoration = function(classes) {
		Y.FaustTranscript.RectInlineDecoration.superclass.constructor.call(this);
		this.classes.push('inline-decoration-type-rect');
	};

	Y.extend (Y.FaustTranscript.RectInlineDecoration, Y.FaustTranscript.InlineDecoration);

	Y.FaustTranscript.CircleInlineDecoration = function(classes) {
		Y.FaustTranscript.CircleInlineDecoration.superclass.constructor.call(this);
		this.classes.push('inline-decoration-type-circle');
	};

	Y.extend (Y.FaustTranscript.CircleInlineDecoration, Y.FaustTranscript.InlineDecoration);


	Y.FaustTranscript.InlineGraphic = function(type, imageUrl, imageWidth, imageHeight, displayWidth, displayHeight) {
		Y.FaustTranscript.InlineGraphic.superclass.constructor.call(this);
		this.type =  type;
		this.imageUrl = imageUrl;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.displayWidth = displayWidth;
		this.displayHeight = displayHeight;
		this.classes.push('use-image');
	};

	Y.extend (Y.FaustTranscript.InlineGraphic, Y.FaustTranscript.InlineViewComponent);

	Y.FaustTranscript.GLine = function() {
		Y.FaustTranscript.GLine.superclass.constructor.call(this);
	};

	Y.extend(Y.FaustTranscript.GLine, Y.FaustTranscript.ViewComponent);

	Y.FaustTranscript.GLine.prototype.dimension = function() {
		this.width = 40;
		this.height = 20;
	};


	Y.FaustTranscript.GBrace = function() {
		Y.FaustTranscript.GBrace.superclass.constructor.call(this);
	};

	Y.extend(Y.FaustTranscript.GBrace, Y.FaustTranscript.ViewComponent);

	Y.FaustTranscript.GBrace.prototype.dimension = function() {
		this.width = 40;
		this.height = 20;
	};

	Y.FaustTranscript.TextDecoration = function(text, classes) {
		this.text = text;
		this.classes = classes;
		this.classes.push('text-decoration');
	};

	Y.FaustTranscript.TextDecoration.prototype.layout = function() {};

	Y.FaustTranscript.NullDecoration = function (text, classes, name) {
		Y.FaustTranscript.NullDecoration.superclass.constructor.call(this, text, classes.concat(['text-decoration-type-' + name]));
	};
	Y.extend(Y.FaustTranscript.NullDecoration, Y.FaustTranscript.TextDecoration);

	Y.FaustTranscript.LineDecoration = function (text, classes, name, yOffset) {
		Y.FaustTranscript.LineDecoration.superclass.constructor.call(this, text, classes.concat(['text-decoration-type-' + name]));
		this.yOffset = yOffset;
	};
	Y.extend(Y.FaustTranscript.LineDecoration, Y.FaustTranscript.TextDecoration);

	Y.FaustTranscript.CloneDecoration = function (text, classes, name, xOffset, yOffset) {
		Y.FaustTranscript.CloneDecoration.superclass.constructor.call(this, text, classes.concat(['text-decoration-type-' + name]));
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	};
	Y.extend(Y.FaustTranscript.CloneDecoration, Y.FaustTranscript.TextDecoration);

	Y.FaustTranscript.Align = function(me, you, coordRotation, myJoint, yourJoint, priority) {
		this.me = me;
		this.you = you;
		this.coordRotation = coordRotation;
		this.myJoint = myJoint;
		this.yourJoint = yourJoint;
		this.priority = priority;
	};
	
	Y.FaustTranscript.Align.IMPLICIT_BY_DOC_ORDER = 0;
	Y.FaustTranscript.Align['0'] = 'IMPLICIT_BY_DOC_ORDER';
	Y.FaustTranscript.Align.REND_ATTR = 5;
	Y.FaustTranscript.Align['5'] = 'REND_ATTR';
	Y.FaustTranscript.Align.INDENT_ATTR = 7;
	Y.FaustTranscript.Align['7'] = 'INDENT_ATTR';
	Y.FaustTranscript.Align.INDENT_ATTR = 7;
	Y.FaustTranscript.Align['8'] = 'INDENT_CENTER_ATTR';
	Y.FaustTranscript.Align.EXPLICIT = 10;
	Y.FaustTranscript.Align['10'] = 'EXPLICIT';
	Y.FaustTranscript.Align.MAIN_ZONE = 15;
	Y.FaustTranscript.Align['15'] = 'MAIN_ZONE';

	
	Y.FaustTranscript.Align.prototype.align = function() {
		var value = this.you.getCoord(this.coordRotation);
		value -= this.myJoint * this.me.getExt(this.coordRotation);
		value += this.yourJoint * this.you.getExt(this.coordRotation);
		this.me.setCoord(value, this.coordRotation);
	};

	Y.FaustTranscript.AbsoluteAlign = function (me, coordRotation, coordinate, priority) {
		this.me = me;
		this.coordRotation = coordRotation;
		this.coordinate = coordinate;
		this.priority = priority;
	};
	
	Y.FaustTranscript.AbsoluteAlign.prototype.align = function() {
		this.me.setCoord(this.coordinate, this.coordRotation);
	};

	Y.FaustTranscript.NullAlign = function (priority) {
		this.priority = priority;
	};

	Y.FaustTranscript.NullAlign.prototype.align = function() {
	};

	Y.FaustTranscript.Dimensions = function() {};

	Y.FaustTranscript.Dimensions.prototype = function() {};
	
	Y.FaustTranscript.Dimensions.prototype.update = function(xMin, yMin, xMax, yMax) {

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
