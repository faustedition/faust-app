YUI().use("node-menunav", function(Y) {
	Y.on("domready", function() {
		Y.one("#top-navigation").plug(Y.Plugin.NodeMenuNav);
	});
});