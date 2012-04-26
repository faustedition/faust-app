YUI().use('node', 'event', 'io', 'json', function(Y) {

	// first request the text content/lines
	// then the svg data
	// (the svg data contains references to the text)

	var svgURL = window.parent.location.href.split('?')[0];

	var textContentURL = top.path;

	function textSuccess(transactionid, response) {

		var content = Y.JSON.parse(response.responseText);
		var ll = imageannotationLines;

		Y.each(content.lines, function(l) {
			ll.add(l)
		});

		svgEditor.loadFromURL(svgURL);
	}

	var textCfg = {
		headers: {
			'Accept': 'application/json',
		},
		on: {
			success: textSuccess
		}
		
	}


	Y.io (textContentURL, textCfg);


});