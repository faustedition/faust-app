YUI().use('node', 'event', 'io', 'json', function(Y) {

	// first request the text content/lines
	// then the svg data
	// (the svg data contains references to the text)

	var svgURL = window.parent.location.href.split('?')[0];

	var textContentURL = top.path;

	function textSuccess(transactionid, response) {
		$.unblockUI();
		var content = Y.JSON.parse(response.responseText);
		var ll = imageannotationLines;

		Y.each(content.lines, function(l) {
			ll.add(l)
		});
		
		svgOpts = {
			callback: function() {
				//zoom and center (this should be a function in SvgCanvas ...)
				// var scrbar = 15,
				// w_area = $('#workarea');
				// var z_info = svgCanvas.setBBoxZoom('canvas', w_area.width()-scrbar, w_area.height()-scrbar); 
				// svgCanvas.updateCanvas();
				//zoomDone();
				$('#fit_to_canvas').trigger('mouseup');
				$.unblockUI();
			}
		}

		$.blockUI({ message: '<h1>Loading SVG data...</h1>' });
		svgEditor.loadFromURL(svgURL, svgOpts);

		
	}

	var textCfg = {
		headers: {
			'Accept': 'application/json',
		},
		on: {
			success: textSuccess
		}
		
	}

	$.blockUI({ message: '<h1>Loading text data...</h1>' });
	Y.io (textContentURL, textCfg);


});