[#ftl]
[#import "/spring.ftl" as spring]
[#assign xhtmlCompliant = true in spring]
[#assign ctx = springMacroRequestContext.getContextPath()]

[#macro page title]
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head profile="http://dublincore.org/documents/dcq-html/">
	<title>${title} :: faustedition.net</title>
	<script type="text/javascript">var ctx = "${ctx}";</script>
	<script type="text/javascript" src="${ctx}/js/faust.js"></script>
	[@yuiHeader/]
	<link rel="stylesheet" type="text/css" href="${ctx}/css/faust.css" id="custom-css"/>
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
	<h1>${title}&nbsp;<span style="color: #764F27">::&nbsp;faustedition.net</span></h1>
	<div id="top-navigation" class="yuimenubar yuimenubarnav">
	<div class="bd">
	<ul class="first-of-type">
		[#if hasRole("ROLE_EDITOR")]
			<li class="yuimenubaritem"><a href="${ctx}/repository/" class="yuimenubaritemlabel">[@spring.message "menu.repository" /]</a></li>
			<li class="yuimenubaritem"><a href="${ctx}/text/" class="yuimenubaritemlabel">[@spring.message "menu.text" /]</a></li>
			<li class="yuimenubaritem"><a href="${ctx}/genesis/" class="yuimenubaritemlabel">[@spring.message "menu.genesis" /]</a></li>
			<li class="yuimenubaritem"><a href="${ctx}/search/" class="yuimenubaritemlabel">[@spring.message "menu.search" /]</a></li>
		[/#if]
		<li class="yuimenubaritem"><span class="yuimenubaritemlabel">[@spring.message "menu.project" /]</span>
		<div class="yuimenu">
		<div class="bd">
		<ul>
			<li class="yuimenuitem"><a href="${ctx}/project/about" class="yuimenuitemlabel">[@spring.message "menu.about" /]</a></li>
			<li class="yuimenuitem"><a href="${ctx}/static/dfg-grant-application.pdf" class="yuimenuitemlabel">[@spring.message "menu.grant_application" /]</a></li>
		</ul>
		</div>
		</div>
		</li>
		<li class="yuimenubaritem"><span class="yuimenubaritemlabel">[@spring.message "menu.partners" /]</span>
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
		<li class="yuimenubaritem"><a href="${ctx}/project/contact" class="yuimenubaritemlabel">[@spring.message "menu.contact" /]</a></li>
		<li class="yuimenubaritem"><a href="${ctx}/project/imprint" class="yuimenubaritemlabel">[@spring.message "menu.imprint" /]</a></li>
		<li class="yuimenubaritem">
			<a href="http://wiki.faustedition.net/" class="yuimenubaritemlabel">[@spring.message "menu.restricted" /]</a>
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
<a href="${url}"[#if title?has_content] title="${title}"[/#if]><img src="${ctx}/img/arrow_up.png"[#if title?has_content] alt="${title}"[/#if] /></a>
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
<script type="text/javascript" src="http://yui.yahooapis.com/2.8.0r4/build/yuiloader/yuiloader-min.js"></script>
<script type="text/javascript">
var loader = new YAHOO.util.YUILoader( {
	require : [ "base", "connection", "datasource", "datatable", "fonts", "grids", "json", "menu", "reset", "yuiloader" ],
	loadOptional : true,
	combine : true,
	insertBefore : "custom-css",
	allowRollup : true,
	onSuccess : yuiInit,
	onFailure : function(msg, obj) {
		alert("YUI loader failed: " + msg +  "\n" + obj);
	}
});
loader.insert();
</script>
[/#macro]

