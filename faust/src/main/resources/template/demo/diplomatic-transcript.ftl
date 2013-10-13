<#assign css>
	#transcript { margin-bottom: 2em; }
	
	.disabled { color: #ccc }
	
	#transcript-navigation { text-align: center; font-size: 85% }
	#transcript-document { margin: 1em; padding: 1em; border: 1px solid #ccc }
	#transcript-placeholder { text-align: center; color: #ccc }
	
	#transcript-browser { border: 1px solid #ccc;  background: #000; color: #ccc }
	#transcript-browser .yui3-widget-hd { padding: 0.5em 1em }
	#transcript-browser .yui3-widget-hd a { color: #ccc; }	
	#transcript-browser .yui3-widget-bd { white-space: nowrap; }
	#transcript-browser .yui3-widget-bd li { width: 150px; text-align: center }
	
	#transcript-canvas { border: 2px inset grey; width: 450px; height: 600px }
	.zone { border: 1px dashed #ccc; margin: 3em 0; padding: 1em}	
</#assign>
<#assign header>
	<link rel="stylesheet" type="text/css" href="${cp}/static/css/facsimile.css">
	<script type="text/javascript" src="${cp}/static/js/mootools-1.2-core-compressed.js"></script>
	<script type="text/javascript" src="${cp}/static/js/mootools-1.2-more-compressed.js"></script>
	<script type="text/javascript" src="${cp}/static/js/swfobject.js"></script>
	<script type="text/javascript" src="${cp}/static/js/goddag.js"></script>
	<script type="text/javascript" src="${cp}/static/js/document/facsimile.js"></script>
	<script type="text/javascript" src="${cp}/static/js/document/materialunit.js"></script>
	<script type="text/javascript" src="${cp}/static/js/document/model.js"></script>
	<script type="text/javascript" src="${cp}/static/js/document/view.js"></script>
	<script type="text/javascript" src="${cp}/static/js/demo/diplomatic-transcript.js"></script>
</#assign>
<@faust.page title=message('demo.diplomatic_transcript.title') css=css header=header>
	<div class="demo-note">
		<p>
		This view shows a diplomatic transcript in the graphical quality we aim for in our publication.
		It is not computed based on an encoded transcript but handcrafted to showcase some features like arbitrary placement and orientation of text segments, free usage of graphic strokes and shapes as well as interactivity.
		</p>
		
		<p>
		For example, in the transcript, please click on the superimposed text segment on the top to change its visibility or click on rotated marginal note on the left to focus the facsimile display on the corresponding region.
		</p>
	</div>
	<#include "diplomatic-transcript-contents.ftl"/>
</@faust.page>