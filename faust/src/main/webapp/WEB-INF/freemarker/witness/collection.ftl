<#assign title><#if path == 'Witness/'>Überlieferungsträger<#else><@faust.nameOf path /></#if></#assign>
<@faust.page title=(title?html)>
	<@faust.breadcrumbs path "Überlieferungsträger" />
	<div class="yui3-gc">
		<div class="yui3-u first">
			<h2>${title?html}</h2>
	
			<table style="width: 90%; margin: 0 5%">
				<@faust.tableGrid contents ; c>
					<#assign name><@faust.nameOf c.path /></#assign>
					<td style="width: 20%; border: 0; text-align: center">
						<a class="img" href="${cp}/${c?string}" title="${name?html}">
							<img src="${cp}/img/witness/<#if c?string?ends_with("/")>collection<#else>witness</#if>.png" alt="[Typ-Icon]" />
						</a>
						<br/>
						<a href="${cp}/${c?string}" title="${name?html}">${name?html}</a>
					</td>
				</@faust.tableGrid>
			</table>
		</div>
		<div class="yui3-u">
			<div id="encoding-status" style="padding: 2em">&#160;</div>
		</div>
		<script type="text/javascript">
			function initPage() {
				encodingStatus('${path?js_string}');
			}
		</script>
	</div>
</@faust.page>
