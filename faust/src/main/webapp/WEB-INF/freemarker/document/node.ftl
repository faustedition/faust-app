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
	<div class="yui-g">
		<div class="yui-u first">
			<h2>${nodeTitle?html}</h2>
	
			<table class="border" style="width: 90%; margin: 0 5%">
				[@faust.tableGrid children ; c]
					<td style="width: 20%" class="center no-border">
						<a class="img" href="${ctx}/document/${encodePath(c.pathComponents)}/" title="${c.name?html}"><img src="${ctx}/static/document/node_type_${c.nodeType?lower_case}.png" alt="${c.nodeType}" /></a><br/>
						<a href="${ctx}/document/${encodePath(c.pathComponents)}/" title="${c.name?html}">${c.name?html}</a>
					</td>
				[/@faust.tableGrid]
			</table>
		</div>
		<div class="yui-u">
			[#if facets??]
				[#list facets as f]
					[#if f.class.simpleName == 'ArchiveFacet']
						<h2>Archiv:</h2>
						
						<table class="border" style="width: 90%; margin: 0 5%">
							<tr class="border"><th class="right">Repository:</th><td>${(f.repository!'–')?html}</td></tr>
							<tr class="border"><th class="right">Signatur:</th><td>${(f.callnumber!'–')?html}</td></tr>
							<tr class="border"><th class="right">Alte Signatur:</th><td>${(f.legacyCallnumber!'–')?html}</td></tr>
						</table>
					[#elseif f.class.simpleName == 'DatingFacet']
						<h2>Datierung:</h2>
						
						<table class="border" style="width: 90%; margin: 0 5%">
							<tr class="border"><th class="right">Von:</th><td>[#if f.fromDate??]${f.fromDate}[#else]–[/#if]</td></tr>
							<tr class="border"><th class="right">Bis:</th><td>[#if f.toDate??]${f.toDate}[#else]–[/#if]</td></tr>
							<tr class="border"><th class="right">Bemerkungen:</th><td>${(f.remarks!'–')?html}</td></tr>
						</table>					
					[/#if]
				[/#list]
			[/#if]
		</div>
	</div>
[/@faust.page]