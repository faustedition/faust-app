<#assign archiveId = document.getMetadataValue("archive")>
<#assign callnumber = document.getMetadataValue("callnumber")>
<#assign waId = document.getMetadataValue('wa-id')!"">
<#assign title>${archiveId?upper_case}/ ${callnumber?html}<#if waId?has_content> &ndash; ${waId?html}</#if></#assign>
<#assign css>
	#transcript { margin: 1em auto; text-align: center }
	
	.disabled { color: #ccc }
	
	#transcript-navigation { text-align: center; font-size: 85%; margin: 1em }
	#transcript-document { margin: 1em; padding: 1em; border: 1px solid #ccc }
	#transcript-placeholder { text-align: center; color: #ccc }
	
	#transcript-browser { border: 1px solid #ccc;  background: #000; color: #ccc }
	#transcript-browser .yui3-widget-hd { padding: 0.5em 1em }
	#transcript-browser .yui3-widget-hd a { color: #ccc; }	
	#transcript-browser .yui3-widget-bd { white-space: nowrap; }
	#transcript-browser .yui3-widget-bd li { width: 150px; text-align: center }
	#transcript {height: 800px}
	#transcript-facsimile, #transcript-text {height: 100%}
	#transcript-text { overflow: auto }
	#transcript-swf { border: 1px inset grey; }
	.zone { border: 1px dashed #ccc; margin: 3em 0; padding: 1em}	
</#assign>
<#assign header>
	<script type="text/javascript" src="${cp}/static/js/swfobject.js"></script>
	<script type="text/javascript" src="${cp}/static/js/goddag.js"></script>
	<script type="text/javascript" src="${cp}/static/js/document/materialunit.js"></script>
	<script type="text/javascript" src="${cp}/static/js/document/model.js"></script>
	<script type="text/javascript" src="${cp}/static/js/document/view-svg.js"></script>	
	<script type="text/javascript" src="${cp}/static/js/document/controller.js"></script>
	<script type="text/javascript" src="${cp}/static/js/document/view.js"></script>
	
</#assign>
<@faust.page title=title css=css header=header layout="wide">
	<div class="demo-note">
		<p>This view shows the diplomatic transcript of a sample manuscript as it is currently computed from encoded sources.</p>
		
		<p>The rendering is not very accurate yet, but you can already get an idea, how we envision the navigation in multi-page manuscripts or the usage of different view modes.</p>
		
		<p>
		Please feel free to use the navigation on top of the content area to move page-by-page in the manuscript or to browse the manuscript as a whole.
		For the latter option, click on “Browse” and drag the mouse over the facsimile images to skim the portfolio. A double-click on a thumbnail image jumps to the corresponding page.
		</p>
	</div>
	<#include "document-contents.ftl"/>
</@faust.page>