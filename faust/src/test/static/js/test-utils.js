YUI.add('test-utils', function (Y) {

	function getTestSandbox(name) {
		var outer = Y.Node.create('<div class="test_sandbox_outer" ><h3>' + name + '</h3></div>');
		Y.one('#test_sandboxes').append(outer);
		var inner = Y.Node.create('<div class="test_sandbox_inner" style="border: 1px black solid;' +
			'float:left"></div>');
		outer.append(inner);
		return inner;
	}

	var svgCanvasSetUp = function() {

		var svgns = "http://www.w3.org/2000/svg";
		var svgElement = document.createElementNS (svgns, 'svg');
		svgElement.setAttribute('width', '200');
		svgElement.setAttribute('height', '200');
		var container = document.createElementNS (svgns, 'g');
		container.setAttribute('id', 'transcript_container');
		svgElement.appendChild(container);
		this.svgCanvas = Y.FaustTest.getTestSandbox(this.name).appendChild(svgElement);

	};

	var marker = function(x, y) {
		var svgns = "http://www.w3.org/2000/svg";
		var marker = document.createElementNS (svgns, 'rect');
		marker.setAttribute('x', x);
		marker.setAttribute('y', y);
		marker.setAttribute('width', '10');
		marker.setAttribute('height', '10');
		document.getElementById('transcript_container').appendChild(marker);
	};


	Y.mix(Y.namespace("FaustTest"), {
		getTestSandbox: getTestSandbox,
		svgCanvasSetUp: svgCanvasSetUp,
		marker: marker
	});

}, '0.0', {
    requires: ["node"]
});