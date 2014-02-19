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

YUI.add('svg-utils', function (Y) {
	
	var SVG_NS = "http://www.w3.org/2000/svg";

	function qscale (degree) {
		return function (val) {
			return val * Math.pow(2, degree);
		};
	}

	function svgElement(name) {
		return Y.config.doc.createElementNS(SVG_NS, name);
	}

	function svgAttrs(element, attrs) {
		Y.Object.each(attrs, function(v, k) {
			element.setAttribute(k, v);
		});
		return element;
	}

	function hasClass(element, classValue) {
		var classTokens = element.getDOMNode().getAttribute('class').split(' ');
		return classTokens.indexOf(classValue) >= 0;
	}

	function svgStyles(element, styles) {
		Y.Object.each(styles, function(v, k) {
			element.style[k] = v;
		});
		return element;
	}

	function svg(element, attrs, styles) {
		return svgStyles(svgAttrs(
			svgElement(element), attrs || {}), styles || {});
	}

	function empty(element) {
		while (element.firstChild) {
			element.removeChild(element.firstChild);
		}
	}
	
	// An implementation of getScreenBBox, by Antoine Quint, modiefied by Moritz Wissenbach
	// http://the.fuchsia-design.com/2006/12/getting-svg-elementss-full-bounding-box.html
	function getScreenBBox(element, svgRoot) {

		// macro to create an SVGPoint object
		function createPoint (x, y) {
			var point = svgRoot.createSVGPoint();
			point.x = x;
			point.y = y;
			return point;
		}

		// macro to create an SVGRect object
		function createRect (x, y, width, height) {
			var rect = svgRoot.createSVGRect();
			rect.x = x;
			rect.y = y;
			rect.width = width;
			rect.height = height;
			return rect; 
		}

		// get the complete transformation matrix
		//var matrix = element.getTransformToElement(svgRoot);
		var matrix = svgRoot.getTransformToElement(element);
		// get the bounding box of the target element
		var box = element.getBBox();

		// create an array of SVGPoints for each corner
		// of the bounding box and update their location
		// with the transform matrix
		var corners = [];
		var point = createPoint(box.x, box.y);
		corners.push( point.matrixTransform(matrix) );
		point.x = box.x + box.width;
		point.y = box.y;
		corners.push( point.matrixTransform(matrix) );
		point.x = box.x + box.width;
		point.y = box.y + box.height;
		corners.push( point.matrixTransform(matrix) );
		point.x = box.x;
		point.y = box.y + box.height;
		corners.push( point.matrixTransform(matrix) );
		var max = createPoint(corners[0].x, corners[0].y);
		var min = createPoint(corners[0].x, corners[0].y);

		// identify the new corner coordinates of the
		// fully transformed bounding box
		for (var i = 1; i < corners.length; i++) {
			var x = corners[i].x;
			var y = corners[i].y;
			if (x < min.x) {
				min.x = x;
			}
			else if (x > max.x) {
				max.x = x;
			}
			if (y < min.y) {
				min.y = y;
			}
			else if (y > max.y) {
				max.y = y;
			}
		}
		
		// return the bounding box as an SVGRect object
		return createRect(min.x, min.y, max.x - min.x, max.y - min.y);
	}

	/**
	 * Return the bounding box of element as seen from the coordinate system given by matrix.
	 */

	function boundingBox(element, matrix) {

		// macro to create an SVGPoint object
		function createPoint (x, y) {
			var point = element.viewportElement.createSVGPoint();
			point.x = x;
			point.y = y;
			return point;
		}

		// macro to create an SVGRect object
		function createRect (x, y, width, height) {
			var rect = element.viewportElement.createSVGRect();
			rect.x = x;
			rect.y = y;
			rect.width = width;
			rect.height = height;
			return rect;
		}

		// local bounding box in local coordinates
		var box = element.getBBox();



		var inv = matrix.inverse();

		inv = inv.multiply(element.getCTM());

		// create an array of SVGPoints for each corner
		// of the bounding box and update their location
		// with the transform matrix
		var corners = [];
		var point = createPoint(box.x, box.y);
		corners.push(point.matrixTransform(inv) );
		point.x = box.x + box.width;
		point.y = box.y;
		corners.push( point.matrixTransform(inv) );
		point.x = box.x + box.width;
		point.y = box.y + box.height;
		corners.push( point.matrixTransform(inv) );
		point.x = box.x;
		point.y = box.y + box.height;
		corners.push( point.matrixTransform(inv) );
		var max = createPoint(corners[0].x, corners[0].y);
		var min = createPoint(corners[0].x, corners[0].y);

		// identify the new corner coordinates of the
		// fully transformed bounding box
		for (var i = 1; i < corners.length; i++) {
			var x = corners[i].x;
			var y = corners[i].y;
			if (x < min.x) {
				min.x = x;
			}
			else if (x > max.x) {
				max.x = x;
			}
			if (y < min.y) {
				min.y = y;
			}
			else if (y > max.y) {
				max.y = y;
			}
		}

		// return the bounding box as an SVGRect object
		return createRect(min.x, min.y, max.x - min.x, max.y - min.y);
	};

	var containingRect = function(rect1, rect2) {
		var x = Math.min(rect1.x, rect2.x);
		var y = Math.min(rect1.y, rect2.y);
		var width = Math.max(rect1.x + rect1.width, rect2.x + rect2.width) - x;
		var height = Math.max(rect1.y + rect1.height, rect2.y + rect2.height) - y;
		return {x: x, y: y, width: width, height: height};
	}


	Y.mix(Y.namespace("SvgUtils"), {
		SVG_NS: SVG_NS,
		boundingBox: boundingBox,
		qscale: qscale,
		svgElement: svgElement,
		svgAttrs: svgAttrs,
		hasClass: hasClass,
		svgStyles: svgStyles,
		svg: svg,
		empty: empty,
		getScreenBBox: getScreenBBox,
		containingRect: containingRect
	});
	
}, '0.0', {
	requires: []
});