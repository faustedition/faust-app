<%@ page session="false" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../includes/taglibs.jspf" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head profile="http://dublincore.org/documents/dcq-html/">
		<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
		<title>faustedition.net :: <decorator:title default="Faust-Edition" /></title>
		<%-- http://developer.yahoo.com/yui/articles/hosting/?base&fonts&grids&menu&reset&yuiloader&MIN&loadOptional --%>
		<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?2.7.0/build/reset-fonts-grids/reset-fonts-grids.css&amp;2.7.0/build/base/base-min.css&amp;2.7.0/build/menu/assets/skins/sam/menu.css" />
		<script type="text/javascript" src="http://yui.yahooapis.com/combo?2.7.0/build/yuiloader-dom-event/yuiloader-dom-event.js&amp;2.7.0/build/container/container_core-min.js&amp;2.7.0/build/menu/menu-min.js"></script>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/static/faust.css" />
		<script type="text/javascript" src="${pageContext.request.contextPath}/static/faust.js"></script>
		<decorator:head />
		<link rel="schema.DC" href="http://purl.org/dc/elements/1.1/" />
		<link rel="schema.DCTERMS" href="http://purl.org/dc/terms/" />
		<meta name="DC.format" scheme="DCTERMS.IMT" content="text/html" />
		<meta name="DC.type" scheme="DCTERMS.DCMIType" content="Text" />
		<meta name="DC.publisher" content="Digitale Faust-Edition" />
		<meta name="DC.creator" content="Digitale Faust-Edition" />
		<meta name="DC.subject" content="Faust, Johann Wolfgang von Goethe, Historisch-kritische Edition, digital humanities" />
		<%-- 
		<meta name="DCTERMS.license"  scheme="DCTERMS.URI" content="http://www.gnu.org/copyleft/fdl.html" />
		<meta name="DCTERMS.rightsHolder" content="Wikimedia Foundation Inc." />
		--%>
	</head>
	<body class="yui-skin-sam">
		<div id="doc2" class="yui-t7">
			<div id="hd">
				<h1><span style="color: #764F27">faustedition.net&nbsp;::</span>&nbsp;<decorator:title /></h1>
				<div id="top-navigation" class="yuimenubar yuimenubarnav">
					<div class="bd">
						<ul class="first-of-type">
							<security:authorize ifAllGranted="ROLE_EDITOR">
								<li class="yuimenubaritem"><a href="${pageContext.request.contextPath}/manuscripts/" class="yuimenubaritemlabel"><fmt:message key="menu.manuscripts" /></a></li>
								<li class="yuimenubaritem"><a href="${pageContext.request.contextPath}/text/" class="yuimenubaritemlabel"><fmt:message key="menu.text" /></a></li>
								<li class="yuimenubaritem"><a href="${pageContext.request.contextPath}/genesis/" class="yuimenubaritemlabel"><fmt:message key="menu.genesis" /></a></li>
							</security:authorize>
							<li class="yuimenubaritem"><a class="yuimenubaritemlabel" href="${pageContext.request.contextPath}/project/about"><fmt:message key="menu.project" /></a>
								<div class="yuimenu">
									<div class="bd">
										<ul>
											<li class="yuimenuitem"><a href="${pageContext.request.contextPath}/static/digitale_faustedition_antrag.pdf" class="yuimenuitemlabel"><fmt:message key="menu.grant_application" /></a></li>
										</ul>
									</div>
								</div>
							</li>
							<li class="yuimenubaritem"><span class="yuimenubaritemlabel"><fmt:message key="menu.partners" /></span>
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
							<li class="yuimenubaritem"><a href="${pageContext.request.contextPath}/project/contact" class="yuimenubaritemlabel"><fmt:message key="menu.contact" /></a></li>
							<li class="yuimenubaritem"><a href="${pageContext.request.contextPath}/project/imprint" class="yuimenubaritemlabel"><fmt:message key="menu.imprint" /></a></li>
							<li class="yuimenubaritem"><a href="http://wiki.faustedition.net/" class="yuimenubaritemlabel"><fmt:message key="menu.restricted" /></a>
								<security:authorize ifNotGranted="ROLE_EDITOR">
									<div class="yuimenu">
										<div class="bd">
											<ul>
												<li class="yuimenuitem"><a href="${pageContext.request.contextPath}/project/login" class="yuimenuitemlabel">Login</a></li>
											</ul>
										</div>
									</div>
								</security:authorize>
							</li>
						</ul>
					</div>
				</div>
			</div>
			<div id="bd">
				<decorator:body />
			</div>
			<div id="ft">
				<p>Digitale Faust-Edition. Copyright (c) 2009 Freies Deutsches Hochstift Frankfurt, Klassik Stiftung Weimar, Universität Würzburg.</p>
			</div>
		</div>
	</body>
</html>
