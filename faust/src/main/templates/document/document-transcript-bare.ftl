	<#macro resources paths>${cp}/resources?<#list paths as p>${p}<#if p_has_next>&amp;</#if></#list></#macro>
	<!DOCTYPE html>
	<html class="yui3-loading">
	<head>
	<title>Digitale Faust-Edition: Bare Transcript</title>
	<link rel="stylesheet" type="text/css" href="<@resources [
"yui3/build/cssreset/reset-min.css",
"yui3/build/cssgrids/grids-min.css",
"yui3/build/cssfonts/fonts-min.css",
"yui3/build/cssbase/base-min.css"
] />" />
    <link rel="stylesheet" type="text/css" href="${cp}/static/css/faust.css"/>
    <link rel="stylesheet" type="text/css" href="${cp}/static/css/document-transcript.css"/>
    <link rel="stylesheet" type="text/css" href="${cp}/static/css/document-transcript-highlight-hands.css"/>

    <script type="text/javascript">
    var cp = '${cp?js_string}';
var Faust = { contextPath: cp, FacsimileServer: "${facsimilieIIPUrl}" };
</script>
    <script type="text/javascript" src="${cp}/static/yui3/build/yui/yui-debug.js"></script>
    <script type="text/javascript" src="${cp}/static/js/yui-config.js"></script>
	<script type="text/javascript" src="${cp}/static/js/faust.js"></script>

	<link rel="stylesheet" type="text/css" href="${cp}/static/js/imageviewer/css/iip.css" />

    <script type="text/javascript" src="${cp}/static/yui3/build/yui/yui-debug.js"></script>
    <script type="text/javascript" src="${cp}/static/js/yui-config.js"></script>
	<script type="text/javascript" src="${cp}/static/js/faust.js"></script>
	<script type="text/javascript" src="${cp}/static/js/swfobject.js"></script>
	<script type="text/javascript" src="${cp}/static/js/raphael-min.js"></script>

</head>
	<body style="margin: 0px;">

	<div class="diplomatic-container"><div class="diplomaticContent"/></div>



	<script type="text/javascript">

YUI().use("node", "event", "transcript-view",	function(Y) {
	
	Y.on('faust:transcript-layout-done', function(e) {
		Y.one('.diplomatic').setStyle('left', '0px');
		Y.one('#editor-toolbar').setStyle('display', 'none');
		Y.one('body').setStyle('width', Y.one('.diplomatic').get('offsetWidth'));
		Y.fire('faust:bare-transcript-rendered', {});

	});		

	Y.on('faust:document-data-arrives', function(e) {

		var pagenum = parseInt (window.location.hash.slice(1));

		var diplomaticContent = Y.one('.diplomaticContent');					  

		var that = this;
		var page = e.pages[pagenum -1];
		var source = page.transcript.source;

		page.transcriptionFromRanges(function(t) {

			var diplomaticTranscriptView = new Y.Faust.DiplomaticTranscriptView({
				container: diplomaticContent,
				visComponent: null,
				transcript: t,
				source: source
			});

			diplomaticTranscriptView.render();

		});
	});				 				  
	
	Faust.Document.load(new Faust.URI("${document.source?js_string}"), function(fd) {

		var pages = [];
		var descendants = fd.descendants();
		for (i=0; i < descendants.length; i++)
			if (descendants[i].type === 'page' && descendants[i].transcript)
				pages.push(descendants[i]);

		Y.fire('faust:document-data-arrives', {
			fd: fd,
			pages: pages
		});
	});
	
});

</script>

</body>