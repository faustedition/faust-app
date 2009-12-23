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
						<a class="img" href="${ctx}/document/${encodePath(c.pathComponents)}/" title="${c.name?html}">
							<img src="${ctx}/static/document/node_type_${c.nodeType?lower_case}.png" alt="${c.nodeType}" />
						</a>
						<br/>
						<a href="${ctx}/document/${encodePath(c.pathComponents)}/" title="${c.name?html}">${c.name?html}</a>
					</td>
				[/@faust.tableGrid]
			</table>

			[#if facets??]
				[#list facets as f]
					[#if (f.class.simpleName == 'FacsimileFacet') && (f.primary)]
						<div class="center" style="margin: 1em 0">
							<img src="${ctx}/facsimile/${encodePath(f.facsimileFile.path)}.jpg" width="400" />
						</div>
					[#elseif f.class.simpleName == 'TranscriptionFacet']
						<div style="margin: 1em 0">${tei2xhtml(f)}</div>
					[/#if]
				[/#list]
			[/#if]
		</div>
		<div class="yui-u">
			[#if facets??]
				[#list facets as f]
					[#if f.class.simpleName == 'ArchiveFacet']
						<h2>Archiv:</h2>
						
						<table class="border" style="width: 90%; margin: 0 5%">
							<tr class="border"><th class="right" style="width: 30%">Repository:</th>
								<td>${(f.repository!'–')?html}</td></tr>
							<tr class="border"><th class="right" style="width: 30%">Signatur:</th>
								<td>${(f.callnumber!'–')?html}</td></tr>
							<tr class="border"><th class="right" style="width: 30%">Alte Signatur:</th>
								<td>${(f.legacyCallnumber!'–')?html}</td></tr>
						</table>
					[#elseif f.class.simpleName == 'DatingFacet']
						<h2>Datierung:</h2>
						
						<table class="border" style="width: 90%; margin: 0 5%">
							<tr class="border"><th class="right" style="width: 30%">Von:</th>
								<td>[#if f.fromDate??]${f.fromDate}[#else]–[/#if]</td></tr>
							<tr class="border"><th class="right" style="width: 30%">Bis:</th>
								<td>[#if f.toDate??]${f.toDate}[#else]–[/#if]</td></tr>
							<tr class="border"><th class="right" style="width: 30%">Bemerkungen:</th>
								<td>${(f.remarks!'–')?html}</td></tr>
						</table>					
					[#elseif f.class.simpleName == 'PrintReferenceFacet']
						<h2>Bibliographische Angaben:</h2>

						<table class="border" style="width: 90%; margin: 0 5%">
							<tr class="border"><th class="right" style="width: 30%">WA:</th>
								<td>${(f.referenceWeimarerAusgabe!'–')?html}</td></tr>
							<tr class="border"><th class="right" style="width: 30%">WA-Manuskript:</th>
								<td>${(f.manuscriptReferenceWeimarerAusgabe!'–')?html}</td></tr>
							<tr class="border"><th class="right" style="width: 30%">WA-Paralipomenon:</th>
								<td>${(f.paralipomenonReferenceWeimarerAusgabe!'–')?html}</td></tr>
						</table>		
					[#elseif f.class.simpleName == 'LegacyMetadataFacet']
						<h2>Allegro-Datenbank:</h2>

						<table class="border" style="width: 90%; margin: 0 5%">
							<tr class="border"><th class="right" style="width: 30%">Textsorte:</th>
								<td>${(f.geneticLevel!'–')?html}</td></tr>
							<tr class="border"><th class="right" style="width: 30%">Hand:</th>
								<td>${(f.hands!'–')?html}</td></tr>
							<tr class="border"><th class="right" style="width: 30%">Bemerkungen:</th>
								<td>${(f.remarks!'–')?html}</td></tr>
							<tr class="border"><th class="right" style="width: 30%">Datensatz-#:</th>
								<td>${(f.recordNumber!'–')?html}</td></tr>
						</table>		
					[/#if]
				[/#list]
			[/#if]

			<h2>Transkriptionen</h2>

			<table class="border" style="width: 90%; margin: 0 5%">
				[#list transcriptionStatus?keys as s]
					<tr class="border">
						<th class="right" style="width: 30%">[@spring.message ("transcriptionStatus." + s) /]:</th>
						<td>${transcriptionStatus[s]}</td>
					</tr>
				[/#list]
			</table>		
		</div>
	</div>
[/@faust.page]
