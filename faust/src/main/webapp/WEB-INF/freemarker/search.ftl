<@faust.page title="Suche">
	<div>
	<form action="${cp}/search" method="get">
		<p><@spring.formInput "searchCommand.query" 'size="40"' /></p>
		<p><input type="submit" value="Suchen" /></p>
	</form>
	</div>

	<#if searchResultList??>
		<div>
			<#list searchResultList as r>
				<p>${r.description?html}</p>
			</#list>
		</div>
	</#if>
</@faust.page>