[#ftl]
[#assign header]
<link rel="stylesheet" type="text/css" href="${cp}/static/js/imageviewer/css/iip.css" />
<link rel="stylesheet" type="text/css" href="${cp}/static/css/document-text.css"/>
<link rel="stylesheet" type="text/css" href="${cp}/static/css/document-transcript.css"/>
<link rel="stylesheet" type="text/css" id="style-document-transcript-highlight-hands" href="${cp}/static/css/document-transcript-highlight-hands.css"/>
<script type="text/javascript" src="${cp}/static/js/swfobject.js"></script>
<script type="text/javascript" src="${cp}/static/js/raphael-min.js"></script>
[/#assign]
[@faust.page title=(callnumber!"n/a") header=header]

<section class="document-structure">
    <h2>${callnumber!"Document"?html} (<a href="${cp}/document/${id?c}/source" title="XML Source">XML</a>)</h2>

    [#macro docstruct node]
        <li class="${node.type?html}">
            <h3>${node.type?html} (${node.order})</h3>
            <table class="pure-table pure-table-horizontal">
                <tbody>
                <tr>
                    <th>Transcript</th>
                    <td>[#if references[node.order].transcript?has_content]<a href="/transcript/${references[node.order].transcript?c}">${references[node.order].transcript?html}</a>[#else]-[/#if]</td>
                </tr>
                    [#list node?keys?sort as key]
                        [#if !(["contents", "class", "type"]?seq_contains(key)) && (node[key]?is_string)]
                        <tr>
                            <th>${key?html}</th>
                            <td>${node[key]?string?html}</td>
                        </tr>
                        [/#if]
                    [/#list]
                </tbody>
            </table>
            [#if node.contents?has_content]
                <ol>[#list node.contents as child][@docstruct child/][/#list]</ol>
            [/#if]
        </li>
    [/#macro]
    <ol>[@docstruct metadata/]</ol>
</section>

<script type="text/javascript">
    var data = [@json metadata=metadata references=references/];
</script>
[/@faust.page]