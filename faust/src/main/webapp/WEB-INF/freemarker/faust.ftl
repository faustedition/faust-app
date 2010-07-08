<#import "/spring.ftl" as spring>
<#assign xhtmlCompliant = true in spring>
<#assign cp = springMacroRequestContext.getContextPath()>

<#macro page title>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head profile="http://dublincore.org/documents/dcq-html/">
	<title>${title} :: faustedition.net</title>
	<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?3.1.1/build/cssreset/reset-min.css&amp;3.1.1/build/cssfonts/fonts-min.css&amp;3.1.1/build/cssgrids/grids-min.css&amp;3.1.1/build/cssbase/base-min.css" />
	<script src="http://yui.yahooapis.com/3.1.1/build/yui/yui-min.js" type="text/javascript"></script>	
	<link rel="stylesheet" type="text/css" href="${cp}/css/faust.css" />
	<link rel="stylesheet" type="text/css" href="${cp}/css/iip.css" />
	<script type="text/javascript" src="${cp}/js/faust.js"></script>
	<script type="text/javascript" src="${cp}/js/mootools-1.2-core-compressed.js"></script>
	<script type="text/javascript" src="${cp}/js/mootools-1.2-more-compressed.js"></script>
	<script type="text/javascript" src="${cp}/js/iipmooviewer-1.1.js"></script>	
	<script type="text/javascript">var cp = "${cp}"; yuiInit();</script>
	<link rel="schema.DC" href="http://purl.org/dc/elements/1.1/" />
	<link rel="schema.DCTERMS" href="http://purl.org/dc/terms/" />
	<meta name="DC.format" scheme="DCTERMS.IMT" content="application/xhtml+xml" />
	<meta name="DC.type" scheme="DCTERMS.DCMIType" content="Text" />
	<meta name="DC.publisher" content="Digitale Faust-Edition" />
	<meta name="DC.creator" content="Digitale Faust-Edition" />
	<meta name="DC.subject" content="Faust, Johann Wolfgang von Goethe, Historisch-kritische Edition, digital humanities" />
	<!-- 
	<meta name="DCTERMS.license"  scheme="DCTERMS.URI" content="http://www.gnu.org/copyleft/fdl.html" />
	<meta name="DCTERMS.rightsHolder" content="Wikimedia Foundation Inc." />
	 -->
</head>
<body class="yui-skin-sam">
<div class="yui3-d2">
<div id="header">
	<h1>${title}&nbsp;<span style="color: #764F27">::&nbsp;faustedition.net</span></h1>
	<div id="top-navigation" class="yuimenubar yuimenubarnav">
	<div class="bd">
	<ul class="first-of-type">
		<#if authAuthorities?seq_contains("ROLE_EDITOR")>
			
			<li class="yuimenubaritem"><a href="${cp}/Witness/" class="yuimenubaritemlabel"><@spring.message "menu.witness" /></a></li>
			<li class="yuimenubaritem"><a href="${cp}/text/" class="yuimenubaritemlabel"><@spring.message "menu.text" /></a></li>
			<li class="yuimenubaritem"><a href="${cp}/genesis/" class="yuimenubaritemlabel"><@spring.message "menu.genesis" /></a></li>
			<li class="yuimenubaritem"><a href="${cp}/search/" class="yuimenubaritemlabel"><@spring.message "menu.search" /></a></li>
		</#if>
		<li class="yuimenubaritem"><span class="yuimenuibartemlabel"><@spring.message "menu.project" /></span>
		<div class="yuimenu">
		<div class="bd">
		<ul>
			<li class="yuimenuitem"><a href="${cp}/project/about" class="yuimenuitemlabel"><@spring.message "menu.about" /></a></li>
			<li class="yuimenuitem"><a href="${cp}/static/dfg-grant-application.pdf" class="yuimenuitemlabel"><@spring.message "menu.grant_application" /></a></li>
		</ul>
		</div>
		</div>
		</li>
		<li class="yuimenubaritem"><span class="yuimenubaritemlabel"><@spring.message "menu.partners" /></span>
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
		<li class="yuimenubaritem"><a href="${cp}/project/contact" class="yuimenubaritemlabel"><@spring.message "menu.contact" /></a></li>
		<li class="yuimenubaritem"><a href="${cp}/project/imprint" class="yuimenubaritemlabel"><@spring.message "menu.imprint" /></a></li>
		<li class="yuimenubaritem">
			<a href="https://wuerzburg.faustedition.net/" class="yuimenubaritemlabel"><@spring.message "menu.restricted" /></a>
			<#if !authAuthorities?seq_contains("ROLE_EDITOR")>
				<div class="yuimenu">
					<div class="bd">
						<ul>
							<li class="yuimenuitem"><a href="${cp}/login" class="yuimenuitemlabel">Login</a></li>
						</ul>
					</div>
				</div>				
			</#if>
		</li>
		<#if authAuthorities?seq_contains("ROLE_EDITOR")>
			<li class="yuimenubaritem">
				<span class="yuimenubaritemlabel"><@spring.message "report" /></span>
				<div class="yuimenu">
					<div class="bd">
						<ul>
							<li class="yuimenuitem"><a href="${cp}/report/tei_validation" class="yuimenuitemlabel"><@spring.message "report.tei_validation" /></a></li>
							<li class="yuimenuitem"><a href="${cp}/report/detached_transcription_documents" class="yuimenuitemlabel"><@spring.message "report.detached_transcription_documents" /></a></li>
							<li class="yuimenuitem"><a href="${cp}/report/whitespace_normalization" class="yuimenuitemlabel"><@spring.message "report.whitespace_normalization" /></a></li>
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

<#macro tableGrid contents rows=5>
<#if contents?size gt 0>
	<tr>
	<#list contents as c>
		<#nested c>
		<#if c_has_next && ((c_index % rows) == (rows - 1))></tr><tr></#if>
	</#list>
	<#if (contents?size % rows) gt 0><#list (rows - 1)..(contents?size % rows) as remainder><td>&nbsp;</td></#list></#if>
	</tr>
</#if>
</#macro>

<#macro breadcrumbs path rootName><#compress>
<p class="node-path">
	<#local uri = '' />
	<#if path?ends_with("/")><#local path = path[0..(path?length - 2)] /></#if>
	<#list path?split("/") as p>
		<#local name><#if p_index == 0>${rootName}<#else>${p?html}</#if></#local>
		<#if p_has_next>
			<#local uri = (uri + p + "/") />
			<a href="${cp}/${encodePath(uri)}" title="${name}">${name}</a>
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