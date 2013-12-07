[#ftl]
[#assign header]
<link rel="stylesheet" type="text/css" href="${cp}/static/js/imageviewer/css/iip.css" />
<link rel="stylesheet" type="text/css" href="${cp}/static/css/document-text.css"/>
<link rel="stylesheet" type="text/css" href="${cp}/static/css/document-transcript.css"/>
<link rel="stylesheet" type="text/css" id="style-document-transcript-highlight-hands" href="${cp}/static/css/document-transcript-highlight-hands.css"/>
<script type="text/javascript" src="${cp}/static/js/swfobject.js"></script>
<script type="text/javascript" src="${cp}/static/js/raphael-min.js"></script>
[/#assign]
[#assign title][#if archive?has_content]${archive.name?html}: [/#if]${callnumber!"Document"?html}[/#assign]
[@faust.page title=title header=header]

<h2>[#if archive?has_content]<span class="archive">${archive.name?html}</span><br>[/#if]${callnumber!"Document"?html}</h2>

<p><a href="${cp}/document/${id?c}/source" title="XML Source">Document Descriptor (XML)</a></p>

<div class="document-textual">[@transcriptMarkup text=textualTranscript /]</div>

<hr>

<div class="pure-g">
    <div class="pure-u-1-4">
        <div class="l-box">
            <section class="document-structure" data-document-id="${id?c}">

                [#macro docstruct node]
                    <li class="${node.type}" data-order="${node.order}" data-type="${node.type}">
                        <h3 class="label">${message('materialUnit.type.' + node.type)?html}&#160;(${node.order})</h3>
                        <table class="metadata pure-table pure-table-horizontal">
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
        </div>
    </div>
    <div class="pure-u-3-4">
        <div class="l-box">
            <ol class="document-paginator"></ol>
        </div>
    </div>
</div>

<script type="text/javascript">
    var model = [@json id=id metadata=metadata references=references/];
    YUI().use("node", "util", "document-structure-view", function(Y) {
        new Y.Faust.IOStatus({ render: true });
        new Y.Faust.DocumentTree({ container: ".document-structure", collapseAll: true }).render();
        new Y.Faust.DocumentPaginator({ container: ".document-paginator", document: model }).render();
    });
</script>
[/@faust.page]