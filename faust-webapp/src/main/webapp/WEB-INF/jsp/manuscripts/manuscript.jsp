<%@ page session="false" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../../../includes/taglibs.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Handschrift „<c:out value="${transcription.name}" />“</title>
</head>
<body>
<c:set var="facsimile" value="${pageContext.request.contextPath}/facsimile/${facsimilePath}"/>

<div class="yui-g">
	<div class="yui-u first">
		<ul>
			<li><a href="${facsimile}" title="Facsimile-Link">&raquo; Facsimile-Link</a></li>
			<li><a href="tei-document/" title="TEI-Dokument">&raquo; TEI-Dokument</a></li>
			<li><a href="${pageContext.request.contextPath}/dav/store/${transcription.path}" title="WebDAV-Link">&raquo; WebDAV-Link</a></li>
		</ul>
	</div>
	<div class="yui-u">
		<iframe src="${facsimile}" height="400" width="400"></iframe>
	</div>
</div>
</body>
</html>