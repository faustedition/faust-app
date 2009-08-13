<%@ page session="false" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,de.faustedition.model.transcription.*"%>
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
		<table class="no-border" width="100%">
			<tr>
			<% 
				int transcriptionCount = 0;
				final int COLUMN_COUNT = 5;
				final int COLUMN_WIDTH = 100 / COLUMN_COUNT;
				TranscriptionStore transcriptionStore = (TranscriptionStore) pageContext.findAttribute("transcriptionStore");
				List<Transcription> transcriptionList = (List<Transcription>)pageContext.findAttribute("transcriptionList");
				for (Transcription transcription : transcriptionList) {
					pageContext.setAttribute("t", transcription);
					pageContext.setAttribute("transcriptionPath", transcriptionStore.buildRelativePath(transcription.getPath()));
					
					if ((transcriptionCount > 0) && (transcriptionCount % COLUMN_COUNT == 0)) {
						pageContext.getOut().println("<tr>");
					}
			%>
				<td class="no-border center" width="<%=COLUMN_WIDTH%>%">
					<p><a href="${t.name}/" title="Zur Handschrift"><img src="${pageContext.request.contextPath}/facsimile/${transcriptionPath}/thumb" alt="Facsimile ${t.name}" /></a></p>
					<p class="small-font"><c:out value="${t.name}" /></p>
				</td>
			<%
					transcriptionCount++;
					if (transcriptionCount == transcriptionList.size()) {
						while (transcriptionCount % COLUMN_COUNT > 0) {
							pageContext.getOut().println("<td class=\"no-border\" width=\"" + COLUMN_WIDTH + "%\">&nbsp;</td>");
							transcriptionCount++;
						}
					} else if (transcriptionCount % COLUMN_COUNT == 0) {
						pageContext.getOut().println("</tr>");
					}
				}
			%>
			</tr>
		</table>
	</c:when>
	<c:otherwise>
		<p class="note">Es sind bislang keine Handschriften in diesem Portfolio verzeichnet.</p>
	</c:otherwise>
</c:choose>
</body>
</html>