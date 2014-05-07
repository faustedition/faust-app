YUI.add('test-svg-utils', function (Y) {

	var aboutEqual = function (a, b) {
		var delta = 1.0;
		return Math.abs(a - b) < delta;
	}

	var drawBBox = function (bbox, container) {
		var rect = Y.one(Y.SvgUtils.svgElement('rect'));
		rect.setAttribute('x', bbox.x);
		rect.setAttribute('y', bbox.y);
		rect.setAttribute('width', bbox.width);
		rect.setAttribute('height', bbox.height);
		rect.setAttribute('stroke', 'gray');
		rect.setAttribute('fill', 'none');
		container.append(rect);
		return rect;
	}

	var svgUtilsTestSuite = new Y.Test.Suite('SVG Utils Test Suite');

	var svgBBTest = new Y.Test.Case({
		name: 'BoundingBox SVG Implementation Test',
		setUp: Y.FaustTest.svgCanvasSetUp,
		'should calculate the appropriate bbox': function () {

			var svgns = "http://www.w3.org/2000/svg";
			var container = this.svgCanvas;
			var textNode = document.createTextNode('BBox me!');

			var bboxMe = document.createElementNS(svgns, 'rect');
			bboxMe.setAttribute('x', '50');
			bboxMe.setAttribute('y', '60');
			bboxMe.setAttribute('width', '100');
			bboxMe.setAttribute('height', '20');

			bboxMe.setAttribute('transform', 'rotate(-10)');
			container.appendChild(bboxMe);
			bboxMe.appendChild(textNode);

			var matrix = bboxMe.viewportElement.createSVGMatrix();
			//matrix = matrix.rotate(coordRotation);

			var bbox = Y.SvgUtils.boundingBox(bboxMe, matrix);

			drawBBox(bbox, container);


			Y.assert(aboutEqual(bbox.x, 60), 'x1');
			Y.assert(aboutEqual(bbox.y, 33), 'y1');
			Y.assert(aboutEqual(bbox.width, 102), 'width1');
			Y.assert(aboutEqual(bbox.height, 37), 'height1');

			matrix = matrix.rotate(30);

			bbox = Y.SvgUtils.boundingBox(bboxMe, matrix);

			var bboxNode = drawBBox(bbox, container);

			var bboxDOM = Y.Node.getDOMNode(Y.one(bboxNode));

			var transf = bboxDOM.transform.baseVal.createSVGTransformFromMatrix(matrix);
			bboxDOM.transform.baseVal.initialize(transf);

			Y.assert(aboutEqual(bbox.x, 77), 'x2');
			Y.assert(aboutEqual(bbox.y, -50), 'y2');
			Y.assert(aboutEqual(bbox.width, 89), 'width2');
			Y.assert(aboutEqual(bbox.height, 80), 'height2');


		}
	});

	svgUtilsTestSuite.add(svgBBTest);


	var svgBBGTest = new Y.Test.Case({
		name: 'BoundingBox SVG Implementation Test',
		setUp: Y.FaustTest.svgCanvasSetUp,
		'should calculate the appropriate bbox': function () {

			var svgns = "http://www.w3.org/2000/svg";
			var container = this.svgCanvas;
			var textNode = document.createTextNode('BBox me!');
			var bboxMe = document.createElementNS(svgns, 'rect');
			bboxMe.setAttribute('x', '50');
			bboxMe.setAttribute('y', '60');
			bboxMe.setAttribute('width', '100');
			bboxMe.setAttribute('height', '20');

			bboxMe.setAttribute('transform', 'rotate(-10)');

			var g = document.createElementNS(svgns, 'g');

			g.appendChild(bboxMe);
			container.appendChild(g);

			bboxMe.appendChild(textNode);

			var matrix = bboxMe.viewportElement.createSVGMatrix();
			//matrix = matrix.rotate(coordRotation);


			var bbox = Y.SvgUtils.boundingBox(g, matrix);

			drawBBox(bbox, container);

			Y.assert(aboutEqual(bbox.x, 60), 'x1');
			Y.assert(aboutEqual(bbox.y, 33), 'y1');
			Y.assert(aboutEqual(bbox.width, 102), 'width1');
			Y.assert(aboutEqual(bbox.height, 37), 'height1');

			matrix = matrix.rotate(30);

			bbox = Y.SvgUtils.boundingBox(g, matrix);

			var bboxNode = drawBBox(bbox, container);

			var bboxDOM = Y.Node.getDOMNode(Y.one(bboxNode));

			var transf = bboxDOM.transform.baseVal.createSVGTransformFromMatrix(matrix);
			bboxDOM.transform.baseVal.initialize(transf);

			Y.assert(aboutEqual(bbox.x, 68), 'x2');
			Y.assert(aboutEqual(bbox.y, -52), 'y2');
			Y.assert(aboutEqual(bbox.width, 107), 'width2');
			Y.assert(aboutEqual(bbox.height, 83), 'height2');

		}
	});


	svgUtilsTestSuite.add(svgBBGTest);


	var fitToTest = new Y.Test.Case({
		name: 'Fit To Test',
		setUp: Y.FaustTest.svgCanvasSetUp,
		'fit an element into another elment (bounding box)': function () {

			var g1 =  Y.SvgUtils.svgElement('g');
			g1.setAttribute('transform', 'rotate(30)');
			this.svgCanvas.appendChild(g1);

			var g2 =  Y.SvgUtils.svgElement('g');
			g2.setAttribute('transform', 'translate(100), scale (1,2)');
			g1.appendChild(g2);

			var h1 =  Y.SvgUtils.svgElement('g');
			h1.setAttribute('transform', 'rotate(70), translate(30), scale (3000,2)');
			this.svgCanvas.appendChild(h1);

			function createRect() {
				var rect = Y.SvgUtils.svgElement('rect');
				rect.setAttribute('x', '14');
				rect.setAttribute('y', '18');
				rect.setAttribute('width', '32');
				rect.setAttribute('height', '27');
				rect.setAttribute('fill', 'none');
				rect.setAttribute('stroke', 'black');
				return rect;
			}

			var rect1 = createRect();
			g2.appendChild(rect1);

			function createEllipse() {
				var ellipse = Y.SvgUtils.svgElement('ellipse');
				ellipse.setAttribute('cx', '5');
				ellipse.setAttribute('cy', '2');
				ellipse.setAttribute('rx', '900');
				ellipse.setAttribute('ry', '5');
				ellipse.setAttribute('fill', 'none');
				ellipse.setAttribute('stroke', 'black');
				return ellipse;
			}

			var ellipse1 = createEllipse();
			h1.appendChild(ellipse1);
			var ellipse2 = createEllipse();
			h1.appendChild(ellipse2);

			Y.SvgUtils.fitTo(ellipse2, rect1);

			var matrix = this.svgCanvas.getDOMNode().createSVGMatrix();

			var bbox1 = Y.SvgUtils.boundingBox(rect1, matrix);
			var bbox2 = Y.SvgUtils.boundingBox(ellipse2, matrix);

			Y.assert(aboutEqual(bbox1.x, bbox2.x), 'x');
			Y.assert(aboutEqual(bbox1.y, bbox2.y), 'y');
			Y.assert(aboutEqual(bbox1.width, bbox2.width), 'width');
			Y.assert(aboutEqual(bbox1.height, bbox2.height), 'height');


		}
	});


	svgUtilsTestSuite.add(fitToTest);

	Y.mix(Y.namespace("FaustTest"), {
		svgUtilsTestSuite: svgUtilsTestSuite
	});


}, '0.0', {
	requires: ["yui-base", "transcript", "test-utils"]
});