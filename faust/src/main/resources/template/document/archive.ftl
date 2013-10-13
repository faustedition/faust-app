<#ftl ns_prefixes={ "D":"http://www.faustedition.net/ns" }>
<#import "snippets.ftl" as snippets />
<#assign css>
	#archive_map { text-align: center; margin: 0 auto; width: 400px; height: 200px }
	#archival-units { margin: 1em auto; }
	#archival-units table { width: 100%; border-collapse: collapse; }
	#archival-units tr { border-bottom: 1px solid #ccc }
	#archival-units th { text-align: left }
</#assign>
<#assign header>
	<script type="text/javascript">
		var archiveLat = parseFloat('${archive.LOCATION_LAT?js_string}');
		var archiveLng = parseFloat('${archive.LOCATION_LNG?js_string}');
	</script>
	<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>
	<script type="text/javascript" src="${cp}/static/js/archive.js"></script>
</#assign>
<@faust.page title=(archive.NAME?html) header=header css=css>
	<div class="yui3-g">
		<div class="yui3-u-1-2">
			<@snippets.archiveData archive />
		</div>
		<div class="yui3-u-1-2" id="archive_map_slot"></div>
	</div>
	
	<#if documents?has_content>
	<div id="archival-units">
		<h2>${message("archive.archival_units")}</h2>
		
		<table>
			<tr>
				<th>#</th>
				<th>${message("archive.callnumber")}</th>
				<th>${message("archive.wa_id")}</th>

				<th>#</th>
				<th>${message("archive.callnumber")}</th>
				<th>${message("archive.wa_id")}</th>
			</tr>
			<#list documents as d>
				<#if (d_index % 2) == 0><tr></#if>
				<td>${d_index + 1}.</td>
				<td><a href="${cp}/document/${d.ID?url}" title="${message('archive.goto_document')}">${d.CALLNUMBER!'-'?html}</a></td>
				<td><a href="${cp}/document/${d.ID?url}" title="${message('archive.goto_document')}">${d.WA_ID!'-'?html}</a></td>
				<#if (d_index % 2) == 1></tr></#if>
				<#if !d_has_next && ((d_index % 2) == 0)>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				</tr>
				</#if>
			</#list>
		</table>
	</div>
	</#if>	
</@faust.page>