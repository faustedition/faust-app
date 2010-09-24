<#ftl ns_prefixes={ "D":"http://www.faustedition.net/ns" }>
<#import "snippets.ftl" as snippets />
<#assign css>
	#archive_map { text-align: center; margin: 0 auto; width: 400px; height: 200px }
</#assign>
<#assign header>
	<@faust.googleMaps />
	<script type="text/javascript" src="${cp}/static/js/archive.js"></script>
	<script type="text/javascript"></script>
</#assign>
<@faust.page title=(archive.name?html) header=header css=css>
	<div class="yui3-g">
		<div class="yui3-u-1-2 first">
			<@snippets.archiveData archive />
		</div>
		<div class="yui3-u" id="archive_map_slot"></div>
	</div>
	<script type="text/javascript">
		var Y = FaustYUI().use("node", "dom", "event", function(Y) { 
			archiveLocation('${archive.geolocation.@lat?js_string}', '${archive.geolocation.@lng?js_string}');
 		});
	</script>		
</@faust.page>