<#assign cp = config["ctx.path"]>
<#if cp?ends_with("/")><#assign cp = cp?substring(0, cp?last_index_of("/"))></#if>

<#macro page title css="" header="">
<!DOCTYPE html>
<html>
<head>
	<title>${title} :: faustedition.net</title>
	<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?3.1.1/build/cssfonts/fonts-min.css&3.1.1/build/cssreset/reset-min.css&3.1.1/build/cssgrids/grids-min.css&3.1.1/build/cssbase/base-min.css">
	<script type="text/javascript" src="http://yui.yahooapis.com/combo?3.1.1/build/yui/yui-debug.js&3.1.1/build/oop/oop-debug.js&3.1.1/build/dom/dom-debug.js&3.1.1/build/dump/dump-debug.js&3.1.1/build/event-custom/event-custom-base-debug.js&3.1.1/build/event/event-debug.js&3.1.1/build/pluginhost/pluginhost-debug.js&3.1.1/build/node/node-debug.js&3.1.1/build/querystring/querystring-stringify-simple-debug.js&3.1.1/build/queue-promote/queue-promote-debug.js&3.1.1/build/datatype/datatype-xml-debug.js&3.1.1/build/io/io-debug.js&3.1.1/build/json/json-debug.js"></script>	<link rel="stylesheet" type="text/css" href="${cp}/css/faust.css">
	<script type="text/javascript">var cp = "${cp}";</script>
	<script type="text/javascript" src="${cp}/js/faust.js"></script>
	<link rel="schema.DC" href="http://purl.org/dc/elements/1.1/">
	<link rel="schema.DCTERMS" href="http://purl.org/dc/terms/">
	<meta name="DC.format" scheme="DCTERMS.IMT" content="text/html">
	<meta name="DC.type" scheme="DCTERMS.DCMIType" content="Text">
	<meta name="DC.publisher" content="Digitale Faust-Edition">
	<meta name="DC.creator" content="Digitale Faust-Edition">
	<meta name="DC.subject" content="Faust, Johann Wolfgang von Goethe, Historisch-kritische Edition, digital humanities">
	<!-- 
	<meta name="DCTERMS.license"  scheme="DCTERMS.URI" content="http://www.gnu.org/copyleft/fdl.html">
	<meta name="DCTERMS.rightsHolder" content="Wikimedia Foundation Inc.">
	 -->
	 <#if css?has_content><style type="text/css">${css}</style></#if>
	 <#if header?has_content>${header}</#if>
