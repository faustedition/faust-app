[#ftl]
[#import "/spring.ftl" as spring]
[#assign xhtmlCompliant = true in spring]
[#assign ctx = springMacroRequestContext.getContextPath()]

[#macro page title]
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head profile="http://dublincore.org/documents/dcq-html/">
	<meta http-equiv="Content-Type" content="application/xhtml+xml;charset=UTF-8" />
	<title>${title} :: Digitale Faust-Edition</title>
	[@yuiHeader/]
    <script type="text/javascript" src="${ctx}/static/faust.js"></script>
    <link rel="stylesheet" type="text/css" href="${ctx}/static/faust.css" />
    <script type="text/javascript">var ctx = "${ctx}"; yuiInit();</script>
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
<div id="doc2" class="yui-t7">
<div id="hd">
	<h1><span style="color: #764F27">faustedition.net&nbsp;::</span>&nbsp;${title}</h1>
	<div id="top-navigation" class="yuimenubar yuimenubarnav">
	<div class="bd">
	<ul class="first-of-type">
		[#if hasRole("ROLE_EDITOR")]
			<li class="yuimenubaritem"><a href="${ctx}/document/" class="yuimenubaritemlabel">Dokumente</a></li>
			<li class="yuimenubaritem"><a href="${ctx}/text/" class="yuimenubaritemlabel">Text</a></li>
			<li class="yuimenubaritem"><a href="${ctx}/genesis/" class="yuimenubaritemlabel">Genese</a></li>
			<li class="yuimenubaritem"><a href="${ctx}/manuscripts/" class="yuimenubaritemlabel">Manuskripte</a></li>
			<li class="yuimenubaritem"><a href="${ctx}/search" class="yuimenubaritemlabel">Suche</a></li>
		[/#if]
		<li class="yuimenubaritem"><a href="${ctx}/project/about" class="yuimenubaritemlabel">Projekt</a>
		<div class="yuimenu">
		<div class="bd">
		<ul>
			<li class="yuimenuitem"><a href="${ctx}/static/dfg-grant-application.pdf" class="yuimenuitemlabel">DFG-Antrag</a></li>
		</ul>
		</div>
		</div>
		</li>
		<li class="yuimenubaritem"><span class="yuimenubaritemlabel">Partner</span>
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
		<li class="yuimenubaritem"><a href="${ctx}/project/contact" class="yuimenubaritemlabel">Kontakt</a></li>
		<li class="yuimenubaritem"><a href="${ctx}/project/imprint" class="yuimenubaritemlabel">Impressum</a></li>
		<li class="yuimenubaritem">
			<a href="http://wiki.faustedition.net/" class="yuimenubaritemlabel">Intern</a>
			[#if !hasRole("ROLE_EDITOR")]
				<div class="yuimenu">
					<div class="bd">
						<ul>
							<li class="yuimenuitem"><a href="${ctx}/login" class="yuimenuitemlabel">Login</a></li>
						</ul>
					</div>
				</div>
			[/#if]
		</li>
	</ul>
	</div>
	</div>
</div>
<div id="bd">
	[#nested]
</div>
<div id="ft">
	<p>Digitale Faust-Edition. Copyright (c) 2009 Freies Deutsches Hochstift Frankfurt, Klassik Stiftung Weimar, Universität Würzburg.</p>
</div>
</div>
</body>
</html>
[/#macro]

[#macro uplink url title=""]
<a href="${url}"[#if title?has_content] title="${title}"[/#if]><img src="${ctx}/static/arrow_up.png"[#if title?has_content] alt="${title}"[/#if] /></a>
[/#macro]

[#macro tableGrid contents rows=5]
[#if contents?size > 0]
	<tr>
	[#list contents as c]
		[#nested c]
		[#if c_has_next && ((c_index % rows) == (rows - 1))]</tr><tr>[/#if]
	[/#list]
	[#if (contents?size % rows) > 0][#list (rows - 1)..(contents?size % rows) as remainder]<td>&nbsp;</td>[/#list][/#if]
	</tr>
[/#if]
[/#macro]

[#macro yuiHeader]
<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.8.0r4/build/reset-fonts-grids/reset-fonts-grids.css"/>
<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.8.0r4/build/base/base-min.css"/>
<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.8.0r4/build/menu/assets/skins/sam/menu.css"/>
<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.8.0r4/build/paginator/assets/skins/sam/paginator.css"/>
<script type="text/javascript" src="http://yui.yahooapis.com/2.8.0r4/build/utilities/utilities.js"></script>
<script type="text/javascript" src="http://yui.yahooapis.com/2.8.0r4/build/container/container_core-min.js"></script>
<script type="text/javascript" src="http://yui.yahooapis.com/2.8.0r4/build/menu/menu-min.js"></script>
<script type="text/javascript" src="http://yui.yahooapis.com/2.8.0r4/build/event-mouseenter/event-mouseenter-min.js"></script>
<script type="text/javascript" src="http://yui.yahooapis.com/2.8.0r4/build/selector/selector-min.js"></script>
<script type="text/javascript" src="http://yui.yahooapis.com/2.8.0r4/build/event-delegate/event-delegate-min.js"></script>
<script type="text/javascript" src="http://yui.yahooapis.com/2.8.0r4/build/paginator/paginator-min.js"></script>
[/#macro]
