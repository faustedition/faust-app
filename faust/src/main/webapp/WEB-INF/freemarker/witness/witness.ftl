<#assign title><@faust.nameOf path /></#assign>
<@faust.page title=(title?html)>
	<@faust.breadcrumbs path "Überlieferungsträger" />
	
	<h2>
		${title?html}
		<a href="${cp}/${encodePath(path)}?format=pdf" title="PDF-Test"><img src="${cp}/img/pdf-logo.png" alt="PDF-Test"/></a>
	</h2>
	
	<div id="facsimile" style="position: relative; left: 0; top: 0; width: 100%; height: 500px"></div>
	<script type="text/javascript">
		iip = new IIP( "facsimile", {
				<#-- image: '${(facsimile.path + ".tif")?url}', -->
				image: '${"London_British_Library/A80014-90.tif"?url}',
				server: '<#if config['facsimile.iip.url']?starts_with("/")>${cp}</#if>${config["facsimile.iip.url"]?js_string}',
				credit: '&copy; Digitale Faust-Edition', 
				zoom: 1,
				showNavButtons: true
				});
	</script>
	
	<div id="transcription">
	${htmlTranscription}
	</div>	
</@faust.page>
