<div id="transcript-navigation">
	<p>
		<a id="transcript-prev-page" class="disabled" href="" class="disabled" title="${message('transcript.prev_page')}">${message('transcript.prev_page')}</a> |
		<a id="transcript-browse" class="disabled" href="" title="${message('transcript.browse')}">${message('transcript.browse')}</a> |
		<a id="transcript-structure" class="disabled" href="" title="${message('transcript.structure')}">${message('transcript.structure')}</a> |
		<a id="transcript-next-page" class="disabled" href="" class="disabled" title="${message('transcript.next_page')}">${message('transcript.next_page')}</a> |
		<a id="image-link" href="${cp}/document/imagelink/${document.source?replace('faust://xml/document/', '')}">Text-Image-Linking</a>
	</p>
	<div id="error-display">	
	</div>
	<form>
		<select name="transcript-view-mode" id="transcript-view-mode">
			<option value="text-facsimile">Text/Faksimile</option>
			<option value="text">Text</option>
			<option value="facsimile">Faksimile</option>
		</select>
		<select name="transcript-preference-overlay" id="transcript-preference-overlay">
			<option value="overlay">Überblendung</option>
			<option value="no-overlay">Entzerrung</option>
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
		Y.on("domready", function() {
			Faust.Document.load(new Faust.URI("${document.source?js_string}"), function(fd) {
			documentView = new Faust.DocumentView(fd);
			});
		});
	});
</script>
