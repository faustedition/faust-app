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




	<@faust.page title=title header=header layout="wide">


	<div id="document-navigation" style="height: 50px;">

</div>
	<div id="document-app" class="yui-u-1" style="min-height: 600px;"></div>

	<script type="text/javascript">

YUI().use("stylesheet", "document-app", function(Y) {
		  
    //Y.StyleSheet('#style-document-transcript-highlight-hands').disable();
    Y.on("contentready", Y.Faust.DocumentApp.initializeFn('${path}', '${document.source?js_string}'), '#document-app');
});


</script>
	</@faust.page>
