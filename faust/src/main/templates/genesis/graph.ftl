<#ftl ns_prefixes={ "D":"http://www.faustedition.net/ns" }>
<#assign css>
	#archival-units { margin: 1em auto; }
	#archival-units table { width: 100%; border-collapse: collapse; }
	#archival-units tr { border-bottom: 1px solid #ccc }
	#archival-units th { text-align: left }	
</#assign>
<#assign header>
</#assign>
<@faust.page title="Genetic Graph" header=header css=css>
	
	<#if archivalUnits?has_content>
	<div id="archival-units">
		<h2>Genetic Graph</h2>
		<table>
			<#list archivalUnits as unit>
				<tr>
				<td>${unit_index + 1}.</td>
				<td>
					<a href="${cp + unit.source.toString()?replace('faust://xml', '')}" title="${message('archive.goto_document')}">
					${unit.getMetadataValue("callnumber")!"–"?html}
					</a>
				</td>
				<td>${unit.getMetadataValue("wa-id")!"–"?html}</td>				
				<td class="relation-targets">
				<#list unit.geneticallyRelatedTo() as g>
					precedes: <a href="${g.source.toString()?replace('faust://xml', '')}" title="${message('archive.goto_document')}">
					${g.getMetadataValue("callnumber")!"–"?html}
					</a>
					<br>
				</#list>
				</td>			
				</tr>
			</#list>
		</table>
	</div>
	</#if>	
</@faust.page>