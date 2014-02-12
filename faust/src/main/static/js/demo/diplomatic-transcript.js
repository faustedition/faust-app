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

Faust.YUI().use("node", "dom", "dump", "io", "json", "event", "overlay", "scrollview", function(Y) {
	Y.on("domready", function() {
		iip = new Faust.FacsimileViewer("transcript-facsimile", {
				image: "gsa/390883/390883_0031.tif",
				zoom: 4,
				showNavButtons: true
				});
				
		var svgDocument = window.frames["transcript-canvas"].document;
		
		if (Y.UA.gecko > 0) {
			Y.each(svgDocument.getElementsByClassName("underline"), function(u) {
				var rect = u.getClientRects()[0];
				
			});
		}
		
		var patch = svgDocument.getElementById("patch");
		patch.addEventListener("click", function(e) {
			patch.setAttribute("opacity", patch.getAttribute("opacity") == "0.1" ? "0.9" : "0.1");
		}, false);
		
		var margin = svgDocument.getElementById("margin");
		margin.addEventListener("click", function(e) {
			while (iip.res < 4) iip.zoomIn();
			while (iip.res > 4) iip.zoomOut();
			iip.moveTo(400, 1800);
		}, false);
	});
});		
