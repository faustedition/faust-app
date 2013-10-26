[#ftl]
[#assign header]
    <style type="text/css">
        h2, h4 { font-size: 1em }
        .text, .annotations { height: 500px; overflow: auto }

        .text p { margin: 0 }
        .annotations ul {margin: 2em 0; padding: 0; list-style: none;  }
        .annotations li { margin: .25em 0; padding: 0 }
    </style>
[/#assign]
[@faust.page title=id header=header]

<h2>#${id} (<a href="${cp}/transcript/${id?c}/source" title="XML Source">XML</a>)</h2>

[#-- <pre>[#list text as t][#if !(t?is_hash)]${t?html}[/#if][/#list]</pre> --]

<div class="pure-g">
    <div class="pure-u-3-4"><div class="l-box">
        <h2>Text</h2>
        <div class="text"></div>
    </div></div>
    <div class="pure-u-1-4"><div class="l-box">
        <h2>Annotations</h2>
        <div class="annotations"></div>
    </div></div>
</div>

<script type="text/javascript" src="${cp}/static/js/rangy-1.3alpha.772/rangy-core.js"></script>
<script type="text/javascript" src="${cp}/static/js/rangy-1.3alpha.772/rangy-textrange.js"></script>
<script type="text/javascript">
    var model = [@json id=id text=text/];
    YUI().use("text-annotation", "datasource", function(Y) {
        var dataSource = new Y.DataSource.Local({ source: model });
        //var dataSource = new Y.DataSource.IO({ source: "data" });
        dataSource.plug({ fn: Y.Faust.TextSchema }).sendRequest({
            on: {
                success: function(e) {
                    text = e.response;

                    var milestones = text.milestones(), lineBreaks = text.lineBreaks(),
                        container = Y.one(".text"), annotationsContainer = Y.one(".annotations");

                    var line = null, newLine = function() {
                        line = container.appendChild("<p></p>");
                    };

                    var segments = {}, segment = function(start, end) {
                        if (line == null) newLine();
                        segments[line.appendChild("<span></span>").set("text", text.content([start, end])).generateID()] = [start, end];
                    };

                    for (var m = 1, mc = milestones.length, l = 0, lc = lineBreaks.length; m < mc; m++) {
                        var start = milestones[m - 1], end = milestones[m];
                        while (l < lc) {
                            var lb = lineBreaks[l];
                            if (lb <= start) {
                                newLine();
                                ++l;
                            } else if (lb > start && lb < end) {
                                segment(start, lb);
                                start = lb;
                            } else {
                                break;
                            }
                        }
                        if (start < end) segment(start, end);
                    }

                    var selection = rangy.getSelection();
                    container.on("mouseup", function() {
                        selection.refresh();
                        var a = Y.one(selection.anchorNode).ancestor("span", true),
                                b = Y.one(selection.focusNode).ancestor("span", true);

                        if (a == null || b == null) return;

                        var segmentA = segments[a.generateID()],
                                segmentB = segments[b.generateID()];

                        if (segmentA == null || segmentB == null) return;

                        var ao = segmentA[0] + selection.anchorOffset,
                                bo = segmentB[0] + selection.focusOffset,
                                segment = [Math.min(ao, bo), Math.max(ao, bo)];

                        if (segment[0] < segment[1]) {
                            Y.Array.each(text.index().find(segment), function(a) {
                                Y.Object.each(a, function(v, k) {
                                    this.append(Y.Lang.sub('<li><span class="attr-name">{name}</span>: {value}</li>', {
                                        name: k,
                                        value: v
                                    }));
                                }, this.appendChild("<ul></ul>").addClass("annotation"));
                            }, annotationsContainer.empty());
                        }
                    });
                },
                failure: function(e) {
                    alert(e.error.message);
                }
            }
        });
    });
</script>
[/@faust.page]