<#assign title><@faust.nameOf path /></#assign>
<#assign header>
	<link rel="stylesheet" type="text/css" href="${cp}/css/facsimile.css">
	<script type="text/javascript" src="${cp}/js/mootools-1.2-core-compressed.js"></script>
	<script type="text/javascript" src="${cp}/js/mootools-1.2-more-compressed.js"></script>
	<script type="text/javascript" src="${cp}/js/iipmooviewer-1.1.js"></script>
</#assign>
<@faust.page title=(title?html) header=header>
	<@faust.breadcrumbs path "Überlieferungsträger" />
	
	<h2>
		${title?html}
		<a href="${cp}/${path?url?replace("%2F", "/")}?format=pdf" title="PDF-Test"><img src="${cp}/img/pdf-logo.png" alt="PDF-Test"/></a>
	</h2>
	
	<#if facsimile??>
	<div id="facsimile" style="position: relative; left: 0; top: 0; width: 100%; height: 500px"></div>
	<script type="text/javascript">
		iip = new IIP("facsimile", {
				image: "${(facsimile.path + '.tif')?url}",
				server: "<#if config['facsimile.iip.url']?starts_with("/")>${cp}</#if>${config['facsimile.iip.url']?js_string}",
				credit: '&#169; Digitale Faust-Edition', 
				zoom: 1,
				showNavButtons: true
				});
	</script>
	</#if>
</@faust.page>
