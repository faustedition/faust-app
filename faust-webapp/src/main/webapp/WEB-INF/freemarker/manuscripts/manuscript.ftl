[#ftl]
[@faust.page title=((portfolio.name + '//' + manuscript.name)?html)]
	<h2>${(portfolio.name + '//' + manuscript.name)?html} [@faust.uplink url=(ctx + "/manuscripts/" + encodePath(repository.name + "/" + portfolio.name) + "/") title="Portfolioübersicht" /]</h2>

	<div class="yui-g">
	<div class="yui-u first">
		<p class="small-font italic" ><strong>Tipp:</strong> Zum Vergrößern klicken Sie bitte auf das Faksimile.</p>
		<p><a href="${ctx}/facsimile/${encodePath(facsimile.imagePath)}.jpg" wicket:id="facsimileLink" title="Vergrößern"><img wicket:id="facsimile" src="${ctx}/facsimile/${encodePath(facsimile.imagePath)}.jpg" width="400" /></a></p>
	</div>
	<div class="yui-u">
		<div style="font-family: tahoma; padding: 0.5em; margin: 0.5em">${htmlTranscription}</div>
	</div>
	</div>

	<div class="border small-font" style="color: #999; padding: 0.5em; margin: 0.5em">
		<pre>${transcriptionSource?html}</pre>
	</div>
[/@faust.page]