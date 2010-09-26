<@faust.page title=message("genesis.sample")>
	<h2>${message("genesis.sample")}</h2>

	<div>${imageMap}</div>
	
	<p style="text-align: center; margin: 2em 0">
		<img src="${cp}/genesis/chart.png" alt="Versanalyse" usemap="#genesisChart" />
	</p>
	
	<h3>Paralipomena</h3>

	<p>
		<#list paralipomena as p>
		<a href="${cp}/document/${p.portfolio}#${p.manuscript}" title="Zum Manuskript">${p.name?html}</a>
		<#if p_has_next>,&nbsp;</#if>
		</#list>
	</p>
	
	<ol>
	</ol>
	
	<#--
	<h3>Urfaust</h3>

	<ol>
	<li><a href="${cp}/document/${urfaust.portfolio}#${urfaust.manuscript}" title="Zum Manuskript">${urfaust.name?html}</a></li>
	</ol>
	-->
</@faust.page>