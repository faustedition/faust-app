YUI.add("text-annotation-dump", function(Y) {
    "use strict";

    Y.namespace("Faust").TextAnnotationDump = Y.Base.create("text-annotation-dump", Y.Widget, [], {
        initializer: function() {
            this.text = null;
            this.segments = {};
        },
        destructor: function() {
            this.segments = null;
            this.text && this.text.destroy();
        },
        renderUI: function() {
            var cb = this.get("contentBox").addClass("pure-g");

            this.textNode = cb.appendChild("<div></div>").addClass("pure-u-3-5").appendChild("<div></div>").addClass("l-box").addClass("text");
            this.annotationsNode = cb.appendChild("<div></div>").addClass("pure-u-2-5").appendChild("<div></div>").addClass("l-box").addClass("annotations");
        },
        bindUI: function() {
            this.textNode.on("mouseup", function() {
                var selection = rangy.getSelection();

                selection.refresh();
                var a = Y.one(selection.anchorNode).ancestor("span", true),
                    b = Y.one(selection.focusNode).ancestor("span", true);

                if (a == null || b == null) return;

                var segmentA = this.segments[a.generateID()],
                    segmentB = this.segments[b.generateID()];

                if (segmentA == null || segmentB == null) return;

                var ao = segmentA[0] + selection.anchorOffset,
                    bo = segmentB[0] + selection.focusOffset,
                    segment = [Math.min(ao, bo), Math.max(ao, bo)];

                if (segment[0] < segment[1]) {
                    var annotations = this.text.index().find(segment);
                    this.annotationsNode.empty().appendChild("<h4></h4>").set("text", "[" + segment[0] + ", " + segment[1] + "]");
                    for (var ac = annotations.length - 1; ac >= 0; ac--) {
                        Y.Object.each(annotations[ac], function(v, k) {
                            this.append(Y.Lang.sub('<li><span class="attr-name">{name}</span>: {value}</li>', {
                                name: k,
                                value: v
                            }));
                        }, this.annotationsNode.appendChild("<ul></ul>").addClass("annotation"));
                    }
                }
            }, this);
        },
        syncUI: function() {
            var ds = this.get("datasource");
            if (!ds.schema) ds.plug({ fn: Y.Faust.TextSchema });
            ds.sendRequest({
                on: {
                    success: Y.bind(this.textRetrieved, this),
                    failure: function(e) { alert(e.error.message); }
                }
            });
        },
        textRetrieved: function(e) {
            var text = (this.text = e.response),
                milestones = text.milestones(),
                lineBreaks = text.lineBreaks();

            var line = null, container = this.textNode, newLine = function() {
                line = container.appendChild("<div></div>");
            };

            var segments = (this.segments = {}), segment = function(start, end) {
                if (line == null) newLine();
                segments[line.appendChild("<span></span>").set("text", text.content([start, end]).replace(/ /g, "\u2423")).generateID()] = [start, end];
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
        }
    }, {
        ATTRS: {
            "datasource": {}
        }
    });
}, "0.0", {
    requires: [ "widget", "text-annotation", "datasource", "rangy-textrange" ]
});