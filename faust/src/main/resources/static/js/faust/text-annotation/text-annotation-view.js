YUI.add("text-annotation-view", function(Y) {
    "use strict";

    var NS = Y.namespace("Faust");

    NS.TextView = Y.Base.create("text-view", Y.Widget, [], {
        initializer: function() {
            this.eventHandles = [];
            this.eventHandles.push(this.after(["textChange", "highlightSpacesChange"], this.syncUI, this));
        },
        destructor: function() {
            Y.Array.each(this.eventHandles, function(eh) { eh.detach(); });
        },
        bindUI: function() {
            this.get("contentBox").on("mouseup", function() {
                var selection = rangy.getSelection();

                selection.refresh();
                var a = Y.one(selection.anchorNode).ancestor("span", true),
                    b = Y.one(selection.focusNode).ancestor("span", true);

                if (a == null || b == null) return;

                var segments = this.get("segments"),
                    segmentA = segments[parseInt(a.getData("n"))],
                    segmentB = segments[parseInt(b.getData("n"))];

                if (segmentA == null || segmentB == null) return;

                var ao = segmentA[0] + selection.anchorOffset,
                    bo = segmentB[0] + selection.focusOffset,
                    segment = [Math.min(ao, bo), Math.max(ao, bo)];

                if (segment[0] < segment[1]) this.fire("textSelected", { text: this.get("text"), segment: segment });
            }, this);
        },
        syncUI: function() {
            var cb = this.get("contentBox").empty();

            var text = this.get("text"),
                highlightSpaces = this.get("highlightSpaces"),
                milestones = text.milestones(),
                lineBreaks = text.lineBreaks();

            var line = null, newLine = function() {
                line = cb.appendChild("<div></div>");
            };

            var segments = [], segment = function(start, end) {
                if (line == null) newLine();

                var content = text.content([start, end]);
                if (highlightSpaces) content = content.replace(/ /g, "\u2423");

                line.appendChild("<span></span>").set("text", content).setData("n", segments.length);
                segments.push([start, end]);
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

            this.set("segments", segments);
        }
    }, {
        ATTRS: {
            "text": { valueFn: function() { return new Y.Faust.Text(); } },
            "highlightSpaces": { value: false, validator: Y.Lang.isBoolean },
            "segments": { valueFn: function() { return []; } }
        }
    });

    NS.TextAnnotationDump =  Y.Base.create("text-annotation-dump", Y.Widget, [], {
        initializer: function() {
            this.eventHandles = [];
            this.eventHandles.push(this.after(["annotationsChange", "segmentChange"], this.syncUI, this));
        },
        destructor: function() {
            Y.Array.each(this.eventHandles, function(eh) { eh.detach(); });
        },
        syncUI: function() {
            var annotations = this.get("annotations"),
                segment = this.get("segment"),
                cb = this.get("contentBox").empty();

            if (segment) cb.appendChild("<h4></h4>").set("text", "[" + segment[0] + ", " + segment[1] + "]");

            var container = cb.appendChild("<div></div>").addClass("pure-g");
            for (var ac = annotations.length - 1; ac >= 0; ac--) {
                Y.Object.each(annotations[ac], function(v, k) {
                    this.append(Y.Lang.sub('<li><span class="attr-name">{name}</span>: {value}</li>', {
                        name: k,
                        value: v
                    }));
                }, container.appendChild("<div></div>").addClass("pure-u-1-5")
                    .appendChild("<ul></ul>").addClass("annotation")
                );
            }
        }
    }, {
        ATTRS: {
            segment: {},
            annotations: { valueFn: function() { return []; } }
        }
    });

}, "0.0", {
    requires: [ "widget", "text-annotation", "rangy-textrange" ]
});