<#assign header>
	<script type="text/javascript" src="${cp}/static/lib/protovis-r3.2.js"></script>
</#assign>
<@faust.page title=message("genesis.sample") header=header>
	<h2>${message("genesis.sample")}</h2>

	<div class="demo-note">
		<p>The following diagram showcases an exemplary visualization of genetic relations between manuscripts.</p>
		
		<p>
		On the vertical axis you see one line for each involved manuscript. The further down a manuscript is displayed in the chart, the earlier we assume it to be created.
		On the horizontal axis you see a sample verse interval, each verse numbered in a standard way.
		The bars denote verse intervals realized in a certain witness and how these intervals relate along the syntagmatic and paradigmatic axis of the emerging text.
		</p>
	</div>
	<#include "genesis-content.ftl"/>
</@faust.page>