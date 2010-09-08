function archiveOverviewMap() {
	YUI().use("node", "dom", "event", function(Y) {
		Y.on("domready", function() {			
			Y.DOM.addHTML(Y.DOM.byId("archives"), '<div id="archives_map"></div>', 'before');
			var archivesMap = new google.maps.Map(document.getElementById("archives_map"), {
				backgroundColor: "#fff",
				zoom: 3,
				center: new google.maps.LatLng(43, -28),
				mapTypeId: google.maps.MapTypeId.HYBRID
			});

			Y.all("#archives tr").each(function(a) {
				var lat = parseFloat(a.one("td div.archive span.lat").get("text"));
				var lng = parseFloat(a.one("td div.archive span.lng").get("text"));
				var marker = new google.maps.Marker({
					position: new google.maps.LatLng(lat, lng),
					title: a.one("th a").get("text"),
					map: archivesMap
				});
				google.maps.event.addListener(marker, 'click', function() {
					window.location = a.one("th a").getAttribute("href");
				});
			});
		});
	});	
}

function archiveLocation(lat, lng) {
	lat = parseFloat(lat);
	lng = parseFloat(lng);
	YUI().use("node", "dom", "event", function(Y) {
		Y.on("domready", function() {			
			Y.DOM.addHTML(Y.DOM.byId("archive_map_slot"), '<div id="archive_map"></div>');
			var map = new google.maps.Map(document.getElementById("archive_map"), {
				backgroundColor: "#fff",
				zoom: 6,
				center: new google.maps.LatLng(lat, lng),
				mapTypeId: google.maps.MapTypeId.HYBRID
			});

			var marker = new google.maps.Marker({
				position: new google.maps.LatLng(lat, lng),
				map: map
			});
		});
	});
}
