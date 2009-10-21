function yuiInit() {
	var loader = new YAHOO.util.YUILoader( {
		base : "http://ajax.googleapis.com/ajax/libs/yui/2.8.0r4/build/",
		require : [ "base", "fonts", "grids", "menu", "paginator", "reset" ],
		loadOptional : true,
		combine : false,
		filter : "MIN",
		allowRollup : true,
		insertBefore : "faust-css",
		onSuccess : function() {
			onContent("top-navigation", initTopNav);
			onDOM(initPage);
		}
	});
	loader.insert();
}

function initTopNav() {
	var topNavigation = new YAHOO.widget.MenuBar("top-navigation", {
		autosubmenudisplay : true,
		hidedelay : 750,
		lazyload : true
	});
	topNavigation.render();
}

function initPage() {
}

function onContent(id, func) {
	YAHOO.util.Event.onContentReady(id, func);
}

function onDOM(func) {
	YAHOO.util.Event.onDOMReady(func);
}