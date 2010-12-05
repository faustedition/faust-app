Faust.YUI().use("oop", "node", function(Y) {
	Faust.ReadingText = function(uri) {
		this.uri = uri;
	};
	Faust.ReadingText.prototype = {
		load: function() {
			Faust.io("goddag/" + this.uri.encodedPath(), function(data) {
				var g = Y.Node.create("<div class='yui3-g'/>");
				var left = g.append("<div id='text-panel' class='yui3-u-1-2'/>");
				var right = g.append("<div id='info-panel' class='yui3-u'/>");
				Y.one(".system-note").replace(g);
				var textPanel = new Faust.ReadingTextPanel("#text-panel", new Goddag.Graph(data));
				textPanel.render();
			});
		}
	};
});
