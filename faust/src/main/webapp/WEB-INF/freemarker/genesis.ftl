[#ftl]
[@faust.page title="Genese"]
	<h2>Genese – Beispiel</h2>

	<div>${imageMap}</div>
	
	<p style="text-align: center; margin: 2em 0">
		<img src="${ctx}/genesis/chart.png" alt="Versanalyse" usemap="#genesisChart" />
	</p>
	
	<div class="yui-g">
	<div class="yui-u first">
		<h3>Urfaust</h3>

		<ol>
			<li><a href="${ctx}/manuscripts/GSA/${urfaust.portfolio?url}/${urfaust.manuscript}" title="Zum Manuskript">${urfaust.name?html}</a></li>
		</ol>
	</div>
	<div class="yui-u">
		<h3>Paralipomena</h3>
	
		<ol>
			[#list paralipomena as p]
				<li><a href="${ctx}/manuscripts/GSA/${p.portfolio?url}/${p.manuscript?url}" title="Zum Manuskript">${p.name?html}</a></li>
			[/#list]
		</ol>
	</div>
	</div>
[/@faust.page]