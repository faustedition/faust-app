<#assign subject><@spring.message ("report." + report.name) /></#assign>
<@faust.page title="${subject?html}">
	<h2>${subject?html}</h2>
	
	<p>Zuletzt aktualisiert am: ${report.generatedOn?datetime}</p>

	<textarea style="width: 80%" rows="25" readonly="readonly">${report.body?html}</textarea>
</@faust.page>