<#assign title = document.getSource()?html>
<#assign css>
	#transcript { margin-bottom: 2em; }
	#transcript-document { padding: 1em; }
	.yui3-scrollview { background: #000; color: #ccc; border: 1px solid #ccc; }
	#page-gallery { white-space: nowrap; margin: 1em }
	#page-gallery li { width: 150px; text-align: center }	
</#assign>
<#assign header>
	<script type="text/javascript" src="${cp}/static/js/swfobject.js"></script>
	<script type="text/javascript" src="${cp}/static/js/goddag.js"></script>
	<script type="text/javascript" src="${cp}/static/js/document.js"></script>
	<script type="text/javascript" src="${cp}/static/js/documentview.js"></script>
</#assign>
<@faust.page title=title css=css header=header>
	<div id="transcript" class="yui3-g">
		<div class="yui3-u-1-2">
			<div id="transcript-facsimile"></div>
		</div>
		<div class="yui3-u-1-2" id="transcript-document">
			<svg width="100%" height="500px" version="1.1" xmlns="http://www.w3.org/2000/svg" />
		</div>
	</div>
	<div id="page-gallery">&nbsp;</div>
	<script type="text/javascript">
		var Y = Faust.YUI().use("node", "dom", "dump", "io", "json", "event", "scrollview", function(Y) {
			Faust.Document.load(new Faust.URI("${document.source?js_string}"), function(fd) {
				documentView = new Faust.DocumentView(fd);				
				documentView.setPage(parseInt(window.location.hash.substring(1)));
				documentView.renderPageNavigation();
			});
		});
	</script>
</@faust.page>