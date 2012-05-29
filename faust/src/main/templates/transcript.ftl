<#assign hasWaId=(waId?has_content && waId != "-")>
<#assign title><#if hasWaId>${waId?html}<#else>${callnumber!"–"?html}</#if></#assign>
<#assign header>
<link href="${cp}/static/google-code-prettify/prettify.css" type="text/css" rel="stylesheet" />
<script src="${cp}/static/google-code-prettify/prettify.js" type="text/javascript"></script>
</#assign>
<@faust.page title=title header=header>
	<h2>
		${title}
		<#if archiveName?has_content>
			&mdash;
			<span style="font-weight: normal;">
				<a href="${cp}/archive/${archiveId!""?url}">${archiveName?html}</a><#if hasWaId>: ${callnumber!"-"?html}</#if>
			</span>
		</#if>
	</h2>

	<div id="facsimile-view" style="text-align: center"></div>

	<div id="plain-text-overlay" class="yui3-overlay-loading">
		<div class="yui3-widget-hd">Textual Transcript</div>
		<div class="yui3-widget-bd"><p id="plain-text" style="font-size: 108%; padding: 1em"></p></div>
		<div class="yui3-widget-ft"></div>
	</div>

	<div id="source-overlay" class="yui3-overlay-loading">
		<div class="yui3-widget-hd">XML Source</div>
		<div class="yui3-widget-bd"><pre id="source" style="font-size: 108%; padding: 1em; border: 0"></pre></div>
		<div class="yui3-widget-ft"></div>
	</div>

	<script type="text/javascript">
		YUI().use("io", "node", "overlay", "dd-plugin", "facsimile", "text-annotation", "text-index", function(Y) {
			Y.on("domready", function() {
				var facsimileViewer = new Y.Faust.FacsimileViewer({
					srcNode: "#facsimile-view",
					src: "/facsimile/agad_warszawa/PL_1_354_13-16-24/0014",
					view: { x: 0, y: 0, width: 900, height: 600 }
				});
				facsimileViewer.render();

				var plainTextOverlay = new Y.Overlay({ srcNode: "#plain-text-overlay", width: "30em", height: "50em", centered: true });
				plainTextOverlay.plug(Y.Plugin.Drag);
				plainTextOverlay.dd.addHandle(".yui3-widget-hd");
				plainTextOverlay.render();

				var sourceOverlay = new Y.Overlay({ srcNode: "#source-overlay", width: "60em", height: "50em", centered: true });
				sourceOverlay.plug(Y.Plugin.Drag);
				sourceOverlay.dd.addHandle(".yui3-widget-hd");
				sourceOverlay.render();

				Y.io(cp + "/transcript/source/${id?c}", {
					headers: {
						"Accept": "application/json"
					},
					on: {
						success: function(id, response) {
							var plainTextNode = Y.one("#plain-text");
							text = Y.Faust.Text.create(Y.JSON.parse(response.responseText));
							Y.Array.each(text.content.split("\n"), function(line, n) {
								if (n > 0) {
									plainTextNode.append("<br>");
								}
								plainTextNode.append(Y.config.doc.createTextNode(line));
							});
							Y.log(text.find(new Y.Faust.Range(0, 10)));
						}
					}
				});

				Y.io(cp + "/transcript/source/${id?c}", {
					headers: {
						"Accept": "application/xml"
					},
					on: {
						success: function(id, response) {
							var sourceNode = Y.one("#source");
							sourceNode.append(Y.config.doc.createTextNode(response.responseText));
							if (response.responseText.length < 51200) {
								sourceNode.addClass("prettyprint");
								prettyPrint();
							}
						}
					}
				});
			});
		})
	</script>
</@faust.page>