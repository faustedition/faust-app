<#ftl ns_prefixes={ "D":"http://www.faustedition.net/ns" }>
<#import "snippets.ftl" as snippets />
<#assign title = message("menu.archives")>
<#assign css>
	#archives_map { text-align: center; margin: 2em auto; width: 800px; height: 400px }
	#archives { margin: 1em; border-collapse: collapse; }
	.archive-row {border-bottom: 1px solid #ccc }
	.archive-container { padding: 1em }
</#assign>
<#assign header>
	<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>
	<script type="text/javascript" src="${cp}/static/js/archive.js"></script>
</#assign>
<@faust.page title=title css=css header=header>
	<div id="archives">
		<#list archives as a>
			<#if (a_index % 3) == 0><div class="yui3-g archive-row"></#if>
			<div class="yui3-u-1-3">
			<div class="archive-container">
				<p>${a_index + 1}.<br><a href="${cp}/archive/${(a.LABEL)?url}">${a.NAME?html}</a></p>
				<@snippets.archiveData a false />
			</div>
			</div>
			<#if ((a_index % 3) == 2) || !a_has_next></div></#if>
		</#list>		
	</div>
</@faust.page>