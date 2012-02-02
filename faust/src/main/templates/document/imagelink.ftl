<#assign iaPath=(cp + "/static/ext-imageannotation")>
<#assign path=(cp + "/document/imagelink/" + document.source?replace('faust://xml/document/', '') + "/" + pageNum)>

<#assign redirect=(iaPath + "/svg-editor.html?url=" + path + "&bkgd_url=/static/img/emblem.jpg")>
<#assign redirect=(iaPath + "/svg-editor.html?url=" + path + "&bkgd_url=" + facsimileUrl)>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>Text-Image-Links</title>
</head>
<frameset rows="100%">
  <frame src="${redirect}">
</frameset>
<noframes>
  <body>Please follow this <a href="${redirect}">link</a>!</body>
</noframes>
</html>