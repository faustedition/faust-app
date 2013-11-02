YUI.add('documentary-transcript', function (Y) {
    var DRAG_NS = "http://www.codedread.com/dragsvg",
        NS = Y.namespace("Faust"),
        EPSILON = 0.01,
        LINE_OVERLAY_MODE = false,
        svg = Y.SvgUtils.svg;

    var Align = function(me, you, xy, myJoint, yourJoint, priority) {
        this.me = me;
        this.you = you;
        this.xy = xy;
        this.myJoint = myJoint;
        this.yourJoint = yourJoint;
        this.priority = priority;
    };

    Y.extend(Align, Object, {
        align: function() {
            var value = this.you.getCoord(this.xy);
            value -= this.myJoint * this.me.getExt(this.xy);
            value += this.yourJoint * this.you.getExt(this.xy);
            this.me.setCoord(value, this.xy);
        }
    }, {
        "IMPLICIT_BY_DOC_ORDER": "0",
        "REND_ATTR": "5",
        "INDENT_ATTR": "7",
        "INDENT_CENTER_ATTR": "8",
        "EXPLICIT": "10",
        "MAIN_ZONE": "15",

        "0": "IMPLICIT_BY_DOC_ORDER",
        "5": "REND_ATTR",
        "7": "INDENT_ATTR",
        "8": "INDENT_CENTER_ATTR",
        "10": "EXPLICIT",
        "15": "MAIN_ZONE"
    });


    var AbsoluteAlign = function (me, xy, coordinate, priority) {
        this.me = me;
        this.xy = xy;
        this.coordinate = coordinate;
        this.priority = priority;
    };

    Y.extend(AbsoluteAlign, Object, {
        align: function() {
            this.me.setCoord(this.coordinate, this.xy);
        }
    });

    var DocumentaryTranscriptNode = function(tree, config) {
        this.xmlName = (config["xml:name"] || "");
        this.nodeType = DocumentaryTranscriptNode.NODE_TYPES[this.xmlName] || (config["txt:content"] ? "text" : "");

        // FIXME make proper phrase/block context differentiation
        this.nodeType = ("anchor" == this.nodeType ? ("zone" == this.parent.nodeType ? "line" : "text") : this.nodeType);

        this.mainZone = ("zone" == this.nodeType) && (config["tei:type"] || config["ge:type"]);
        this.rotation = parseInt(config["tei:rotate"] || config["ge:rotate"] || "0") || 0;

        // default alignment
        var prev = this.previous();
        this.hAlign = new Align(this, (prev || this.parent), this.rotX(), 0, (prev ? 1 : 0), Align.IMPLICIT_BY_DOC_ORDER);
        this.vAlign = new Align(this, this.parent, this.rotY(), 0, 0, Align.IMPLICIT_BY_DOC_ORDER);
    };

    Y.extend(DocumentaryTranscriptNode, Object, {
        align: function(nodeMap) {
            // node-type specific alignment
            switch (this.nodeType) {
                case "zone":
                    if (this.mainZone) {
                        // main zone is absolutely anchored
                        this.setAlign('hAlign', new Faust.AbsoluteAlign(this, 0, 0, Align.MAIN_ZONE));
                        this.setAlign('vAlign', new Faust.AbsoluteAlign(this, 0, 0, Align.MAIN_ZONE));
                    }
                    break;
                case "vspace":
                    // FIXME: add cases for other breaking node types (BreakingVC)
                    this.setAlign("hAlign", new Align(this, this.parent, this.rotX(), 0, 0, Align.IMPLICIT_BY_DOC_ORDER));
                    this.setAlign("vAlign", new Align(this, prev || this.parent, this.rotY(), 0, (prev ? 1 : 0), Align.IMPLICIT_BY_DOC_ORDER));
                    break;
                case "line":
                    var rendition = (this.data["tei:rend"] || this.data["ge:rend"] || this.data["f:rend"] || ""), idx;
                    if ((idx = rendition.indexOf("indent-center")) >=0) {
                        var indentCenter = parseInt(rendition.substring(idx + 14, rendition.length)) / 100.0;
                        this.setAlign("hAlign", new Align(this, this.parent, this.rotX(), 0.5, indentCenter, Align.INDENT_CENTER_ATTR));
                    } else if ((idx = rendition.indexOf("indent")) >= 0) {
                        var indent = parseInt(rendition.substring(idx + 7, rendition.length)) / 100.0;
                        this.setAlign("hAlign", new Align(this, this.parent, this.rotX(), 0, indent, Align.INDENT_ATTR));
                    } else {
                        if (rendition.indexOf("centered") >= 0) {
                            // FIXME: NOOP? this.lineAttrs.center = true;
                        }
                        this.setAlign("hAlign", new Align(this, this.parent, this.rotX(), 0, 0, Align.IMPLICIT_BY_DOC_ORDER));
                    }

                    if (prev) {
                        var position = (this.data["f:pos"] || ""),
                            positionOver = (position.indexOf("over") >= 0),
                            positionBetween = (position.indexOf("between") >= 0),
                            yourJoint = 1.5;

                        if (LINE_OVERLAY_MODE) {
                            //yourJoint = (positionBetween ? 1 : 1);
                            yourJoint = (positionOver ? 0.1 : yourJoint);
                        } else {
                            yourJoint = (positionBetween ? 0.7 : yourJoint);
                            yourJoint = (positionOver ? 0.5 : yourJoint);
                        }
                        this.setAlign("vAlign", new Align(this, this.prev, this.rotY(), 0, yourJoint, Align.IMPLICIT_BY_DOC_ORDER));
                    } else {
                        this.setAlign("vAlign", new Align(this, this.parent, this.rotY(), 0, 0, Align.IMPLICIT_BY_DOC_ORDER));
                    }
                    break;
            }

            // @f:{at|left|...} alignment
            var alignmentKeys = ["f:at", "f:left", "f:left-right", "f:right", "f:right-left", "f:top", "f:top-bottom", "f:bottom", "f:bottom-top"];
            for (var kc = 0, kl = alignmentKeys.length; kc < kl; kc++) {
                var k = alignmentKeys[kc];

                var anchorId = (this.data[k] || "").replace(/^#/, "");
                if (!anchorId) continue;

                var coordRot = (k in {"f:at":1, "f:left":1, "f:left-right":1, "f:right":1, "f:right-left":1} ? 0 : 90);
                var alignName = (coordRot == 0 ? "hAlign" : "vAlign");
                var myJoint = (k in {"f:left":1, "f:left-right":1, "f:top":1, "f:top-bottom":1 } ? 0 : 1);
                var yourJoint = (k in {"f:at":1, "f:left":1, "f:right-left":1, "f:top":1, "f:bottom-top":1 } ? 0 : 1);

                var orient = this.data["f:orient"];
                if (orient) myJoint = (orient == "left" ? 1 : 0);

                //FIXME id hash hack; do real resolution of references
                var anchor = nodeMap[anchorId];
                if (!anchor) throw "Reference to #" + anchorId + " cannot be resolved!";

                this.setAlign(alignName, new Align(this, anchor, coordRot + anchor.globalRotation(), myJoint, yourJoint, Align.EXPLICIT));
            }

            // @rend alignment; TODO special treatment of zones
            var rendition = this.data["tei:rend"] || this.data["ge:rend"] || this.data["rend"];
            if (rendition == "right") {
                this.setAlign("hAlign", new Align(this, this.parent, this.parent.globalRotation(), 1, 1, Align.REND_ATTR));
            } else if (rendition == "left") {
                this.setAlign("hAlign", new Align(this, this.parent, this.parent.globalRotation(), 0, 0, Align.REND_ATTR));
            } else if (rendition == "centered") {
                this.setAlign("hAlign", new Align(this, this.parent, this.parent.globalRotation(), 0.5, 0.5, Align.REND_ATTR));
            }
        },
        getCoord: function(xy) {
            return Y.SvgUtils.getBoundingBox(this.view, this.view.viewportElement.createSVGMatrix().rotate(xy)).x;
        },
        getExt: function(xy) {
            return Y.SvgUtils.getBoundingBox(this.view, this.view.viewportElement.createSVGMatrix().rotate(xy)).width;
        },
        setCoord: function(coord, xy) {

            var myRot = this.globalRotation();
            var myRotMx = this.view.viewportElement.createSVGMatrix().rotate(myRot);
            var myRotTf = this.view.viewportElement.createSVGTransformFromMatrix(myRotMx);
            var myRotTfInv = this.view.viewportElement.createSVGTransformFromMatrix(myRotMx.inverse());

            var matrix = this.view.viewportElement.createSVGMatrix();
            var currentCoord = this.getCoord(xy);
            var deltaCoord = coord - currentCoord;

            matrix = matrix.rotate(xy);
            matrix = matrix.translate(deltaCoord, 0);
            matrix = matrix.rotate(-xy);
            var transform = this.view.viewportElement.createSVGTransformFromMatrix(matrix);
            this.view.transform.baseVal.consolidate();
            this.view.transform.baseVal.appendItem(myRotTfInv);
            this.view.transform.baseVal.appendItem(transform);
            this.view.transform.baseVal.appendItem(myRotTf);
            this.view.transform.baseVal.consolidate();
        },
        svgContainer: function() {
            if (this.parent)
                if (this.parent.view)
                    return this.parent.view;
                else
                    return this.parent.svgContainer();
            else
            if (this.svgCont)
                return this.svgCont;
            else
                throw ('Cannot find SVG container for view component');
        },
        svgDocument: function() {
            return document;
        },
        render: function(container) {
            switch (this.nodeType) {
                case "text":
                    // FIXME: read text attributes
                    var textAttrs = {}, classes = [];
                    Y.Object.each(textAttrs, function(v, k) {
                        switch (k) {
                            case "hand":
                                if (this.getWriter()) classes.push("hand-" + this.getWriter());
                                if (this.getMaterial()) classes.push("material-" + this.getMaterial());
                                if (this.getScript()) classes.push("script-" + this.getScript());
                                break;
                            case "rewrite":
                            case "under":
                            case "over":
                                classes.push(k);
                                break;
                            case "fontsize":
                                if ("small" == v) classes.push(v);
                                break;
                        }
                    });

                    var bbox = container.getBBox(),
                        bgBox = container.appendChild(svg("rect", { "class": classes.concat("bgBox").join(" ") })),
                        strikethrough = ("strikethrough" in textAttrs) && container.appendChild(svg("line")),
                        underline = ("underline" in textAttrs) && container.appendChild(svg("line"));

                    bgBox.setAttribute("x", bbox.x);
                    bgBox.setAttribute("y", bbox.y);
                    bgBox.setAttribute("height", bbox.height);
                    bgBox.setAttribute("width", bbox.width);
                    bgBox.transform.baseVal.initialize(container.transform.baseVal.consolidate());

                    if (underline) {
                        classes.push("underline");
                        underline.setAttribute("x1", this.x);
                        underline.setAttribute("x2", this.x + this.width);
                        underline.setAttribute("y1", this.y + this.height);
                        underline.setAttribute("y2", this.y + this.height);
                        //underline.setAttribute("stroke", this.handColor());
                        underline.setAttribute("class", classes.join(" "));
                        underline.transform.baseVal.initialize(container.transform.baseVal.consolidate());

                    }

                    if (strikethrough) {
                        var height = Math.round(this.view.getBBox().height / 6);
                        strikethrough.setAttribute("x1", this.x.toString());
                        strikethrough.setAttribute("x2", this.x + this.width.toString());
                        strikethrough.setAttribute("y1", (this.y - height).toString());
                        strikethrough.setAttribute("y2", (this.y - height).toString());
                        strikethrough.setAttribute("stroke", "#333");
                        strikethrough.transform.baseVal.initialize(container.transform.baseVal.consolidate());
                    }

                    container = container.appendChild(svg("text", { "class": classes.concat("text").join(" ") }));
                    container.appendChild(Y.config.doc.createTextNode(
                        this.tree.text.content(this.data["txt:content"] || [0, 0]).replace(/\s+/g, "\u00a0")
                    ));
                    break;
                case "line":
                    // FIXME: space at the beginning of each line, to give empty lines height
                    // vc.add (Y.Faust.DocumentLayout.createText("\u00a0", annotationStart, annotationEnd, text));
                    container = container.appendChild(svg("g", { "class": "Line" }));
                    break;
                case "zone":
                    var zone = svg("rect", {
                        "x": "0",
                        "y": "0",
                        "width": "0.1em",
                        "height": "0.1em"
                    }, {
                        "fill": "transparent",
                        "stroke": "black",
                        "visibility": "hidden"
                    });

                    container = container.appendChild(svg("g", { "class": "Zone" }));
                    container.setAttributeNS(DRAG_NS, 'drag:enable', 'true');
                    container.appendChild(zone);

                    /*
                     var sheet = Y.StyleSheet('#style-document-transcript-highlight-hands');

                     Y.one(container).on("mouseenter", function () {
                     //Y.all('.bgBox').transition('fadeIn');
                     sheet.enable();
                     });

                     Y.one(container).on("mouseleave", function () {
                     //Y.all('.bgBox').transition('fadeOut');
                     sheet.disable();
                     });
                     */
                    break;
                case "surface":
                    container = container.appendChild(svg("g", { "class": "Surface" }));
                    break;
                case "vspace":
                    if (this.data["f:unit"] != "lines") throw ("Invalid unit for vspace element! Use 'lines'!");
                    if (!this.data["f:quantity"]) throw ("f:vspace: Please specify @quantity");

                    //TODO real implementation, non-integer values
                    container = container.appendChild(svg("rect", {
                        "class": "VSpace",
                        "width": "0.1em",
                        "height": ((parseInt(this.data["f:quantity"]) || 0) * 2) + 'em'
                    }, {
                        "visibility": "hidden"
                    }));
                    break;
                case "hspace":
                    if (this.data["f:unit"] != "chars") throw "Invalid unit for hspace element! Use 'chars'!";
                    if (!this.data["f:quantity"]) throw "f:hspace: Please specify @quantity";

                    //TODO real implementation, non-integer values
                    container = container.appendChild(Y.SvgUtils("rect", {
                        "class": "HSpace",
                        "width": ((parseInt(this.data["f:quantity"]) || 0) * 0.5) + 'em',
                        "height": "0.1em"
                    }, {
                        "visibility": "hidden"
                    }));
                    break;
                case "grLine":
                    var lineStyle = this.data["f:style"];
                    if (lineStyle && lineStyle != "curly") Y.log("Unsupported style for grLine: " + lineStyle);

                    // FIXME: zoneSpanning added to the container, but another dummy rect returned
                    container.appendChild(svg("rect", {
                        "width": "100",
                        "height": this.parent.getExt(this.parent.rotY()),
                        "fill": 'url(#curlyLinePattern)',
                        "x": "0",
                        "y": "0"
                    }));

                    container = container.appendChild(svg("rect", {
                        "width": "0.1em",
                        "height": "0.1em"
                    }, {
                        "fill": "transparent",
                        "stroke": "black",
                        "visibility": "hidden"
                    }));
                    break;
                case "gLine":
                    // FIXME: still used?
                    container = container.appendChild(svg("line", {
                        "x1": this.x,
                        "y1": (this.y - 10),
                        "x2": (this.x + 40),
                        "y2": (this.y - 10),
                        "stroke-width": "1",
                        "stroke": "black"
                    }));
                    break;
                case "grBrace":
                    container = container.appendChild(svg("path", {
                        "d": "M " + ((this.x) + " " + (this.y) + " q 5,-10 20,-5 q 5,0 10,-10 q -5,0 10,10 q 10,-5 20,5"),
                        "stroke-width": "1",
                        "stroke": "black",
                        "fill": "transparent"

                    }));
                    break;
            }

            if (this.rotation) {
                var vp = container.viewportElement,
                    matrix = vp.createSVGMatrix().rotate(this.rotation),
                    transform = vp.createSVGTransformFromMatrix(matrix);

                container.transform.baseVal.insertItemBefore(transform, 0);
            }

            // recurse
            for (var cc = 0, cl = this.children.length; cc < cl; cc++) this.children[cc].render(container);

            return (this.container = container);
        },
        globalRotation: function () {
            var rotation = 0;
            for (var node = this; node; node = node.parent) rotation += node.rotation;
            return rotation;
        },
        layout: function() {
            var oldX = this.x,
                oldY = this.y,
                oldWidth = this.width,
                oldHeight = this.height;

            //Y.each(this.children, function(c) { c.computeDimension(); });

            switch (this.nodeType) {
                case "text":
                    // FIXME: render text off-screen for measurements
                    var bbox = this.view.getBBox();
                    this.width = Math.round(bbox.width);
                    this.height = Math.round(bbox.height);
                case "line":
                    // FIXME: Line: NOOP ?
                    break;
                case "gLine":
                case "grLine":
                case "grBrace":
                    this.width = 40;
                    this.height = 20;
                    break;
                default:
                    this.width = 0;
                    this.height = 0;
                    for (var cc = 0, cl = this.children.length; cc < cl; cc++) {
                        var child = this.children[cc];
                        this.width = Math.max(this.width, child.width);
                        this.height += c.height;
                    }
            }

            switch (this.nodeType) {
                case "surface":
                    this.x = 0;
                    this.y = 0;
                    break;
                default:
                    this.hAlign.align();
                    this.vAlign.align();
            }

            //Y.each(this.children, function(c) { c.computePosition(); });

            this.layoutSatisfied =
                (abs(oldWidth - this.width) < EPSILON) &&
                (abs(oldHeight - this.height) < EPSILON) &&
                (abs(oldX - this.x) < EPSILON) &&
                (abs(oldY - this.y) < EPSILON);

            var dimension = {
                xMin: this.x,
                yMin: this.y,
                xMax: (this.x + this.width),
                yMax: (this.y + this.height)
            };
            for (var cc = 0, cl = this.children.length; cc < cl; cc++) {
                var child = this.children[cc];
                dimension = {
                    xMin: Math.min(dimension.xMin, child.x),
                    xMax: Math.max(dimension.xMax, child.x + child.width),
                    yMin: Math.min(dimension.yMin, child.y),
                    yMax: Math.max(dimension.yMax, child.y + child.height)
                };
            }

            return dimension;
        },
        rotX: function() {
            return 0 + this.globalRotation()
        },
        rotY: function() {
            return 90 + this.globalRotation()
        },
        setAlign: function (name, align) {
            if (this[name] && align.priority === this[name].priority)  throw("ENCODING ERROR: Conflicting alignment instructions for element " +
                this.xmlName + " #" + (this.data["xml:id"] || "") + " (" + name + ", " + Align[align.priority] + " )");

            if (!this[name] || (align.priority > this[name].priority)) this[name] = align;
        },
        getHand: function() {
            // Text
            if (this.textAttrs.rewrite)
                return this.textAttrs.rewrite;
            else
                return this.textAttrs["hand"];
        },
        getWriter: function() {
            // Text
            return this.getHand().split('_')[0];
        },
        getMaterial: function() {
            // Text
            return this.getHand().split('_')[1];
        },
        getScript: function() {
            // Text
            return this.getHand().split('_')[2];
        }
    }, {
        NODE_TYPES: {
            "ge:document": "surface",
            "tei:document": "surface",
            "tei:surface": "surface",
            "tei:zone": "zone",
            "ge:zone" : "zone",
            "tei:line": "line",
            "ge:line": "line",
            "f:vspace": "vspace",
            "f:hspace": "hspace",
            "f:grLine": "grLine",
            "f:grBrace": "grBrace",
            "tei:anchor": "anchor",
            "f:ins": "ins"
        }
    });

    NS.DocumentaryTranscriptModel = function() {
    };

    Y.extend(NS.DocumentaryTranscriptModel, Object, {
        initializer: function () {
            this.nodeExtensions = this.nodeExtensions.concat(DocumentaryTranscriptNode);
        }
    });
}, "0.0", {
    requires: [ "svg-utils", "array-extras" ]
});