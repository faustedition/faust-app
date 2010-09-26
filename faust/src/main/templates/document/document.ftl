<#assign archiveId = document.getMetadataValue("archive")>
<#assign callnumber = document.getMetadataValue("callnumber")>
<#assign waId = document.getMetadataValue('wa-id')!"">
<#assign title>${archiveId?upper_case}/ ${callnumber?html}<#if waId?has_content> &ndash; ${waId?html}</#if></#assign>
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
			<div id="transcript-navigation">
				<a id="transcript-prev-page" class="disabled" href="" class="disabled" title="${message('transcript.prev_page')}">${message('transcript.prev_page')}</a> |
				<a id="transcript-browse" class="disabled" href="" title="${message('transcript.browse')}">${message('transcript.browse')}</a> |
				<a id="transcript-next-page" class="disabled" href="" class="disabled" title="${message('transcript.next_page')}">${message('transcript.next_page')}</a>
			</div>
			
			<h3>${message("transcript.title")}</h3>
			
			<div id="transcript-document">
				<div id="transcript-placeholder">n/a</div>
			</div>
		</div>
		<div class="yui3-u">
			<div id="transcript-facsimile"></div>
		</div>
	</div>
	<div id="transcript-browser" class="hidden">
		<div class="yui3-widget-hd"><a id="transcript-hide-browser" href="" title="...">[X]</a></div>
		<div class="yui3-widget-bd">&nbsp;</div>
		<div class="yui3-widget-ft">&nbsp;</div>		
	</div>
	<script type="text/javascript">
		var Y = Faust.YUI().use("node", "dom", "dump", "io", "json", "event", "overlay", "scrollview", function(Y) {
			Faust.Document.load(new Faust.URI("${document.source?js_string}"), function(fd) {
				documentView = new Faust.DocumentView(fd);				
			});
		});
	</script>
</@faust.page>