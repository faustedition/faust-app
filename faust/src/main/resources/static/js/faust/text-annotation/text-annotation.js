YUI.add('text-annotation', function (Y) {

    var SEGMENT_KEY = "txt:segment", ID_KEY = "txt:id";

    var Text = function(content, annotations, tree, segment, length) {
        this._content = (content || "");
        this.annotations = (annotations || []);
        this.tree = tree;
        this.length = (length || this._content.length);
        this.segment = (segment || [0, this.length]);
    };

    Y.extend(Text, Object, {
        offset: function() {
            return this.segment[0];
        },
        content: function(segment) {
            if (!segment) return this._content;
            var offset = this.offset(), length = this._content.length;
            return this._content.substring(Math.max(0, segment[0] - offset), Math.min(segment[1] - offset, length));
        },
        milestones: function() {
            if (this._milestones) return this._milestones;

            var milestones = [];
            Y.Object.each(this.annotations, function (a) {
                var ms = 0, me = milestones.length, segment = a[SEGMENT_KEY], start = segment[0], end = segment[1];
                while (ms < me && milestones[ms] < start) ms++;
                if (ms == me || milestones[ms] != start) {
                    milestones.splice(ms, 0, start);
                    me++;
                }
                me--;
                while (me > ms && milestones[me] > end) me--;
                if (milestones[me] != end) milestones.splice(me + 1, 0, end);
            }, this);

            if (milestones.length == 0 || milestones[0] > this.segment[0]) milestones.unshift(this.segment[0]);
            if (milestones.length == 1 || milestones[milestones.length - 1] < this.segment[1]) milestones.push(this.segment[1]);

            return (this._milestones = milestones);
        },
        lineBreaks: function() {
            var lb = this._content.indexOf("\n", 0), offset = this.offset(), newLines = [];
            while (lb != -1) {
                newLines.push(offset + lb);
                lb = this._content.indexOf("\n", lb + 1);
            }
            return newLines;
        },
        index: function() {
            if (this._index) return this._index;

            var annotations = [];
            Y.Object.each(this.annotations, function(a) { this.push([a[SEGMENT_KEY], a]); }, annotations);
            return (this._index = new Y.Faust.SegmentIndex(annotations));
        }
    });

    var TextSchema = Y.Base.create("text-schema", Y.Plugin.Base, [], {
        initializer: function() {
            this.doBefore("_defDataFn", this._beforeDefDataFn);
        },
        _beforeDefDataFn: function(e) {
            var data = e.data && (e.data.responseText || e.data),
                text = (Y.Lang.isString(data) ? Y.JSON.parse(data) : data),
                payload = e.details[0];

            if (Y.Lang.isObject(text) && text.text) text = text.text;

            var tree = null,
                offset = 0,
                content = "",
                annotations = {},
                treeNodes = {},
                treeStack = [],
                treeFilter = this.get("treeFilter");

            for (var tc = 0, tl = text.length; tc < tl; tc++) {
                var t = text[tc],
                    parentId = (treeStack.length == 0 ?  null : treeStack[treeStack.length - 1]),
                    parent = (parentId == null ? null : treeNodes[parentId]);

                if (Y.Lang.isString(t)) {
                    var end = offset + t.length;
                    if (parent) {
                        var data = [];
                        data[SEGMENT_KEY] = [ offset, end ];
                        parent.append(data);
                    }
                    content += t;
                    offset = end;
                } else if (t.s && t.d) {
                    var data = annotations[t.s] = t.d;
                    data[ID_KEY] = t.s;
                    data[SEGMENT_KEY] = [ offset ];

                    if (treeFilter(t.d)) {
                        var node = (parent ? parent.append(tree.createNode()) : (tree = new Y.Tree()).rootNode);
                        node.data = data;
                        treeNodes[t.s] = node;
                        treeStack.push(t.s);
                    }
                } else if (t.e) {
                    annotations[t.e][SEGMENT_KEY].push(offset);

                    if (parentId && (t.e == parentId)) {
                        treeStack.pop();
                        delete treeNodes[t.e];
                    }
                }
            }

            payload.response = new Text(content, annotations, tree);

            this.get("host").fire("response", payload);

            return new Y.Do.Halt("TextSchema plugin halted _defDataFn");
        }
    }, {
        NS: "schema",

        ATTRS: {
            treeFilter: {
                value: function(t) { return t["xml:name"]; },
                validator: Y.Lang.isFunction
            }
        }
    });


	Y.mix(Y.namespace("Faust"), {
		Text: Text,
        TextSchema: TextSchema
	});
}, '0.0', {
	requires: ["text-index", "base", "substitute", "array-extras", "io", "json", "plugin", "tree"]
});