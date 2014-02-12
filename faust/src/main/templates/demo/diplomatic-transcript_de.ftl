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
	<script type="text/javascript" src="${cp}/static/lib/mootools-1.2-core-compressed.js"></script>
	<script type="text/javascript" src="${cp}/static/lib/mootools-1.2-more-compressed.js"></script>
	<script type="text/javascript" src="${cp}/static/lib/swfobject.js"></script>
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
		In dieser Ansicht sehen Sie eine diplomatische Umschrift, hergestellt in der graphischen Qualität, die wir letztlich für unsere Ausgabe anstreben.
		Die Umschrift ist nicht auf Basis eines kodierten Transkripts berechnet worden, sondern wurde von Hand erstellt, um einige besondere Funktionen hervorheben zu können.
		So können Textsegmente frei platziert, orientiert und graphische Markierung können möglichst genau nachgebildet werden.
		Zudem interagiert die Darstellung mit dem Benutzer.
		</p>
		
		<p>
		Zum Beispiel können Sie auf den überblendeten Text oben in der Umschrift klicken, um diesen ein- oder auszublenden;
		ein Klick auf die gedreht dargestellte Marginalie am linken Rand fokussiert die Faksimileanzeige im ihr entsprechenden Bildbereich.
		</p>
	</div>
	<#include "diplomatic-transcript-contents.ftl"/>
</@faust.page>