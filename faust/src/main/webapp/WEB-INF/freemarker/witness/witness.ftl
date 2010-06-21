<#assign title><@faust.nameOf path /></#assign>
<@faust.page title=(title?html)>
	<@faust.breadcrumbs path "Überlieferungsträger" />
	
	<h2>
		${title?html}
		<a href="${cp}/${encodePath(path)}?format=pdf" title="PDF-Test"><img src="${cp}/img/pdf-logo.png" alt="PDF-Test"/></a>
		<a href="${cp}/${encodePath(path)}?format=svg" title="SVG-Test"><img src="${cp}/img/svg-logo.png" alt="SVG-Test"/></a>		
	</h2>
	
	<div class="yui-gc">
		<div class="yui-u first">
			<#--
			<#if facsimile??>			
				<p><a href="${cp}/facsimile/${encodePath(facsimile.path)}.jpg" title="Vergrößern">
					<img src="${cp}/facsimile/${encodePath(facsimile.path)}.jpg" id="facsimile" style="max-width: 500px"/>
				</a></p>
			</#if>
			-->
			<div id="facsimile" style="position: relative; left: 0; top: 0; width: 500px; height: 500px"></div>
			<script type="text/javascript">
    				iip = new IIP( "facsimile", {
						image: 'test.tif',
						server: '${cp}/facsimile/iip',
						credit: '&copy; Digitale Faust-Edition', 
						zoom: 1,
						render: 'spiral',
						showNavButtons: true
    						});
    			</script>
		</div>
		<div class="yui-u">
			${htmlTranscription}
		</div>	
	</div>
</@faust.page>
