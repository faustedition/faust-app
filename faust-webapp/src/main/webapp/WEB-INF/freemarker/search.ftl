<@page "Suche">
	<h2>Suche</h2>

	<form action="${ctx}/search" method="post">
		<p id="search-input">
			<@spring.formInput "search.query" />
			<input type="submit" value="Suchen" />
		</p>
	</form>
	
	<#if search.results != null>
		<h3>Suchergebnisse</h3>
		
		<p class="notice">${search.totalResults} Ergebnisse insgesamt</p>
		
		<#list search.results as result>
			<p class="search-result">
				${result.node.fullPath?html}
			</p>
		</#list>
	</#if>
	
</@page>