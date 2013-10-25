<#macro resources paths>${cp}/resources?<#list paths as p>${p}<#if p_has_next>&amp;</#if></#list></#macro>

<#macro page title css="" header="" layout="">
<!DOCTYPE html>
<html class="yui3-loading">
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
	<title>Digitale Faust-Edition: ${title}</title>
    <link rel="stylesheet" type="text/css" href="${cp}/static/css/pure-0.3.0<#if !debug>-min</#if>.css"><#--
    <link rel="stylesheet" type="text/css" href="${cp}/static/css/bootstrap<#if !debug>.min</#if>.css">-->
    <link rel="stylesheet" type="text/css" href="${cp}/static/css/faust.css">
    <script type="text/javascript">
        var cp = '${cp?js_string}';
        var Faust = { contextPath: cp, FacsimileServer: "${facsimileIIPUrl}" };
    </script><#--
    <script type="text/javascript" src="${cp}/static/js/jquery-1.10.2<#if !debug>.min</#if>.js"></script>
    <script type="text/javascript" src="${cp}/static/js/bootstrap<#if !debug>.min</#if>.js"></script>-->
    <script type="text/javascript" src="${yp}/yui/yui<#if !debug>-min</#if>.js"></script>
    <script type="text/javascript" src="${cp}/static/js/yui-config.js"></script>
    <#if debug>
    <script type="text/javascript">
        YUI.applyConfig({ debug: true, filter: "debug" });
    </script>
    </#if>
    <script type="text/javascript" src="${cp}/static/js/svg<#if !debug>.min</#if>.js"></script>
    <script type="text/javascript" src="${cp}/static/js/svg.draggable<#if !debug>.min</#if>.js"></script>
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
<body>
<div class="pure-g">
    <div class="pure-u-2-3">
        <div class="pure-menu pure-menu-open pure-menu-horizontal">
            <a href="${cp}/" class="pure-menu-heading">Goethe: Faust</a>

            <ul>
                <li><a href="${cp}/text/">${message("menu.text")}</a></li>
                <li><a href="${cp}/document/">${message("menu.document")}</a></li>
                <li><a href="${cp}/genesis/">${message("menu.genesis")}</a></li>
            </ul>
        </div>
    </div>
    <div class="pure-u-1-3">
        <form class="pure-form" id="search">
            <input type="text" class="pure-input-rounded">
            <button type="submit" class="pure-button">Search</button>
        </form>
    </div>
</div>
<section id="main">
	<#nested />
</section>
<footer>
    <p>Digitale Faust-Edition. Copyright (c) 2009-2013 Freies Deutsches Hochstift Frankfurt, Klassik Stiftung Weimar, Universität Würzburg.</p>
</footer>
</body>
</html>
</#macro>

<#macro topNavigation>
<div id="top-navigation" class="yui3-menu yui3-menu-horizontal yui3-menubuttonnav" style="clear: both">
	<div class="yui3-menu-content">
		<ul class="first-of-type">
			<li>
				<a href="#demo" class="yui3-menu-label"><em>${message("menu.demo")}</em></a>
				<div id="demo" class="yui3-menu">
				<div class="yui3-menu-content">
				<ul>
					<li class="yui3-menuitem"><a href="${cp}/demo/diplomatic-transcript" class="yui3-menuitem-content">${message("menu.demo.diplomatic-transcript_1")}</a></li>
					<li class="yui3-menuitem"><a href="${cp}/document/faust/2.5/gsa_390883.xml" class="yui3-menuitem-content">${message("menu.demo.diplomatic-transcript_2")}</a></li>
					<li class="yui3-menuitem"><a href="${cp}/structure/gsa/391282/391282-structure.xml" class="yui3-menuitem-content">${message("menu.demo.manuscript_description")}</a></li>
					<li class="yui3-menuitem"><a href="${cp}/demo/genesis" class="yui3-menuitem-content">${message("menu.demo.genesis_1")}</a></li>
					<!--<li class="yui3-menuitem"><a href="${cp}/text/2-5-4.xml" class="yui3-menuitem-content">${message("menu.demo.genesis_2")}</a></li>-->
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
			<li class="yui3-menuitem"><a href="${cp}/genesis/work" class="yui3-menuitem-content">${message("menu.genesis")}</a></li>
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
            <li class="yui3-menuitem"><a href="${cp}/xml-query/" class="yui3-menuitem-content">XML Query</a></li>
		</ul>
	</div>
</div>	
</#macro>

<#function resolveUri uri>
	<#return cp + uri?replace("faust://", "/")>
</#function>