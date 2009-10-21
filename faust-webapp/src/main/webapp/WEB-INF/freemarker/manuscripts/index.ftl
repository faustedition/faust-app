[#ftl]
[@faust.page title="Bestände"]
	<h2>Bestände</h2>

	[#if repositoryList?has_content]
		<ul>
			[#list repositoryList as r]
				<li><a href="${ctx}/manuscripts/${r.name?url}">${r.name?html}</a></li>
			[/#list]
		</ul>
	[#else]
	[/#if]
[/@faust.page]