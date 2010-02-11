[#ftl]
[@faust.page title=(path[1..]?html)]
	<p class="node-path">
		[#assign cp = '/']
		[#list path?split("/") as p]
			[#if p == ""]
				<a href="${ctx}/repository/" title="Repository">Repository</a>
			[#elseif p_has_next]
				[#assign cp = (cp + p + "/") /]
				<a href="${ctx}/repository${encodePath(cp)}" title="${p?html}">${p?html}</a>
			[/#if]
			[#if p_has_next]<strong>&gt;</strong>[/#if]
			[#if !p_has_next]${p?html}[/#if]
		[/#list]
		
	</p>
	<div class="yui-g">
		<h2>${path?html}</h2>
		
		<pre>${document?html}</pre>	
	</div>
[/@faust.page]
