<#assign archiveId = document.getMetadataValue("archive")>
<#assign callnumber = document.getMetadataValue("callnumber")>
<#assign waId = document.getMetadataValue('wa-id')!"">
<#assign title>${archiveId?upper_case}/ ${callnumber?html}<#if waId?has_content> &ndash; ${waId?html}</#if></#assign>
<#assign css>
	#transcript { margin: 1em auto; text-align: center }
	
	.disabled { color: #ccc }
	
	#transcript-navigation { text-align: center; font-size: 85%; margin: 1em }
	#transcript-document { margin: 1em; padding: 1em; border: 1px solid #ccc }
	#transcript-placeholder { text-align: center; color: #ccc }
	
	#transcript-browser { border: 1px solid #ccc;  background: #000; color: #ccc }
	#transcript-browser .yui3-widget-hd { padding: 0.5em 1em }
	#transcript-browser .yui3-widget-hd a { color: #ccc; }	
	#transcript-browser .yui3-widget-bd { white-space: nowrap; }
	#transcript-browser .yui3-widget-bd li { width: 150px; text-align: center }
	
	#transcript-facsimile, #transcript-text { height: 600px; }
	#transcript-text { overflow: scroll }
	#transcript-swf { border: 1px inset grey; }
	.zone { border: 1px dashed #ccc; margin: 3em 0; padding: 1em}	
</#assign>
<#assign header>
	<script type="text/javascript" src="${cp}/static/js/swfobject.js"></script>
	<script type="text/javascript" src="${cp}/static/js/goddag.js"></script>
	<script type="text/javascript" src="${cp}/static/js/document/materialunit.js"></script>
	<script type="text/javascript" src="${cp}/static/js/document/model.js"></script>
	<script type="text/javascript" src="${cp}/static/js/document/view.js"></script>
</#assign>
<@faust.page title=title css=css header=header>
	<div id="transcript-navigation">
		<p>
			<a id="transcript-prev-page" class="disabled" href="" class="disabled" title="${message('transcript.prev_page')}">${message('transcript.prev_page')}</a> |
			<a id="transcript-browse" class="disabled" href="" title="${message('transcript.browse')}">${message('transcript.browse')}</a> |
			<a id="transcript-structure" class="disabled" href="" title="${message('transcript.structure')}">${message('transcript.structure')}</a> |
			<a id="transcript-next-page" class="disabled" href="" class="disabled" title="${message('transcript.next_page')}">${message('transcript.next_page')}</a>
		</p>
		<form>
			<select name="transcript-view-mode" id="transcript-view-mode">
				<option value="text-facsimile">Text/Faksimile</option>
				<option value="text">Text</option>
				<option value="facsimile">Faksimile</option>
			</select>
		</form>
	</div>
	<div id="transcript" class="yui3-g">
		<div class="yui3-u-1-2">
			<div id="transcript-facsimile"></div>
		</div>
		<div class="yui3-u">
			<iframe name="transcript-canvas" id="transcript-canvas" src="${cp}/static/empty.svg"></iframe>
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