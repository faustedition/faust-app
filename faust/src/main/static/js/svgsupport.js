SVG_NS = "http://www.w3.org/2000/svg";

Faust.YUI().use("oop", "dump", function(Y) {
	Faust.Surface.prototype.render = function(svgNode) {
		var g = svgNode.ownerDocument.createElementNS(SVG_NS, "g");
		// surface-specific layout
		svgNode.appendChild(g);
		
		for (var cc = 0; cc < this.children.length; cc++) {
			var child = this.children[cc];
			if (child instanceof Faust.Zone || child instanceof Faust.Surface) {
				child.render(g);
			}
		}
	};
	
	Faust.Zone.prototype.render = function(svgNode) {
		var g = svgNode.ownerDocument.createElementNS(SVG_NS, "g");
		// zone-specific layout
		svgNode.appendChild(g);
		
		for (var cc = 0; cc < this.children.length; cc++) {
			var child = this.children[cc];
			if (child instanceof Faust.Zone || child instanceof Faust.Line) {
				child.render(g);
			}
		}
	};
	
	Faust.Line.prototype.render = function(svgNode) {
		var text = svgNode.ownerDocument.createElementNS(SVG_NS, "text");
		text.setAttribute("x", "20");
		text.setAttribute("y", y.toString());
		y+= 20;
		
		// zone-specific layout
		svgNode.appendChild(text);
		
		for (var cc = 0; cc < this.children.length; cc++) {
			var child = this.children[cc];
			if (child instanceof Faust.Text) {
				child.render(text);
			}
		}
	};
	
	Faust.Text.prototype.render = function(svgNode) {
		var document = svgNode.ownerDocument;
		var node = this.node;
		var text = this.text;
		var tspan = document.createElementNS(SVG_NS, "tspan");
		tspan.appendChild(document.createTextNode("[" + text + "]"));
		svgNode.appendChild(tspan);
		tspan.addEventListener("click", function(e) { 
			var ancestors = node.ancestors();
			var tags = "";
			for (var ac = 0; ac < ancestors.length; ac++) {
				tags += (tags.length == 0 ? "" : ", ") + ancestors[ac].node.name;
			}
			
			var measureText = document.createElementNS(SVG_NS, "text");
			measureText.setAttribute("x", "-10000");
			measureText.setAttribute("y", "-10000");
			measureText.appendChild(document.createTextNode(text));
			document.documentElement.appendChild(measureText);
			var bbox = measureText.getBBox();
			document.documentElement.removeChild(measureText);
			
			alert("Tags: " + tags + "\n\nPixels: " + Math.round(bbox.width).toString() + "x" + Math.round(bbox.height).toString()); 
		}, false);
	};
});