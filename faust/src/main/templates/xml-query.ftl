<#assign title>XML Query</#assign>
<#assign css>
</#assign>
<#assign header>
</#assign>
<@faust.page title=title css=css header=header>
	
	<div>
		XML Query.
		<form action="/xml-query" method="get">
		XPath: <input name="xpath"></input>
		<input type="submit" value="Submit"></input>
		</form>
		</h2>Results</h2> 
		<div id="results">
		<#list results as result>
				<h3>${result.url}</h3>
				<p>
				  <#list result.results as node>
				    <pre>${node}</pre>
				  </#list>
				</p>
		</#list>		
		
		</div>
	</div>	
	
	
	<script type="text/javascript">
		var Y = Faust.YUI().use("node", "dom", "dump", "io", "json", "event", "overlay", "scrollview", function(Y) {
			});
	</script>
</@faust.page>