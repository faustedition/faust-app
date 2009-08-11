<%@ page session="false" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../../../includes/taglibs.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Manuskripte</title>
</head>
<body>

<h2>Bestände</h2>

<c:choose>
	<c:when test="${fn:length(repositoryList) gt 0}">
		<ol class="manuscript-browser-list">
			<c:forEach var="r" items="${repositoryList}">
				<li><a href="${r.name}/" title="Zum Bestand"><c:out value="${r.name}" /></a></li>
			</c:forEach>
		</ol>
	</c:when>
	<c:otherwise>
		<p class="note">Es sind bislang keine Bestände in der Edition verzeichnet.</p>
	</c:otherwise>
</c:choose>
</body>
</html>