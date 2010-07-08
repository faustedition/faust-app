YUI().use("node", "event", "dom", "yui2-menu", function(Y) {
	Y.on("domready", function() {
		var topNav = new Y.YUI2.widget.MenuBar("top-navigation", {
			autosubmenudisplay : true,
			hidedelay : 750
			//lazyload : true
		});
		topNav.render();
		topNav.show();
	});
});