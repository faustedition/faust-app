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

function encodingStatus(path) {
	onContent("encoding-status", function() {
		var transcriptionStatusDs = new YAHOO.util.XHRDataSource(cp + '/metadata/encoding/' + encodeURI(path));
		transcriptionStatusDs.connMgr.initHeader('Content-Type', 'application/json', true);
		transcriptionStatusDs.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
		transcriptionStatusDs.responseSchema = {
			resultsList : "statusList",
			fields : [ "status", "count" ]
		};
		var transcriptionStatusTable = new YAHOO.widget.DataTable("encoding-status", [ {
			key : "status",
			label : "Status"
		}, {
			key : "count",
			label : "Anzahl"
		} ], transcriptionStatusDs);
	});
}

function resizeFacsimile() {
	YAHOO.util.Dom.setStyle("facsimile", "display", "block");
}
