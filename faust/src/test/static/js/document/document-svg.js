YUI({gallery: 'gallery-2010.10.15-19-32'}).add ('document-test-svg', function (Y) {

	Y.namespace('faust.test.document');

	// TEST SETUP
	
	Y.faust.test.document.canvasSetUp = function() {

		var testRoot = document.getElementById('test_root');
		//var container = document.getElementById('transcript_container');
		// Faust.DocumentTranscriptCanvas.prototype.intoView(container, svgRoot);


		var old_container = Y.one('#transcript_container');
		if (old_container)
			old_container.removeAttribute('id');
		var body = Y.one('body');
		// YUI is not good with creating SVG nodes, use DOM
		var svgns = "http://www.w3.org/2000/svg";
		var svgElement = document.createElementNS (svgns, 'svg');
		svgElement.setAttribute('width', '200');
		svgElement.setAttribute('height', '200');
		var container = document.createElementNS (svgns, 'g');
		container.setAttribute('id', 'transcript_container');
		svgElement.appendChild(container);
		body.appendChild(svgElement);

	};
	
	Y.faust.test.document.marker = function(x, y) {
		var svgns = "http://www.w3.org/2000/svg";
		var marker = document.createElementNS (svgns, 'rect');
		marker.setAttribute('x', x);
		marker.setAttribute('y', y);
		marker.setAttribute('width', '10');
		marker.setAttribute('height', '10');
		document.getElementById('transcript_container').appendChild(marker);
	};

	// TEST CASES
	
	var svgBBTest = new Y.Test.Case({
		name: 'BoundingBox SVG Implementation Test',
		setUp: Y.faust.test.document.canvasSetUp,
		//tearDown: Y.faust.test.document.canvasTearDown,
		'should calculate the appropriate bbox': function () {

			// YUI is not good with creating SVG nodes, use DOM
			var svgns = "http://www.w3.org/2000/svg";
			var container = Y.one('#transcript_container');
			var textNode = document.createTextNode('BBox me!');
			
			var bboxMe = document.createElementNS (svgns, 'rect');
			bboxMe.setAttribute('x', '50');
			bboxMe.setAttribute('y', '60');
			bboxMe.setAttribute('width', '100');
			bboxMe.setAttribute('height', '20');
			
			bboxMe.setAttribute('transform', 'rotate(-10)');
			container.appendChild(bboxMe);
			bboxMe.appendChild(textNode);

			var matrix = bboxMe.viewportElement.createSVGMatrix();
			//matrix = matrix.rotate(coordRotation);

			var drawBBox = function(id) {
				container.loadContent({
					tag: 'rect',
					x: bbox.x,
					y: bbox.y,
					width: bbox.width,
					height: bbox.height,
					stroke: 'gray',
					fill: 'none',
					id: id
				});
			}

			var bbox = Y.SvgUtils.boundingBox (bboxMe, matrix);

			drawBBox('bbox1');
			
			var aboutEqual = function (a, b) {
				var delta = 1.0;
				return Math.abs (a - b) < delta; 
			} 
			
			Y.assert(aboutEqual(bbox.x, 60), 'x1');
			Y.assert(aboutEqual(bbox.y, 33), 'y1');
			Y.assert(aboutEqual(bbox.width, 102), 'width1');
			Y.assert(aboutEqual(bbox.height, 37), 'height1');

			matrix = matrix.rotate(30);

			bbox = Y.SvgUtils.boundingBox (bboxMe, matrix);

			drawBBox('bbox2');

			var bboxDOM = Y.Node.getDOMNode(Y.one('#bbox2'));

			var transf = bboxDOM.transform.baseVal.createSVGTransformFromMatrix(matrix);
			bboxDOM.transform.baseVal.initialize(transf);
			
			Y.assert(aboutEqual(bbox.x, 77) , 'x2');
			Y.assert(aboutEqual(bbox.y, -50), 'y2');
			Y.assert(aboutEqual(bbox.width, 89), 'width2');
			Y.assert(aboutEqual(bbox.height, 80), 'height2');


		}
	});

	Y.Test.Runner.add(svgBBTest);
	
	var svgBBGTest = new Y.Test.Case({
		name: 'BoundingBox SVG Implementation Test',
		setUp: Y.faust.test.document.canvasSetUp,
		//tearDown: Y.faust.test.document.canvasTearDown,
		'should calculate the appropriate bbox': function () {

			// YUI is not good with creating SVG nodes, use DOM
			var svgns = "http://www.w3.org/2000/svg";
			var container = Y.one('#transcript_container');
			var textNode = document.createTextNode('BBox me!');
			var bboxMe = document.createElementNS (svgns, 'rect');
			bboxMe.setAttribute('x', '50');
			bboxMe.setAttribute('y', '60');
			bboxMe.setAttribute('width', '100');
			bboxMe.setAttribute('height', '20');
			
			bboxMe.setAttribute('transform', 'rotate(-10)');
			
			var g = document.createElementNS (svgns, 'g');
			
			g.appendChild(bboxMe);
			container.appendChild(g);
			
			bboxMe.appendChild(textNode);

			var matrix = bboxMe.viewportElement.createSVGMatrix();
			//matrix = matrix.rotate(coordRotation);

			var drawBBox = function(id) {
				container.loadContent({
					tag: 'rect',
					x: bbox.x,
					y: bbox.y,
					width: bbox.width,
					height: bbox.height,
					stroke: 'gray',
					fill: 'none',
					id: id
				});
			}

			var bbox = Y.SvgUtils.boundingBox (g, matrix);

			drawBBox('bboxg1');
			
			var aboutEqual = function (a, b) {
				var delta = 1.0;
				return Math.abs (a - b) < delta; 
			} 
			
			Y.assert(aboutEqual(bbox.x, 60), 'x1');
			Y.assert(aboutEqual(bbox.y, 33), 'y1');
			Y.assert(aboutEqual(bbox.width, 102), 'width1');
			Y.assert(aboutEqual(bbox.height, 37), 'height1');

			matrix = matrix.rotate(30);

			bbox = Y.SvgUtils.boundingBox (g, matrix);

			drawBBox('bboxg2');

			var bboxDOM = Y.Node.getDOMNode(Y.one('#bboxg2'));

			var transf = bboxDOM.transform.baseVal.createSVGTransformFromMatrix(matrix);
			bboxDOM.transform.baseVal.initialize(transf);
			
			Y.assert(aboutEqual(bbox.x, 68) , 'x2');
			Y.assert(aboutEqual(bbox.y, -52), 'y2');
			Y.assert(aboutEqual(bbox.width, 107), 'width2');
			Y.assert(aboutEqual(bbox.height, 83), 'height2');

		}
	});

	Y.Test.Runner.add(svgBBGTest);
}, 

'0.1', 
{requires: 'base, gallery-svg, test'});