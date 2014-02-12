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

Faust.YUI().use("oop", "node", function(Y) {
	Faust.ReadingText = function(uri) {
		this.uri = uri;
	};
	Faust.ReadingText.prototype = {
		load: function() {
			Faust.io("goddag/" + this.uri.encodedPath(), function(data) {
				var g = Y.Node.create("<div class='yui3-g'/>");
				g.append("<div id='text-panel' class='yui3-u-1-2'/>");
				g.append("<div id='info-panel' class='yui3-u'><div id='gantt-chart'></div><p></p><div id='genetic-tree'></div></div>");
				Y.one(".system-note").replace(g);
				
				var textPanel = new Faust.ReadingTextPanel("#text-panel", new Goddag.Graph(data));				
				textPanel.render();
				
				var chart = new Faust.GeneticGanttChart("gantt-chart", data);
				chart.render();
				
				var tree = new Faust.GeneticTree("genetic-tree", data);
				tree.render();
				
			});
		}
	};
});
