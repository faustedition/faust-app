[#ftl]
[#assign title][@faust.nameOf path /][/#assign]
[@faust.page title=(title?html)]
	[@faust.breadcrumbs path "Überlieferungsträger" /]
	<div class="yui-g">
		<h2>${title?html}</h2>
		
		<pre>${document?html}</pre>	
	</div>
[/@faust.page]
