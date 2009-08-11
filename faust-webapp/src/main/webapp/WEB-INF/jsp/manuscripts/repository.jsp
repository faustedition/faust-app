<%@ page session="false" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../../../includes/taglibs.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Bestand „<c:out value="${repository.name}" />“</title>
</head>
<body>

<h2>Portfolios/ Mappen</h2>

<c:choose>
	<c:when test="${fn:length(portfolioList) gt 0}">
		<ol class="manuscript-browser-list">
			<c:forEach var="p" items="${portfolioList}">
				<li><a href="${p.name}/" title="Zum Portfolio"><c:out value="${p.name}" /></a></li>
			</c:forEach>
		</ol>
	</c:when>
	<c:otherwise>
		<p class="note">Es sind bislang keine Portfolios in diesem Bestand verzeichnet.</p>
	</c:otherwise>
</c:choose>
</body>
</html>