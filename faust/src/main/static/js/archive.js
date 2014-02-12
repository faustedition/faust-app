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

Faust.YUI().use("node", "dom", "event", function(Y) {
	Y.on("domready", function() {
		// check for overview map enhancement
		var archives = Y.DOM.byId("archives");
		if (archives) {
			Y.DOM.addHTML(archives, '<div id="archives_map"></div>', 'before');
			var archivesMap = new google.maps.Map(document.getElementById("archives_map"), {
				backgroundColor: "#fff",
				zoom: 3,
				center: new google.maps.LatLng(43, -28),
				mapTypeId: google.maps.MapTypeId.HYBRID
			});

			Y.all(".archive-container").each(function(a) {
				var lat = parseFloat(a.one("span.lat").get("text"));
				var lng = parseFloat(a.one("span.lng").get("text"));
				var marker = new google.maps.Marker({
					position: new google.maps.LatLng(lat, lng),
					title: a.one("a").get("text"),
					map: archivesMap
				});
				google.maps.event.addListener(marker, 'click', function() {
					window.location = a.one("a").getAttribute("href");
				});
			});	
		}

		// check for single archive map enhancement
		var archiveMapSlot = Y.DOM.byId("archive_map_slot");
		if (archiveMapSlot) {
			Y.DOM.addHTML(archiveMapSlot, '<div id="archive_map"></div>');
			var map = new google.maps.Map(document.getElementById("archive_map"), {
				backgroundColor: "#fff",
				zoom: 6,
				center: new google.maps.LatLng(archiveLat, archiveLng),
				mapTypeId: google.maps.MapTypeId.HYBRID
			});

			var marker = new google.maps.Marker({
				position: new google.maps.LatLng(archiveLat, archiveLng),
				map: map
			});			
		}
	});
});

