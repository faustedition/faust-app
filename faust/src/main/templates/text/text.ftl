<#macro partTitle textId><#compress>
	<#local textPath=textId?split("-") />
	<#if textPath[0] == "0">Urfaust<#elseif textPath[0] == "1">Faust I<#else>Faust II</#if>:
	<#if textPath[0] == "0"><#if textPath[1]?starts_with("0")>${textPath[1]?substring(1)}<#else>${textPath[1]}</#if>. Szene</#if>
	<#if textPath[0] == "1">
		<#if textPath[1] == "1">
			Prolog
		<#else>
			<#if textPath[2]?starts_with("0")>${textPath[2]?substring(1)}<#else>${textPath[2]}</#if>. Szene
		</#if>
	</#if>

	<#if textPath[0] == "2">${textPath[1]}. Akt, ${textPath[2]}. Szene</#if>
</#compress></#macro>
<#assign textId=text.source?substring('faust://xml/text/'?length, text.source?length - 4) />
<#macro textNavigation>
	<div class="text-navigation">
	<#local page=0 />
	<#list textToc?keys as t>
		<#if t?starts_with(textId?substring(0, 2))>
		<#local page=page+1 />
		<#local title><@partTitle t/> (${textToc[t]?html})</#local> 
		<#if t == textId><span class="current-page"><#else><a class="page-link" href="${cp}/text/${t}.xml" title="${title}"></#if>
		${page}<#if t == textId></span><#else></a></#if>&nbsp;
		</#if>		
	</#list>
	</div>
</#macro>
<#assign css>
	#text-panel { margin: 1em 2em }
	.text-navigation { text-align: center; margin-top: 2em; margin-bottom: 1em }
	.page-link, .current-page { padding: 0.5em 0.25em }
	.page-link { border: 1px solid #ccc; font-weight: normal }
	.current-page { font-size: 123.1%; font-weight: bold }
</#assign>
<#assign header>
	<script type="text/javascript" src="${cp}/static/lib/protovis-r3.2.js"></script>
	<script type="text/javascript" src="${cp}/static/js/goddag.js"></script>
	<script type="text/javascript" src="${cp}/static/js/text/model.js"></script>
	<script type="text/javascript" src="${cp}/static/js/text/view.js"></script>
</#assign>
<#assign title><@partTitle textId/></#assign>
<@faust.page title=title css=css header=header>
	<@textNavigation/>
	<p class="system-note">${message('note.loading')}</p>
	<@textNavigation/>
	<script type="text/javascript">
		Faust.YUI().use("event", "node", function(Y) {
			var text = new Faust.ReadingText(new Faust.URI('${text.source?js_string}'));
			text.load();				
		});
	</script>
</@faust.page>