</head>
<body class="yui-skin-sam">
<div class="yui3-d2">
<div id="header">
	<h1>${title}&#160;<span class="color-1">::&#160;faustedition.net</span></h1>
	<div id="top-navigation" class="yuimenubar yuimenubarnav">
	<div class="bd">
	<ul class="first-of-type">
		<#if roles?seq_contains("editor")>			
			<li class="yuimenubaritem"><a href="${cp}/archive/" class="yuimenubaritemlabel">${message("menu.archives")}</a></li>
			<li class="yuimenubaritem">
				<a href="${cp}/Witness/" class="yuimenubaritemlabel">${message("menu.witness")}</a>
				<div class="yuimenu">
				<div class="bd">
				<ul>
					<li class="yuimenuitem"><a href="${cp}/visualization/style_catalogue" class="yuimenuitemlabel">${message("menu.visualization.style_catalogue")}</a></li>
				</ul>
				</div>
				</div>
			</li>
			<li class="yuimenubaritem"><a href="${cp}/text/" class="yuimenubaritemlabel">${message("menu.text")}</a></li>
			<li class="yuimenubaritem"><a href="${cp}/genesis/" class="yuimenubaritemlabel"${message("menu.genesis")}></a></li>
			<#-- <li class="yuimenubaritem"><a href="${cp}/search/" class="yuimenubaritemlabel">${message("menu.search")}</a></li> -->
		</#if>
		<li class="yuimenubaritem"><span class="yuimenuibartemlabel">${message("menu.project")}</span>
		<div class="yuimenu">
		<div class="bd">
		<ul>
			<li class="yuimenuitem"><a href="${cp}/project/about" class="yuimenuitemlabel">${message("menu.about")}</a></li>
			<li class="yuimenuitem"><a href="${cp}/static/dfg-grant-application.pdf" class="yuimenuitemlabel">${message("menu.grant_application")}</a></li>
		</ul>
		</div>
		</div>
		</li>
		<li class="yuimenubaritem"><span class="yuimenubaritemlabel">${message("menu.partners")}</span>
		<div class="yuimenu">
		<div class="bd">
		<ul>
			<li class="yuimenuitem"><a href="http://www.goethehaus-frankfurt.de/" class="yuimenuitemlabel">Freies Deutsches Hochstift Frankfurt</a></li>
			<li class="yuimenuitem"><a href="http://www.klassik-stiftung.de/" class="yuimenuitemlabel">Klassik Stiftung Weimar</a></li>
			<li class="yuimenuitem"><a href="http://www.uni-wuerzburg.de/" class="yuimenuitemlabel">Julius-Maximilians-Universität Würzburg</a></li>
		</ul>
		</div>
		</div>
		</li>
		<li class="yuimenubaritem"><a href="${cp}/project/contact" class="yuimenubaritemlabel">${message("menu.contact")}</a></li>
		<li class="yuimenubaritem"><a href="${cp}/project/imprint" class="yuimenubaritemlabel">${message("menu.imprint")}</a></li>
		<li class="yuimenubaritem">
			<a href="https://wuerzburg.faustedition.net/" class="yuimenubaritemlabel">${message("menu.restricted")}</a>
			<#if !roles?seq_contains("editor")>
				<div class="yuimenu">
					<div class="bd">
						<ul>
							<li class="yuimenuitem"><a href="${cp}/login" class="yuimenuitemlabel">Login</a></li>
						</ul>
					</div>
				</div>				
			</#if>
		</li>
		<#if roles?seq_contains("editor")>
			<li class="yuimenubaritem">
				<span class="yuimenubaritemlabel">${message("report")}</span>
				<div class="yuimenu">
					<div class="bd">
						<ul>
							<li class="yuimenuitem"><a href="${cp}/report/tei_validation" class="yuimenuitemlabel">${message("report.tei_validation")}</a></li>
							<li class="yuimenuitem"><a href="${cp}/report/detached_transcription_documents" class="yuimenuitemlabel">${message("report.detached_transcription_documents")}</a></li>
							<li class="yuimenuitem"><a href="${cp}/report/whitespace_normalization" class="yuimenuitemlabel">${message("report.whitespace_normalization")}</a></li>
						</ul>
					</div>
				</div>
			</li>
		</#if>		
	</ul>
	</div>
	</div>
</div>
<div id="main">
	<#nested>
</div>
<div id="footer">
	<p>Digitale Faust-Edition. Copyright (c) 2009 Freies Deutsches Hochstift Frankfurt, Klassik Stiftung Weimar, Universität Würzburg.</p>
</div>
</div>
</body>
</html>
</#macro>

<#macro uplink url title="">
<a href="${url}"<#if title?has_content> title="${title}"</#if>><img src="${cp}/img/arrow_up.png"<#if title?has_content> alt="${title}"</#if> /></a>
</#macro>

<#macro tableGrid contents rows=4>
<#if contents?size gt 0>
	<tr>
	<#list contents as c>
		<#nested c>
		<#if c_has_next && ((c_index % rows) == (rows - 1))></tr><tr></#if>
	</#list>
	<#if (contents?size % rows) gt 0><#list (rows - 1)..(contents?size % rows) as remainder><td>&#160;</td></#list></#if>
	</tr>
</#if>
</#macro>

<#macro breadcrumbs path rootName><#compress>
<p class="node-path">
	<#local uri = '' />
	<#if path?ends_with("/")><#local path = path[0..(path?length - 1)] /></#if>
	<#list path?split("/") as p>
		<#local name><#if p_index == 0>${rootName}<#else>${p?html}</#if></#local>
		<#if p_has_next>
			<#local uri = (uri + p + "/") />
			<a href="${cp}/${uri?url?replace("%2F", "/")}" title="${name}">${name}</a>
		</#if>
		<#if p_has_next><strong>&gt;</strong></#if>
		<#if !p_has_next>${name}</#if>
	</#list>
</p>
</#compress></#macro>

<#macro nameOf path><#compress>
		<#if path?ends_with("/")><#local path = path[0..(path?length - 2)] /></#if>
		${path[(path?last_index_of("/") + 1)..]}
</#compress></#macro>

<#macro googleMaps>
	<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>
</#macro>