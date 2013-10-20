<#assign header>
<link rel="stylesheet" type="text/css" href="${cp}/static/js/imageviewer/css/iip.css" />
<link rel="stylesheet" type="text/css" href="${cp}/static/css/document-text.css"/>
<link rel="stylesheet" type="text/css" href="${cp}/static/css/document-transcript.css"/>
<link rel="stylesheet" type="text/css" id="style-document-transcript-highlight-hands" href="${cp}/static/css/document-transcript-highlight-hands.css"/>
<script type="text/javascript" src="${cp}/static/js/swfobject.js"></script>
<script type="text/javascript" src="${cp}/static/js/raphael-min.js"></script>
</#assign>
<@faust.page title=(callnumber!"n/a") header=header>

<p><a href="${cp}/document/${id?c}/source" title="XML Source">XML</a></p>

<h2>Metadata</h2>

<table>
    <tbody>
    <#list metadata?keys as key>
        <#if key != "contents">
        <tr>
            <th>${key?html}</th>
            <td><#if !(metadata[key]?is_sequence)>${metadata[key]?html}<#else>${metadata[key]?join(", ")?html}</#if></td>
        </tr>
        </#if>
    </#list>
    </tbody>
</table>
<dl>
</dl>

<h2>References</h2>

<ol>
<#list references as ref>
    <#if ref.transcript?has_content><li><a href="/transcript/${ref.transcript?c}">${ref.transcript}</a></li></#if>
</#list>
</ol>
</@faust.page>
