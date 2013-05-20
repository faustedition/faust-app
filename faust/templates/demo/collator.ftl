<#assign css>
	table.alignment { width: 100%; border-collapse: separate; border-spacing: 0.25em }
	.alignment tr { border: 0 }
	.alignment th, .alignment td { border: 1px solid #ccc; margin: 1em }
	.alignment th { width: 15% }
</#assign>
<@faust.page title="Collation" css=css>
	<table class="alignment">
		<tr>
			<th colspan="2">
				Text<br>
				<a href="${cp}/goddag/transcript/gsa/390883/390883.xml?root=tei:text&amp;textnodes=true"
					title="Source">Source</a>
				
			</th>
			<th colspan="2">
				Document<br>
				<a href="${cp}/goddag/transcript/gsa/390883/0002.xml?root=ge:document&amp;textnodes=true"
					title="Source">Source</a>
			</th>
		</tr>
	<#list alignment as a>
		<tr>
			<th>${a.a!'-'?html}<br><#list a.anodes as n>${n?html}<#if n_has_next>, </#if></#list></th>
			<td><#list a.atags as t>&lt;${t?html}&gt; <#if t_has_next><br></#if></#list></td>
			<th>${a.b!'-'?html}<br><#list a.bnodes as n>${n?html}<#if n_has_next>, </#if></#list></th>
			<td><#list a.btags as t>&lt;${t?html}&gt; <#if t_has_next><br></#if></#list></td>
		</tr>
	</#list>
	</table>
</@faust.page>