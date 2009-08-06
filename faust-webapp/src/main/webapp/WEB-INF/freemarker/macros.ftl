<#import "/spring.ftl" as spring>
<#assign xhtmlCompliant = true in spring>
<#assign ctx = springMacroRequestContext.getContextPath() />

<#macro page title>
	<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
	<html xmlns="http://www.w3.org/1999/xhtml" lang="de" xml:lang="de">
	<head>
		<meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
		<#-- http://developer.yahoo.com/yui/articles/hosting/?base&fonts&grids&menu&reset&MIN&loadOptional -->
		<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?2.7.0/build/reset-fonts-grids/reset-fonts-grids.css&amp;2.7.0/build/base/base-min.css&amp;2.7.0/build/menu/assets/skins/sam/menu.css" />
		<script type="text/javascript" src="http://yui.yahooapis.com/combo?2.7.0/build/yahoo-dom-event/yahoo-dom-event.js&amp;2.7.0/build/container/container_core-min.js&amp;2.7.0/build/menu/menu-min.js"></script>
		<#-- customizations -->
		<link rel="stylesheet" type="text/css" href="${ctx}/static/css/faustedition.css" />
		<script type="text/javascript" src="${ctx}/static/js/faustedition.js"></script>
		<title>${title} :: Digitale Faustedition</title>
	</head>
		<body class="yui-skin-sam">
			<div id="doc2" class="yui-t7">
				<div id="hd" role="banner">
					<p><img src="${ctx}/static/gfx/header.jpg" alt="Digitale Faustedition" /></p>
					<div id="main-menu" class="yuimenubar">
						<div class="bd">
							<ul class="first-of-type">
								<li class="yuimenubaritem first-of-type"><a class="yuimenubaritemlabel" href="${ctx}/metadata/" title="Metadaten">Metadatenbrowser</a></li>
								<li class="yuimenubaritem first-of-type"><a class="yuimenubaritemlabel" href="${ctx}/metadata/" title="Impressum">Metadatensuche</a></li>
							</ul>
						</div>
					</div>
				</div>
				<div id="bd" role="main">
					<div id="yui-main">
						<div class="yui-b">
							<div class="yui-g">
								<#nested />
							</div>
						</div>
					</div>
				</div>
				<div id="ft" role="contentinfo">
					<p>Copyright &copy; 2009 Freies Deutsches Hochstift, Klassik-Stiftung Weimar, Universität Würzburg. Alle Rechte vorbehalten.</p>
				</div>
			</div>
		</body>
	</html>
</#macro>