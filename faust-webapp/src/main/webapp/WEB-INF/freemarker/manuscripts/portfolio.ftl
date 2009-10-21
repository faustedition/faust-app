[#ftl]
[@faust.page title=(portfolio.name?html)]
	<div class="yui-g">
	<div class="yui-u first">
		<h2>Portfolio ${portfolio.name?html} [@faust.uplink url=(ctx + "/manuscripts/" + encodePath(repository.name) + "/") title="Bestandsübersicht" /]</h2>
	
		<table class="no-border" style="margin: 0 5%">
			[@faust.tableGrid manuscriptList ; m]
				<td style="width: 20%" class="center no-border">
				<div class="center">
					<p>
						<a href="${ctx}/manuscripts/${repository.name?url}/${portfolio.name?url}/${m.name?url}" title="Zum Manuskript">
							[#-- TODO: GSA-abhängiger Faksimile-Pfad --]
							<img src="${ctx}/facsimile/${encodePath(repository.name + '/' + portfolio.name + '/' + portfolio.name + '_' + m.name)}_thumb.jpg" alt="${m.name?html}" />
						</a>
					</p>
					<p>${m.name?html}</p>
				</div>
				</td>		
			[/@faust.tableGrid]
		</table>
	</div>
	<div class="yui-u">
		[#if !(metadataTable.empty)]
			<h2>Metadaten</h2>
			
			<table class="data-table">
				${metadataTable.toHtmlContent()}
			</table>
		[/#if]
	</div>
	</div>
[/@faust.page]