<#assign title>XML Query</#assign>
<#assign css>
</#assign>
<#assign header>
</#assign>
<@faust.page title=title css=css header=header>
	
	<div>
		<form action="/xml-query" method="get">
		<fieldset>
		<legend>Folder</legend>
		<input type="radio" name="folder" value="transcript"<#if folder?? && folder="transcript">checked="true"</#if>>transcript</input>
		<input type="radio" name="folder" value="document" <#if folder?? && folder="document">checked="true"</#if>>document</input>
		<input type="radio" name="folder" value="edition" <#if folder?? && folder="edition">checked="true"</#if>>edition</input>
		<input type="radio" name="folder" value="attic" <#if folder?? && folder="attic">checked="true"</#if>>attic</input>
		<input type="radio" name="folder" value="macrogenesis" <#if folder?? && folder="macrogenesis">checked="true"</#if>>macrogenesis</input>
		<input type="radio" name="folder" value="readings" <#if folder?? && folder="readings">checked="true"</#if>>readings</input>
		<input type="radio" name="folder" value="attic" <#if folder?? && folder="attic">checked="true"</#if>>testimony</input>	
		</fieldset>
		<fieldset>
		<legend>XPath Expression</legend>
		<input name="xpath" value="${(xpath!"")?html}"></input>
		</fieldset>
		<fieldset>
		<legend>Display Mode</legend>
		<input type="radio" name="mode" value="xml"<#if mode?? && mode="xml">checked="true"</#if>>XML nodes</input>
		<input type="radio" name="mode" value="files"<#if mode?? && mode="files">checked="true"</#if>>Files</input>
		<input type="radio" name="mode" value="values"<#if mode?? && mode="values">checked="true"</#if>>Unique values</input>
		</fieldset>
		<input type="submit" value="Search"></input>	
		</form>
	<#if xpath??>
	<h2>Results</h2>
		<div id="results">
		<#if mode = "xml" || mode = "files">
		<#if (files?size = 0)><h3>Nothing found.</h3></#if>	
		<#list files as file>
				<h3><a target="_blank" href="${faust.resolveUri(file.uri)}">${file.uri}</a></h3>
				<p>
				  <#if file.results?? && mode = "xml">
				     <#list file.results as node>
				       <pre>${node?html}</pre>
				     </#list>
				  </#if>
				  <#if file.error??><pre>${file.error}</pre></#if>
				</p>
		</#list>
		</#if>
		<#if mode = "values">
			<#if (values?size = 0)><h3>Nothing found.</h3></#if>	
			<#list values as value>
			<p><pre>${value}</pre></p>
			</#list>
		</#if>
		
		</div>
	</#if>
	</div>	
	
	
	<script type="text/javascript">
		var Y = Faust.YUI().use("node", "dom", "dump", "io", "json", "event", "overlay", "scrollview", function(Y) {
			});
	</script>
</@faust.page>