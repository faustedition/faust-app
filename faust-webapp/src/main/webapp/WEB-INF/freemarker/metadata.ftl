<@page "Homepage">
	<h2>
		<#if node.name?length gt 0><img src="${ctx}/static/gfx/nodetype_${node.nodeType?lower_case}.png" alt="Objekttyp" /> ${node.name?html}<#else>Digitale Faustedition</#if>
	</h2>

	<#if node.name?length gt 0>
		<p id="path">
			<a href="${ctx}/metadata/" title="Metadaten">Digitale Faustedition</a>
			<#if node.path?length gt 0>
				<#assign path = "" />
				<#list node.pathComponents as pc>
					<#assign path>${path}<#if path?length gt 0>/</#if>${pc}</#assign>
					&rArr; <a href="${ctx}/metadata/${path?url?replace('%2F', '/')}/" title="Metadaten">${pc?html}</a>
				</#list>
			</#if>
		</p>
	</#if>
	
	<h3>Metadaten</h3>
	
	<#if metadata.metadataValues?size gt 0>
		<table class="metadata">
			<#assign groupName = '' />
			<#list metadata.metadataValues as mv>
				<tr>
					<#if groupName != mv.field.group.name>
						<#assign groupName = mv.field.group.name />
						<th class="metadata-field-group" rowspan="${metadata.groupSizes[mv.field.group.name]}"><@spring.message ('metadata_group.' + mv.field.group.name) /></th>
					</#if>
					<th class="metadata-field"><@spring.message ('metadata.' + mv.field.name) />:</th>
					<td class="metadata-value">${mv.value?html?replace('\n', '<br/>')}</td>
				</tr>
			</#list>
		</table>
	<#else>
		<p class="notice">Diesem Objekt wurden bislang keine Metadaten zugewiesen.</p>
	</#if>
	
	<#if children?size gt 0>
		<h3>Untergeordnete Objekte</h3>
		
		<ul class="children-list">
			<#list children as child>
				<li>
					<img src="${ctx}/static/gfx/nodetype_${child.nodeType?lower_case}.png" alt="Objekttyp" />
					<a href="${ctx}/metadata/${child.fullPath?url?replace('%2F', '/')}/" title="Zum Objekt">${child.name?html}</a>
				</li>
			</#list>
		</ul>
	</#if>
</@page>