[#ftl]
[#assign nodeTitle][#if node??]${node.name}[#else]Dokumente[/#if][/#assign]
[@faust.page title=(nodeTitle?html)]
	[#if node?? || parentPath??]
		<p class="node-path">
			[#if node??]<a href="${ctx}/document/" title="Dokumente">Dokumente</a> <strong>&gt;</strong>[/#if]
			[#if parentPath??]
				[#list parentPath?values as p]
					<a href="${ctx}/document/${encodePath(p.pathComponents)}/" title="${p.name?html}">${p.name?html}</a> <strong>&gt;</strong>
				[/#list]
			[/#if] 
		</p>
	[/#if]
	<h2>${nodeTitle?html}</h2>
	
	<table class="no-border" style="width: 90%; margin: 0 5%">
		[@faust.tableGrid children ; c]
			<td style="width: 20%" class="center no-border">
				<a class="img" href="${ctx}/document/${encodePath(c.pathComponents)}/" title="${c.name?html}"><img src="${ctx}/static/document/node_type_${c.nodeType?lower_case}.png" alt="${c.nodeType}" /></a><br/>
				<a href="${ctx}/document/${encodePath(c.pathComponents)}/" title="${c.name?html}">${c.name?html}</a>
			</td>				
		[/@faust.tableGrid]
	</table>
[/@faust.page]