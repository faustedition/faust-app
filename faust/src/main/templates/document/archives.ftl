<#ftl ns_prefixes={ "D":"http://www.faustedition.net/ns" }>
<#import "snippets.ftl" as snippets />
<#assign title = message("menu.archives")>
<#assign css>
	#archives_map { text-align: center; margin: 2em auto; width: 800px; height: 400px }
	#archives { margin: 1em auto; border-collapse: collapse; }
	#archives tr th, #archives tr td { border-bottom: 1px solid #ccc }
	#archives tr th { width: 40%; text-align: left }
	#archives tr td.archive_description { width: 50%; padding: 1em }
</#assign>
<#assign header>
	<@faust.googleMaps />
	<script type="text/javascript" src="${cp}/static/js/archive.js"></script>
	<script type="text/javascript">archiveOverviewMap();</script>
</#assign>
<@faust.page title=title css=css header=header>
	<table id="archives">
		<#list archives.archive as a>
			<tr>
				<td>${a_index + 1}.</td>
				<th><a href="${cp}/archive/${(a.@id)?url}">${a.name?html}</a></th>
				<td><@snippets.archiveData a false /></td>			
			</tr>
		</#list>		
	</table>
</@faust.page>