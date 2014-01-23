SVG_NS = "http://www.w3.org/2000/svg";
DRAG_NS = "http://www.codedread.com/dragsvg";

YUI.add('transcript-svg', function (Y) {

	// Implementation of View Components. Keep in sync with transcript.js.
	// These components are based on SVG. The model is only
	// accessing a narrow interface to learn about the actual
	// size, so in theory this could be exchanged with say a 
	// HTML / CSS Transforms implementation

	Faust.ViewComponent.prototype.getCoord = function(coordRotation) {
		var matrix = this.view.viewportElement.createSVGMatrix();
		matrix = matrix.rotate(coordRotation);
		return Y.SvgUtils.boundingBox(this.view, matrix).x;
	};

	Faust.ViewComponent.prototype.getExt = function(coordRotation) {
		var matrix = this.view.viewportElement.createSVGMatrix();
		matrix = matrix.rotate(coordRotation);
		return Y.SvgUtils.boundingBox(this.view, matrix).width;
	};

	Faust.ViewComponent.prototype.wrap = function(view) {
		var wrapper = this.svgDocument().createElementNS(SVG_NS, "g");
		wrapper.appendChild(view);
		return wrapper;
	};

	Faust.ViewComponent.prototype.setCoord = function(coord, coordRotation) {
		
		var myRot = this.globalRotation();
		var myRotMx = this.view.viewportElement.createSVGMatrix().rotate(myRot);
		var myRotTf = this.view.viewportElement.createSVGTransformFromMatrix(myRotMx);
		var myRotTfInv = this.view.viewportElement.createSVGTransformFromMatrix(myRotMx.inverse());
		
		var matrix = this.view.viewportElement.createSVGMatrix();
		var currentCoord = this.getCoord(coordRotation);
		var deltaCoord = coord - currentCoord;
		
		matrix = matrix.rotate(coordRotation);
		matrix = matrix.translate(deltaCoord, 0);
		matrix = matrix.rotate(-coordRotation);
		var transform = this.view.viewportElement.createSVGTransformFromMatrix(matrix);
		this.view.transform.baseVal.consolidate();
		this.view.transform.baseVal.appendItem(myRotTfInv);
		this.view.transform.baseVal.appendItem(transform);
		this.view.transform.baseVal.appendItem(myRotTf);
		this.view.transform.baseVal.consolidate();
	};

	Faust.ViewComponent.prototype.rotate = function(deg) {
		var matrix = this.view.viewportElement.createSVGMatrix();
		matrix = matrix.rotate(deg);
		var transform = this.view.viewportElement.createSVGTransformFromMatrix(matrix);
		this.view.transform.baseVal.insertItemBefore(transform, 0);		
	};
	
	Faust.ViewComponent.prototype.svgContainer = function() {
		if (this.parent)
			if (this.parent.view)
				return this.parent.view;
			else
				return this.parent.svgContainer();
		else
			if (this.svgCont)
				return this.svgCont;
		else
			throw ('Cannot find SVG container for view component');
	};
	
	Faust.ViewComponent.prototype.svgDocument = function() {
		return document;
	};

	Faust.ViewComponent.prototype.onRelayout = function() {
	};
	
	Faust.ViewComponent.prototype.render = function() {
		this.view = this.createView();
		this.view.setAttribute('class', this.getClassesString());
		this.svgContainer().appendChild(this.view);
		this.rotate(this.rotation);
		Y.each(this.children, function(c) { c.render(); });
	};

	Faust.ViewComponent.prototype.measure = function() {
		var bbox = this.view.getBBox();
		return { width: Math.round(bbox.width), height: Math.round(bbox.height)};		
	};

	Faust.ViewComponent.prototype.getClassesString = function() {
		return this.computeClasses().join(' ');
	};
	
	Faust.BlockViewComponent.prototype.createView = function() {
		var view = this.svgDocument().createElementNS(SVG_NS, "g");
		return view;
	};

	Faust.InlineViewComponent.prototype.createView = function() {
		var view = this.svgDocument().createElementNS(SVG_NS, "g");
		return view;
	};

	Faust.Patch.prototype.createView = function() {
		this.patchBackground = this.svgDocument().createElementNS(SVG_NS, "rect");
		this.svgContainer().appendChild(this.patchBackground);
		this.patchBackground.setAttribute("class", "patchBackground " + this.getClassesString());
		this.view = this.svgDocument().createElementNS(SVG_NS, "g");
		var patchBox = this.view.getBBox();
		return this.view;
	}

	Faust.Patch.prototype.onRelayout = function() {
		var bbox = this.view.getBBox();
		this.patchBackground.setAttribute("x", bbox.x);
		this.patchBackground.setAttribute("y", bbox.y);
		this.patchBackground.setAttribute("height", bbox.height);
		this.patchBackground.setAttribute("width", bbox.width);
		this.patchBackground.transform.baseVal.initialize(this.view.transform.baseVal.consolidate());
	}

	Faust.VSpace.prototype.createView = function() {
		var result = this.svgDocument().createElementNS(SVG_NS, "rect");
		result.setAttribute('class', 'VSpace');
		result.setAttribute('width', '0.1em');
		result.setAttribute('style', 'visibility: hidden;');
		//TODO dynamically calculate from context line height
		var height = String(this.vSpaceHeight * 1.5) + 'em';
		result.setAttribute('height', height);
		return result;
	};

	Faust.HSpace.prototype.createView = function() {
		var result = this.svgDocument().createElementNS(SVG_NS, "rect");
		result.setAttribute('class', 'HSpace');
		result.setAttribute('height', '0.1em');
		result.setAttribute('style', 'visibility: hidden;');
		//TODO dynamically calculate from context ? 
		var width = String(this.hSpaceWidth * 0.5) + 'em';
		result.setAttribute('width', width);
		return result;
	};

	Faust.Surface.prototype.createView = function() {
		var surface = this.svgDocument().createElementNS(SVG_NS, "g");
		surface.setAttribute('class', 'Surface');
		return surface;
	};

	Faust.Zone.prototype.createView = function() {
		var result = this.svgDocument().createElementNS(SVG_NS, "g");
		var box0 = this.svgDocument().createElementNS(SVG_NS, 'rect');
		box0.setAttribute('class', 'ZoneDot');
		box0.setAttribute('width', '0.1em');
		box0.setAttribute('height', '0.1em');
		box0.setAttribute('x', '0');
		box0.setAttribute('y', '0');
		box0.setAttribute('style', 'fill: transparent; stroke: black; visibility: hidden');
		result.appendChild(box0);
		result.setAttribute('class', 'Zone');
		result.setAttributeNS(DRAG_NS, 'drag:enable', 'true');

		return result;
	};
	
	Faust.Line.prototype.createView = function() {
		var line = this.svgDocument().createElementNS(SVG_NS, "g");
		return line;
	};

	Faust.Text.prototype.createView = function() {
		// wrapper will contain text decorations
		var wrapper = this.svgDocument().createElementNS(SVG_NS, "g");
		wrapper.setAttribute('class', 'text-wrapper');
		this.textElement = this.svgDocument().createElementNS(SVG_NS, "text");
		this.textElement.setAttribute("class", "text " + this.getClassesString());
		this.textElement.appendChild(this.svgDocument().createTextNode(this.text));
		wrapper.appendChild(this.textElement);
		return wrapper;
	};
	
	Faust.Text.prototype.onRelayout = function() {

		if (this.rewrite) {
			var offset = this.measure().height / 20.0;
			this.rewrite.setAttribute("x", this.x + offset);
			this.rewrite.setAttribute("y", this.y - offset);
			this.rewrite.transform.baseVal.initialize(this.view.transform.baseVal.consolidate());
		}

		var bbox = this.view.getBBox();
		this.bgBox.setAttribute("x", bbox.x);
		this.bgBox.setAttribute("y", bbox.y);
		this.bgBox.setAttribute("height", bbox.height);
		this.bgBox.setAttribute("width", bbox.width);
		this.bgBox.transform.baseVal.initialize(this.view.transform.baseVal.consolidate());
		
		Y.each (this.decorations, function(decoration) {decoration.layout()});
	};

	Faust.Text.prototype.render = function() {

		this.bgBox = this.svgDocument().createElementNS(SVG_NS, "rect");
		this.svgContainer().appendChild(this.bgBox);
		this.bgBox.setAttribute("class", "bgBox " + this.getClassesString());

		this.view = this.createView();
		this.svgContainer().appendChild(this.view);
		var textBox = this.view.getBBox();

		if ("rewrite" in this.textAttrs) {
			this.rewrite = this.createView();
			this.svgContainer().appendChild(this.rewrite);
		}

		this.rotate(this.rotation);
		Y.each (this.decorations, function(decoration) {decoration.render()});
		Y.each(this.children, function(c) { c.render(); });
	};

	Faust.Text.prototype.getExt = function(coordRotation) {
		// only measure the text extent without the decorations
		var matrix = this.textElement.viewportElement.createSVGMatrix();
		matrix = matrix.rotate(coordRotation);
		return Y.SvgUtils.boundingBox(this.textElement, matrix).width;
	};


	Faust.SpanningVC.prototype.createView = function() {
		this.spanningRect = this.svgDocument().createElementNS(SVG_NS, 'use');
		this.spanningRect.setAttributeNS('http://www.w3.org/1999/xlink', 'href', this.imageUrl);
		this.spanningRect.setAttribute('class', this.type);
		this.spanningRect.setAttribute('x', 0);
		this.spanningRect.setAttribute('y', 0);
		this.spanningRect.setAttribute('width', this.imageWidth);
		this.spanningRect.setAttribute('height', this.imageHeight);
		this.spanningRect.setAttribute('transform', 'scale(0.000001)');


		this.svgContainer().appendChild(this.spanningRect);
		var block = this.svgDocument().createElementNS(SVG_NS, 'rect');
		block.setAttribute('width', '0.1em');
		block.setAttribute('height', '0.1em');
		block.setAttribute('style', 'fill: transparent; stroke: black; visibility: hidden');
		block.setAttribute('class', 'SpanningDot');
		return block;
	};
	
	Faust.SpanningVC.prototype.onRelayout = function() {
		var parentWidth = this.parent.getExt(this.rotX());
		var parentHeight = this.parent.getExt(this.rotY());
		var width = this.fixedWidth ? this.fixedWidth : parentWidth;
		var height = this.fixedHeight ? this.fixedHeight : parentHeight;
		this.spanningRect.setAttribute('x', (parentWidth - width) / 2);
		this.spanningRect.setAttribute('y', (parentHeight - height) / 2);

		var transform = "scale(" + width / this.imageWidth + "," + height / this.imageHeight + ")";

		//this.spanningRect.setAttribute('width', width);
		//this.spanningRect.setAttribute('height', height);
		this.spanningRect.setAttribute('transform', transform);
	};

	Faust.InlineGraphic.prototype.createView = function() {
		// FIXME: a g element must be used as a wrapper, because we cannot set the transform attribute on the element
		// returned by createView() directly (as that is overwritten by layout). TODO: wrap the output of createView
		// in a central place.
		var g = this.svgDocument().createElementNS(SVG_NS, 'g');

		this.graphic = this.svgDocument().createElementNS(SVG_NS, 'use');
		g.setAttribute('class', this.type);
		this.graphic.setAttribute('width', this.imageWidth);
		this.graphic.setAttribute('height', this.imageHeight);
		this.graphic.setAttributeNS('http://www.w3.org/1999/xlink', 'href', this.imageUrl);
		g.setAttribute('vector-effect', 'non-scaling-stroke');
		var transform = "scale(" + this.displayWidth / this.imageWidth + "," + this.displayHeight / this.imageHeight + ")";
		this.graphic.setAttribute('transform', transform);
		g.setAttribute('transform', 'translate(0, -' + this.displayHeight + ')');
		g.appendChild(this.graphic);
		return g;
	};

	Faust.GLine.prototype.createView = function() {
		var line = this.svgDocument().createElementNS(SVG_NS, "line");
		line.setAttribute("x1", this.x);
		line.setAttribute("y1", this.y - 10);
		line.setAttribute("x2", this.x + 40);
		line.setAttribute("y2", this.y - 10);
		line.setAttribute("stroke-width", 1);
		line.setAttribute("stroke", "black");
		return line;
	};

	Faust.GBrace.prototype.createView = function() {
		var path = this.svgDocument().createElementNS(SVG_NS, "path");
		path.setAttribute("d", "M " + (this.x) + " " + (this.y) + " q 5,-10 20,-5 q 5,0 10,-10 q -5,0 10,10 q 10,-5 20,5");
		path.setAttribute("stroke-width", 1);
		path.setAttribute("stroke", "black");
		path.setAttribute("fill", "transparent");
		return path;
	};

	Faust.TextDecoration.prototype.render = function() {
		this.view = this.createView();
		this.view.setAttribute('class', this.classes.join(' '));
		this.text.view.appendChild(this.view);
	};

	Faust.LineDecoration.prototype.createView = function() {
		var view = this.text.svgDocument().createElementNS(SVG_NS, "line");
		return view;
	};

	Faust.LineDecoration.prototype.layout = function() {
		var textBBox = this.text.textElement.getBBox();
		this.view.setAttribute("x1", textBBox.x);
		this.view.setAttribute("x2", textBBox.x + textBBox.width);
		var y = textBBox.height * this.yOffset;
		this.view.setAttribute("y1", y);
		this.view.setAttribute('y2', y);
	};

}, '0.0', {
	requires: ["node", "dom", "event", "transcript", "event-mouseenter",
		"stylesheet", "transition", "svg-utils"]
});
