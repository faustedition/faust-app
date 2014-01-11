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

<div class="pure-g">
    <div class="pure-u-1-2"><h3>Textuelles Transkript</h3></div>
    <div class="pure-u-1-2"><h3>Dokumentarisches Transkript</h3></div>
    <div class="pure-u-1-2"><div class="l-box transcript-textual"></div></div>
    <div class="pure-u-1-2"><div class="l-box transcript-documentary"></div></div>
</div>

<div class="document-annotation-dump"></div>

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
    YUI().use("node", "datasource", "util", "document-structure-view", "text-annotation-view", function(Y) {
        var documentModel = [@json id=id metadata=metadata references=references /],
            documentaryTranscript = Y.Faust.TextSchema.parse([@json documentaryTranscript=documentaryTranscript /]),
            textualTranscript = Y.Faust.TextSchema.parse([@json textualTranscript=textualTranscript /]),
            collation = new Y.Faust.Collation([@json collation=collation /]);

        new Y.Faust.IOStatus({ render: true });
        new Y.Faust.DocumentTree({ container: ".document-structure", collapseAll: true }).render();
        new Y.Faust.DocumentPaginator({ container: ".document-paginator", document: documentModel }).render();

        var documentaryView = new Y.Faust.TextView({ text: documentaryTranscript, milestones: collation.milestones(0) }),
            textualView = new Y.Faust.TextView({ text: textualTranscript, milestones: collation.milestones(1) }),
            annotationDump = new Y.Faust.TextAnnotationDump().render(".document-annotation-dump");

        var updateAnnotationDump = function(e) {
            annotationDump.setAttrs({
                segment: e.segment,
                annotations: e.text.index().find(e.segment)
            });
        };
        documentaryView.after("textSelected", updateAnnotationDump);
        textualView.after("textSelected", updateAnnotationDump);

        var overlaps = function(a, b) {
            return (a[0] >= b[1]) ? 1 : ((b[0] >= a[1]) ? -1 : 0);
        };

        var highlightChanges = function(witness, other) {
            return function(e) {
                var alignments = collation.alignments(),
                    witnessOffset = witness * 2,
                    otherOffset = other * 2,
                    segmentNodes = e.nodes,
                    segments = e.segments,
                    lastSegment = 0;

                for (var ac = 0, al = alignments.length; ac < al; ac++) {
                    var alignment = alignments[ac];

                    var witnessRange = [alignment[witnessOffset], alignment[witnessOffset + 1]];
                    if (witnessRange[0] == witnessRange[1]) continue;

                    var segmentIdx = Y.Faust.SortedArray.search(segments, witnessRange, overlaps, lastSegment, segments.length);

                    if (segmentIdx >= 0) {
                        var otherHasGap = alignment[otherOffset] == alignment[otherOffset + 1],
                            editClass = otherHasGap ? "added" : (Y.Lang.isBoolean(alignment[alignment.length - 1]) ? "changed" : "match");

                        //lastSegment = segmentIdx;
                        for (var sc = segmentIdx, sl = segments.length; sc < sl; sc++) {
                            if (overlaps(segments[sc], witnessRange) != 0) break;
                            segmentNodes[sc].addClass(editClass);
                        }
                        for (var sc = segmentIdx - 1; sc >= 0; sc--) {
                            if (overlaps(segments[sc], witnessRange) != 0) break;
                            segmentNodes[sc].addClass(editClass);
                        }
                    }
                }
            };
        };

        documentaryView.after("segmentsRendered", highlightChanges(0, 1));
        textualView.after("segmentsRendered", highlightChanges(1, 0));

        documentaryView.render(".transcript-documentary");
        textualView.render(".transcript-textual");

        Y.log(Y.Faust.SortedArray.search([1, 2, 3, 4, 5, 6], 5, Y.Faust.compareNumbers, 0, 5));
    });
</script>
[/@faust.page]