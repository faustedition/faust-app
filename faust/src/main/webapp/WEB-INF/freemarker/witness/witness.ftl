[#ftl]
[#assign title][@faust.nameOf path /][/#assign]
[@faust.page title=(title?html)]
	[@faust.breadcrumbs path "Überlieferungsträger" /]
	
	<h2>${title?html}</h2>
	
	<div class="yui-gc">
		<div class="yui-u first">
			[#if facsimile??]
				<img src="${ctx}/facsimile/${encodePath(facsimile.path)}.jpg" id="facsimile" style="max-width: 500px"/>
			[/#if]
		</div>
		<div class="yui-u">${htmlTranscription}</div>	
	</div>
[/@faust.page]
