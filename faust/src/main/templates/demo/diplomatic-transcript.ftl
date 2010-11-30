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
	<link rel="stylesheet" type="text/css" href="${cp}/static/css/facsimile.css">
	<script type="text/javascript" src="${cp}/static/js/mootools-1.2-core-compressed.js"></script>
	<script type="text/javascript" src="${cp}/static/js/mootools-1.2-more-compressed.js"></script>
	<script type="text/javascript" src="${cp}/static/js/facsimile.js"></script>
	<script type="text/javascript" src="${cp}/static/js/swfobject.js"></script>
	<script type="text/javascript" src="${cp}/static/js/goddag.js"></script>
	<script type="text/javascript" src="${cp}/static/js/textmodel.js"></script>
	<script type="text/javascript" src="${cp}/static/js/document.js"></script>
	<script type="text/javascript" src="${cp}/static/js/documentview.js"></script>
	<script type="text/javascript" src="${cp}/static/js/demo/diplomatic-transcript.js"></script>
</#assign>
<@faust.page title=message('demo.diplomatic_transcript.title') css=css header=header>
	<div id="transcript" class="yui3-g">
		<div class="yui3-u-1-2">
			<iframe name="transcript-canvas" id="transcript-canvas" src="${cp}/static/svg/demo-diplomatic-transcript.svg"></iframe>
		</div>
		<div class="yui3-u">
			<div id="transcript-facsimile" class="facsimile" style="width: 450px; height: 600px"></div>
		</div>
	</div>
</@faust.page>