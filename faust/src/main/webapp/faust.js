function yuiInit() {
	onContent("top-navigation", initTopNav);
	onDOM(initPage);
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