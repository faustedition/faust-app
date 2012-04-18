YUI().use('node', 'event', 'io', 'json', function(Y) {

	var textContentURL = top.path;

	function success(transactionid, response) {

		var content = Y.JSON.parse(response.responseText);
		// var ll = new Y.LineList();
		var ll = imageannotationLines;

		Y.each(content.lines, function(l) {
			ll.add(l)
		});
	}

	var cfg = {
		headers: {
			'Accept': 'application/json',
		},
		on: {
			success: success
		}
		
	}

	Y.io (textContentURL, cfg);

});