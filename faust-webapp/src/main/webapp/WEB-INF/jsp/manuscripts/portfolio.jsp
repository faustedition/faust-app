<%@ page session="false" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../../../includes/taglibs.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Portfolio „<c:out value="${portfolio.name}" />“</title>
</head>
<body>

<h2>Handschriften</h2>

<c:choose>
	<c:when test="${fn:length(transcriptionList) gt 0}">
		<ol class="manuscript-browser-list">
			<c:forEach var="t" items="${transcriptionList}">
				<li><a href="${t.name}/" title="Zur Handschrift"><c:out value="${t.name}" /></a></li>
			</c:forEach>
		</ol>
	</c:when>
	<c:otherwise>
		<p class="note">Es sind bislang keine Handschriften in diesem Portfolio verzeichnet.</p>
	</c:otherwise>
</c:choose>
</body>
</html>