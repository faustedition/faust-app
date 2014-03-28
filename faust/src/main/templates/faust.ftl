<#macro resources paths>${cp}/resources?<#list paths as p>${p}<#if p_has_next>&amp;</#if></#list></#macro>

<#macro page title css="" header="" layout="" menuhighlight="">
<!DOCTYPE html>
<html class="yui3-loading">
<head>
	<title>Digitale Faust-Edition: ${title}</title>



	<!-- http://purecss.io/ -->
	<link rel="stylesheet" href="${cp}/static/css/pure-min.css">
	<!-- http://fortawesome.github.io/Font-Awesome/ -->
	<link href="//netdna.bootstrapcdn.com/font-awesome/3.2.1/css/font-awesome.css" rel="stylesheet">
	<!-- http://www.google.com/fonts/: light, normal, medium, bold -->
	<link href="https://fonts.googleapis.com/css?family=Ubuntu:300,300italic,400,400italic,500,500italic,700,700italic" rel="stylesheet" type="text/css">
	<!-- http://www.google.com/fonts/: normal, bold -->
	<link href="https://fonts.googleapis.com/css?family=Ubuntu+Mono:400,400italic,700,700italic" rel="stylesheet" type="text/css">
	<link rel="stylesheet" href="${cp}/static/css/pure-custom.css">
	<link rel="stylesheet" href="${cp}/static/css/style.css">
	<script src="https://code.jquery.com/jquery-1.11.0.min.js"></script>
	<#if layout!="wide"><script src="${cp}/static/js/functions.js"></script></#if>



	<link rel="stylesheet" type="text/css" href="<@resources [
        "yui3/build/cssreset/reset-min.css",
        "yui3/build/cssgrids/grids-min.css",
        <#-- "yui3/build/cssfonts/fonts-min.css", -->
        "yui3/build/cssbase/base-min.css"
    ] />" />

    <link rel="stylesheet" type="text/css" href="${cp}/static/css/faust.css"/>



    <script type="text/javascript">
        var cp = '${cp?js_string}';
        var Faust = { contextPath: cp, FacsimileServer: "${facsimilieIIPUrl}" };
    </script>
    <script type="text/javascript" src="${cp}/static/yui3/build/yui/yui-debug.js"></script>
    <script type="text/javascript" src="${cp}/static/js/yui-config.js"></script>
	<script type="text/javascript" src="${cp}/static/js/faust.js"></script>

	<link rel="schema.DC" href="http://purl.org/dc/elements/1.1/" />
	<link rel="schema.DCTERMS" href="http://purl.org/dc/terms/" />
	<meta name="DC.format" scheme="DCTERMS.IMT" content="text/html" />
	<meta name="DC.type" scheme="DCTERMS.DCMIType" content="Text" />
	<meta name="DC.publisher" content="Digitale Faust-Edition" />
	<meta name="DC.creator" content="Digitale Faust-Edition" />
	<meta name="DC.subject" content="Faust, Johann Wolfgang von Goethe, Historisch-kritische Edition, digital humanities" />
	<!-- 
	<meta name="DCTERMS.license"  scheme="DCTERMS.URI" content="http://www.gnu.org/copyleft/fdl.html">
	<meta name="DCTERMS.rightsHolder" content="Wikimedia Foundation Inc.">
	 -->
	 <#if css?has_content><style type="text/css">${css}</style></#if>
	 <#if header?has_content>${header}</#if>
</head>
<body class="yui3-skin-sam<#if layout?has_content> layout-${layout}</#if>">



<header class="<#if layout="wide">compressed</#if>">
	<div class="center">
		<a class="logo" href="${cp}/" title="Faustedition">
			<img src="${cp}/static/img/logo.svg" width="380" height="30" alt="Faustedition">
		</a>
		<@topNavigation menuhighlight=menuhighlight/>
</header>


<div id="main" class="<#if layout?has_content>layout-${layout}<#else>center</#if>">
	<#nested />
</div>

<footer>
	<b>Digitale Faust-Edition</b> • Copyright (c) 2009-2014 • Freies Deutsches Hochstift Frankfurt • Klassik Stiftung Weimar • Universität Würzburg
</footer>

