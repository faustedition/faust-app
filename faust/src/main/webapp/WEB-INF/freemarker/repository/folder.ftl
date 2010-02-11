[#ftl]
[#assign nodeTitle][#if path == '/']Repository[#else]${path[1..]}[/#if][/#assign]
[@faust.page title=(nodeTitle?html)]
	[#if path != '/']
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
	[/#if]
	<div class="yui-g">
		<div class="yui-u first">
			<h2>${nodeTitle?html}</h2>
	
			[#if path == '/'][#assign path = "" /][/#if]
			<table class="border" style="width: 90%; margin: 0 5%">
				[@faust.tableGrid repositoryFolderContentList ; c]
					<td style="width: 20%" class="center no-border">
						<a class="img" href="${ctx}/repository${encodePath(path + '/' + c.name)}/" title="${c.name?html}">
							<img src="${ctx}/img/repository/type_${c.type?lower_case}.png" alt="[Typ-Icon]" />
						</a>
						<br/>
						<a href="${ctx}/repository${encodePath(path + '/' + c.name)}/" title="${c.name?html}">${c.name?html}</a>
					</td>
				[/@faust.tableGrid]
			</table>
		</div>
		<div class="yui-u">
			<div id="encoding-status" style="padding: 2em">&nbsp;</div>
		</div>
		<script type="text/javascript">
			function initPage() {
				encodingStatus('${path?js_string}');
			}
		</script>
	</div>
[/@faust.page]
