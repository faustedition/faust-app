<#assign header>
	<script type="text/javascript" src="${cp}/static/js/protovis-r3.2.js"></script>
</#assign>
<@faust.page title=message("genesis.sample") header=header>
	<h2>${message("genesis.sample")}</h2>

	<div class="demo-note">
		<p>Das folgende Diagramm zeigt eine beispielhafte Visualisierung textgenetischer Bezüge zwischen Handschriften.</p>
		
		<p>
		Auf der vertikalen (paradigmatischen) Achse sind die betreffenden Handschriften abgetragen. Je weiter unten eine Handschrift abgetragen wird, desto früher wird ihre Entstehung angenommen.
		Auf der horizontalen (syntagmatischen) Achse werden Versintervalle abgetragen.
		Die Balken repräsentieren in der jeweiligen Handschrift realisierte Versintervalle und deren genetische Beziehungen zueinander.
		</p>
	</div>
	<#include "genesis-content.ftl"/>
</@faust.page>