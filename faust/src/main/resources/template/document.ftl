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

<h2>
    [#if archive?has_content]<a href="${cp}/document/archive/${archive.label?html}/" title="Archive" class="archive">${archive.name?html}</a><br>[/#if]
    ${callnumber!"Document"?html}
</h2>

<p class="note"><strong>Tipp:</strong> Beim Markieren von Abschnitten in den folgenden Transkripten wird der jeweils gültige Annotationskontext angezeigt.</p>

<h3>Dokumentarisches Transkript (Annotationen)</h3>

<div class="document-documentary-dump"></div>

<h3>Textuelles Transkript (Annotationen)</h3>

<div class="document-textual-dump"></div>

<h3>Dokument-Deskriptor</h3>

<iframe src="${cp}/document/${id?c}/source" style="width: 100%; height: 400px; overflow: auto; border: 1px solid #ccc"></iframe>

<h3>Textuelles Transkript (zur Fehlerbereinigung)</h3>

<div class="document-textual">[@transcriptMarkup text=text /]</div>

<h3>Prototyp: Konvolut-Navigation</h3>

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
    var documentModel = [@json id=id metadata=metadata references=references /],
        documentaryTranscript = [@json text=documentaryTranscript /],
        textualTranscript = [@json text=textualTranscript /];

    YUI().use("node", "util", "document-structure-view", "text-annotation-dump", function(Y) {
        new Y.Faust.IOStatus({ render: true });
        new Y.Faust.DocumentTree({ container: ".document-structure", collapseAll: true }).render();
        new Y.Faust.DocumentPaginator({ container: ".document-paginator", document: documentModel }).render();
        new Y.Faust.TextAnnotationDump({ datasource: new Y.DataSource.Local({ source: documentaryTranscript }) }).render(".document-documentary-dump");
        new Y.Faust.TextAnnotationDump({ datasource: new Y.DataSource.Local({ source: textualTranscript }) }).render(".document-textual-dump");
    });
</script>
[/@faust.page]