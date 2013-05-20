<#assign uri = uri.toString()>
<#assign title>Structure ${uri}</#assign>
<#assign css>
	#transcript { margin-bottom: 2em; }
	
	.disabled { color: #ccc }
	
	#transcript-navigation { text-align: center; font-size: 85% }
	#transcript-document { margin: 1em; padding: 1em; border: 1px solid #ccc }
	#transcript-placeholder { text-align: center; color: #ccc }
	
	#transcript-browser { border: 1px solid #ccc;  background: #000; color: #ccc }
	#transcript-browser .yui3-widget-hd { padding: 0.5em 1em }
	#transcript-browser .yui3-widget-hd a { color: #ccc; }	
	#transcript-browser .yui3-widget-bd { white-space: nowrap; }
	#transcript-browser .yui3-widget-bd li { width: 150px; text-align: center }
	
	#transcript-canvas { border: 0;width: 450px; height: 500px }
	.zone { border: 1px dashed #ccc; margin: 3em 0; padding: 1em}	
</#assign>
<#assign header>
	<script type="text/javascript" src="${cp}/static/js/raphael-min.js"></script>
	<script type="text/javascript" src="${cp}/static/js/structure.js"></script>
</#assign>
<@faust.page title=title css=css header=header>
	<div class="demo-note">
		<p>In dieser Ansicht ist die Konvolutstruktur auf der linken Seite schematisch dargestellt. Bewegen Sie den Mauszeiger an eine beliebige Stelle im Konvolut, um die zu dieser Stelle gehörigen Faksimiles anzuzeigen. Ein Klick fixiert die Ansicht. Das Konvolut liegt nun vor Ihnen,  als wäre es auf der entsprechenden Seite aufgeschlagen. Ein Klick auf ein Faksimile führt zur Umschrift.</p>
	</div>

	<div id="structure" class="yui3-g">
		<div id="canvas" class="yui3-u-1-2"></div>
		<div id="padding" class="yui3-u-1-8"></div>		
		<div id="metadata" class="yui3-u-3-8"></div>	
	</div>	
	
	
	<div id="transcript-browser" class="hidden">
		<div class="yui3-widget-hd"><a id="transcript-hide-browser" href="" title="...">[X]</a></div>
		<div class="yui3-widget-bd">&nbsp;</div>
		<div class="yui3-widget-ft">&nbsp;</div>		
	</div>
	
	<script type="text/javascript">
		var Y = Faust.YUI().use("node", "dom", "dump", "io", "json", "event", "overlay", "scrollview", function(Y) {
			FaustStructure.load("${uri}")				
			});
	</script>
</@faust.page>