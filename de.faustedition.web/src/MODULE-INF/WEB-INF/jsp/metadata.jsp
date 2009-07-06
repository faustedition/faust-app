<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>
<%@page import="de.faustedition.model.HierarchyNode"%>
<%@page import="org.springframework.web.util.HtmlUtils"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="/WEB-INF/tld/faust.tld" prefix="faust"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<h2><c:if test="${not node.root}">
	<img src="<%=request.getContextPath()%>/gfx/nodetype_${fn:toLowerCase(node.nodeType)}.png" alt="Objekttyp" />
	<c:out value="${node.name}" />
</c:if> <c:if test="${node.root}">Digitale Faustedition</c:if></h2>

<c:if test="${not node.root}">
	<p id="path"><a href="<%=request.getContextPath()%>/metadata/" title="Metadaten">Digitale Faustedition</a> <c:forEach var="p"
		items="${node.pathIterator}">
			&rArr;
			<faust:pathUrl path=""/>
			<a href="<%=request.getContextPath()%>/metadata/${p.href}/" title="Metadaten"><c:out value="${p.name}" /></a>
	</c:forEach></p>
</c:if>

<h3>Metadaten</h3>

<c:if test="${fn:length(metadata.metadataValues) > 0}">
	<table class="metadata">
		<#assign groupName = '' /> <#list metadata.metadataValues as mv>
		<tr>
			<#if groupName != mv.field.group.name> <#assign groupName = mv.field.group.name />
			<th class="metadata-field-group" rowspan="${metadata.groupSizes[mv.field.group.name]}"><@spring.message ('metadata_group.' +
			mv.field.group.name) /></th>
			</#if>
			<th class="metadata-field"><@spring.message ('metadata.' + mv.field.name) />:</th>
			<td class="metadata-value">${mv.value?html?replace('\n', '<br/>')}</td>
		</tr>
		</#list>
	</table>
</c:if>

<c:if test="${fn:length(metadata.metadataValues) == 0}">
	<p class="notice">Diesem Objekt wurden bislang keine Metadaten zugewiesen.</p>
</c:if>

<c:if test="${fn:length(children) > 0}">
	<h3>Untergeordnete Objekte</h3>

	<ul class="children-list">
		<c:forEach var="child" items="${children}">
			<li><img src="<%=request.getContextPath()%>/gfx/nodetype_${fn:toLowerCase(child.nodeType)}.png" alt="Objekttyp" /> <a
				href="${ctx}/metadata/${child.fullPath?url?replace('%2F', '/')}/" title="Zum Objekt">${child.name?html}</a></li>
		</c:forEach>
	</ul>
</c:if>
</body>
</html>