<script type="text/javascript">
	YUI().use("node", "dom", "node-menunav", "search", function(Y) {
		//Y.one("#top-navigation").plug(Y.Plugin.NodeMenuNav);
		Y.one("html").removeClass("yui3-loading");

		//var quickSearch = Y.one("#quick-search");
		//new Y.Faust.QuickSearch({ srcNode: quickSearch }).render();
		//quickSearch.focus();
	});
</script>
</body>
</html>
</#macro>

<#macro topNavigation menuhighlight="">
<#--
<div id="top-navigation" class="yui3-menu yui3-menu-horizontal yui3-menubuttonnav" style="clear: both">
	<div class="yui3-menu-content">
		<ul class="first-of-type">
			<#if roles?seq_contains("editor") || roles?seq_contains("external")>
			<li>
				<a href="#demo" class="yui3-menu-label"><em>${message("menu.demo")}</em></a>
				<div id="demo" class="yui3-menu">
				<div class="yui3-menu-content">
				<ul>
					<li class="yui3-menuitem"><a href="${cp}/demo/diplomatic-transcript" class="yui3-menuitem-content">${message("menu.demo.diplomatic-transcript_1")}</a></li>
					<li class="yui3-menuitem"><a href="${cp}/document/faust/2.5/gsa_390883.xml" class="yui3-menuitem-content">${message("menu.demo.diplomatic-transcript_2")}</a></li>
					<li class="yui3-menuitem"><a href="${cp}/structure/gsa/391282/391282-structure.xml" class="yui3-menuitem-content">${message("menu.demo.manuscript_description")}</a></li>
					<li class="yui3-menuitem"><a href="${cp}/demo/genesis" class="yui3-menuitem-content">${message("menu.demo.genesis_1")}</a></li>
					<li class="yui3-menuitem"><a href="${cp}/text/2-5-4.xml" class="yui3-menuitem-content">${message("menu.demo.genesis_2")}</a></li>
                    <li class="yui3-menuitem"><a href="${cp}/demo/text-image-link" class="yui3-menuitem-content">${message("menu.demo.text_image_link")}</a></li>
				</ul>
				</div>
				</div>
			</li>
			<li>
				<a href="#documents" class="yui3-menu-label"><em>${message("menu.document")}</em></a>
				<div id="documents" class="yui3-menu">
				<div class="yui3-menu-content">
				<ul>
					<li class="yui3-menuitem"><a href="${cp}/archive/" class="yui3-menuitem-content">${message("menu.archives")}</a></li>
					<li class="yui3-menuitem"><a href="${cp}/document/faust/2/gsa_391098.xml" class="yui3-menuitem-content"><em>GSA/ W 1804 (XIX,3) – H</em></a></li>
					<li class="yui3-menuitem"><a href="${cp}/document/faust/2.5/gsa_390883.xml" class="yui3-menuitem-content"><em>GSA/ W 1698 (XVIII,7,3) – V H.2</em></a></li>
				</ul>
				</div>
				</div>
			</li>
			<li>
				<a href="#texts" class="yui3-menu-label"><em>${message("menu.text")}</em></a>
				<div id="texts" class="yui3-menu">
				<div class="yui3-menu-content">
				<ul>
					<@textMenu "Faust – In ursprünglicher Gestalt." "0-"/>
					<@textMenu "Faust – Eine Tragödie. [Erster Theil.]" "1-"/>
					<@textMenu "Faust – Eine Tragödie. [Zweiter Theil.]" "2-"/>
				</ul>
				</div>
				</div>
			</li>
			<li class="yui3-menuitem"><a href="${cp}/genesis/work" class="yui3-menuitem-content">${message("menu.genesis")}</a></li>
			</#if>
			<li><a class="yui3-menu-label" href="${cp}/project/about"><em>${message("menu.project")}</em></a>
				<div id="project" class="yui3-menu">
				<div class="yui3-menu-content">
				<ul>
					<li class="yui3-menuitem"><a href="${cp}/static/dfg-grant-application.pdf" class="yui3-menuitem-content">${message("menu.grant_application")}</a></li>
				</ul>
				</div>
				</div>
			</li>
			<li><a class="yui3-menu-label" href="#partners"><em>${message("menu.partners")}</em></a>
				<div id="partners" class="yui3-menu">
				<div class="yui3-menu-content">
				<ul>
					<li class="yui3-menuitem"><a href="http://www.goethehaus-frankfurt.de/" class="yui3-menuitem-content">Freies Deutsches Hochstift Frankfurt</a></li>
					<li class="yui3-menuitem"><a href="http://www.klassik-stiftung.de/" class="yui3-menuitem-content">Klassik Stiftung Weimar</a></li>
					<li class="yui3-menuitem"><a href="http://www.uni-wuerzburg.de/" class="yui3-menuitem-content">Julius-Maximilians-Universität Würzburg</a></li>
				</ul>
				</div>
				</div>
			</li>
			<li class="yui3-menuitem"><a href="${cp}/project/contact" class="yui3-menuitem-content">${message("menu.contact")}</a></li>
			<li class="yui3-menuitem"><a href="${cp}/project/imprint" class="yui3-menuitem-content">${message("menu.imprint")}</a></li>
			
			<@restricted>
			<li><a class="yui3-menu-label" href="#restricted"><em>${message("menu.restricted")}</em></a>
				<div id="restricted" class="yui3-menu">
				<div class="yui3-menu-content">
				<ul>
					<li class="yui3-menuitem"><a href="${cp}/xml-query/" class="yui3-menuitem-content">XML Query</a></li>
				</ul>
				</div>
				</div>
			</li>
			</@restricted>

			<#if !roles?seq_contains("editor") && !roles?seq_contains("external")>
			<li class="yui3-menuitem"><a href="${cp}/login" class="yui3-menuitem-content">Login</a></li>
			</#if>
		</ul>
	</div>
