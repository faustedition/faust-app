	<#assign archiveId = document.getMetadataValue("archive")>
	<#assign callnumber = document.toString()>
	<#assign waId = document.getMetadataValue('wa-id')!"">
	<#assign title>${callnumber?html}<#if waId?has_content> &ndash; ${waId?html}</#if></#assign>
	<#assign imageLinkBase>${cp}/document/imagelink/${document.source?replace('faust://xml/document/', '')}</#assign>
	<#assign header>
	<link rel="stylesheet" type="text/css" href="${cp}/static/lib/imageviewer/css/iip.css" />
	<link rel="stylesheet" type="text/css" href="${cp}/static/css/document-text.css"/>
    <link rel="stylesheet" type="text/css" href="${cp}/static/css/document-transcript.css"/>
    <link rel="stylesheet" type="text/css" id="style-document-transcript-highlight-hands" href="${cp}/static/css/document-transcript-highlight-hands.css"/>
	<link rel="stylesheet" type="text/css" href="${cp}/static/css/document-transcript-interaction.css"/>
	<script type="text/javascript" src="${cp}/static/lib/swfobject.js"></script>
	<script type="text/javascript" src="${cp}/static/lib/raphael-min.js"></script>
	<script type="text/javascript"> Faust.imageLinkBase = "${imageLinkBase}" </script>

	<@faust.passMessages ["page_abbrev", "document.old_version", "document.facsimile",
	"document.diplomatic_transcript", "document.document_structure", "document.document_text"] />

	</#assign>




	<@faust.page title=title header=header layout="wide" menuhighlight="archives">



	<div id="document-app" class="yui-u-1" style="min-height: 600px;">
		<div class="facsimile-container mode-switchable" style="position: absolute; top: 0em; left: 0em; width: 100%;
		overflow:hidden;">
			<div class="facsimile-content">
			</div>
		</div>
		<div class="diplomatic-container mode-switchable" style="position: absolute; top: 4em; left: 0em; width: 100%;
		overflow: auto; height: 100%">
			<div class="diplomatic-content text-center">
			</div>
		</div>
		<div class="text-container mode-switchable" style="position: absolute; top: 4em; left: 0em; width: 100%;
		overflow: auto; height: 100%">
			<div class="text-content" style="width: 40em; display: block; margin-left: auto; margin-right: auto;">
			</div>
		</div>

	</div>
	<div id="document-navigation" style="z-index: 20; height: 50px; position: absolute; top: 1em; left: 2em;"></div>
	<div id="document-mode" style="z-index: 20; height: 50px; position: absolute; bottom: 2em; right: 2em;"></div>
	<script type="text/javascript">

YUI().use("stylesheet", "document-app", function(Y) {
		  
    //Y.StyleSheet('#style-document-transcript-highlight-hands').disable();
    Y.on("contentready", Y.Faust.DocumentApp.initializeFn('${path}', '${document.source?js_string}'), '#document-app');
});


</script>
	</@faust.page>
