[#ftl]
[@faust.page title=(repository.name?html)]
	<h2>${repository.name?html} [@faust.uplink url=(ctx + "/manuscripts/") title="Bestandsübersicht" /]</h2>

	<table class="no-border" style="width: 90%; margin: 0 5%">
		[@faust.tableGrid portfolioList ; p]
			<td style="width: 20%" class="center no-border">
				<a href="${ctx}/manuscripts/${repository.name?url}/${p.name?url}/">${p.name?url}</a>
			</td>				
		[/@faust.tableGrid]
	</table>
[/@faust.page]