</div>
-->

<div class="pure-menu pure-menu-open pure-menu-horizontal pure-submenu pure-right">
	<ul>
		<@restricted><li <#if menuhighlight="xml-query">class="pure-menu-selected"</#if>><a href="${cp}/xml-query"> <i class="icon-wrench"></i> Query</a></li></@restricted>
		<#if !roles?seq_contains("editor") && !roles?seq_contains("external")>
			<li <#if menuhighlight="login">class="pure-menu-selected"</#if>><a href="${cp}/login">Login</a></li>
		</#if>
		<li <#if menuhighlight="contact">class="pure-menu-selected"</#if>><a href="${cp}/project/contact">${message("menu.contact")}</a></li>
		<li <#if menuhighlight="imprint">class="pure-menu-selected"</#if>><a href="${cp}/project/imprint">${message("menu.imprint")}</a></li>
		<li <#if menuhighlight="about">class="pure-menu-selected"</#if>><a href="${cp}/project/about">${message("menu.project")}</a></li>
	</ul>
</div>
<nav class="pure-menu pure-menu-open pure-menu-horizontal pure-right">
	<ul>
		<li <#if menuhighlight="archives">class="pure-menu-selected"</#if>><a href="${cp}/archive/">${message("menu.archives")}</a></li>
		<li <#if menuhighlight="genesis">class="pure-menu-selected"</#if>><a href="${cp}/genesis/work/">${message("menu.genesis")}</a></li>
		<li <#if menuhighlight="text">class="pure-menu-selected"</#if>><a href="#">Text</a></li>
		<li><form class="pure-form"><input type="text" placeholder="${message("menu.search")}"></form></li>
	</ul>
</nav>
</div>

</#macro>

<#macro textMenu title prefix>
<li>
	<#local firstEntry=true />
	<#list textToc?keys as text>
	<#if text?starts_with(prefix)>
		<#if firstEntry>
			<a href="${cp}/text/${text}.xml" class="yui3-menu-label">${title}</a>
			<div id="#texts-faust-0" class="yui3-menu">
			<div class="yui3-menu-content">
			<ul>
			<#local firstEntry=false />
		</#if>
		<li class="yui3-menuitem">
			<a href="${cp}/text/${text}.xml" class="yui3-menuitem-content">${textToc[text]?html}</a>
		</li>
	</#if>
	<#if !text_has_next>
		</ul>
		</div>
		</div>
	</#if>
	</#list>
</li>
</#macro>

<#macro restricted>
	<#if roles?seq_contains("editor") || roles?seq_contains("admin")>
		<#nested>
	</#if>
</#macro>

<#macro passMessages messages>
<script type="text/javascript">
	Faust.messages = {};
	<#list messages as msg>
		Faust.messages["${msg}"] = "${message(msg)}";
	</#list>
</script>
</#macro>

<#function resolveUri uri>
	<#return cp + uri?replace("faust://", "/")>
</#function>