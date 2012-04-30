YUI.add('facsimile', function (Y) {

    var SVG_NS = "http://www.w3.org/2000/svg",
        XLINK_NS = "http://www.w3.org/1999/xlink",
        NULL_IMAGE_VALUE = { width: Number.MAX_VALUE, height: Number.MAX_VALUE, tileSize: 1, maxZoom: 0 },
        NULL_VIEW_VALUE = { x: 0, y: 0, width: 0, height: 0, zoom: 0 };

    function qscale(degree) {
        return function (val) {
            return val * Math.pow(2, degree);
        };
    }

    function svgElement(name) {
        return Y.config.doc.createElementNS(SVG_NS, name);
    }

    function svgAttrs(element, attrs) {
        Y.Object.each(attrs, function(v, k) {
            element.setAttribute(k, v);
        });
        return element;
    }

    function svgStyles(element, styles) {
        Y.Object.each(styles, function(v, k) {
            element.style[k] = v;
        });
        return element;
    }

    function empty(element) {
        while (element.firstChild) {
            element.removeChild(element.firstChild);
        }
    }

    var TiledViewModel = Y.Base.create("tile-view-model", Y.Base, [], {
        initializer: function () {
            this.imageChangeSub = this.after("imageChange", this.generateTiles, this);
            this.viewChangeSub = this.after("viewChange", this.generateTiles, this);
        },
        destructor: function () {
            this.viewChangeSub.detach();
            this.imageChangeSub.detach();
        },
        generateTiles: function () {
            var view = this.get("view"),
                tileSize = this.get("image").tileSize,
                startX = Math.max(0, Math.floor(Math.min(view.imageWidth, view.x) / tileSize)),
                startY = Math.max(0, Math.floor(Math.min(view.imageHeight, view.y) / tileSize)),
                endX = Math.ceil(Math.min(view.imageWidth, view.x + view.width) / tileSize),
                endY = Math.ceil(Math.min(view.imageHeight, view.y + view.height) / tileSize);

            var tiles = [];
            for (var y = startY; y < endY; y++) {
                for (var x = startX; x < endX; x++) {
                    tiles.push({ x: x, y: y });
                }
            }
            this.set("tiles", tiles);
        },
        center: function () {
            var view = this.get("view");
            this.moveTo(
                Math.max(view.imageWidth - view.width, 0) / 2,
                Math.max(view.imageHeight - view.height, 0) / 2
            );
        },
        centerOn: function (x, y) {
            var view = this.get("view");
            this.moveTo(
                x - Math.min(view.imageWidth, view.width) / 2,
                y - Math.min(view.imageHeight, view.height) / 2
            );
        },
        moveTo: function (x, y) {
            var view = this.get("view");
            this.set("view", {
                x: x || 0,
                y: y || 0
            });
        },
        move: function (x, y) {
            var view = this.get("view");
            this.moveTo(
                view.x + (x || 0),
                view.y + (y || 0)
            );
        },
        zoom: function (zoom) {
            var view = this.get("view"), prev = this.get("zoom"), prevScale = qscale(prev);
            this.set("zoom", prev + (zoom || 0));
            var nextScale = qscale(-this.get("zoom"));
            this.centerOn(
                nextScale(prevScale(view.x + (Math.min(view.imageWidth, view.x + view.width) - view.x) / 2)),
                nextScale(prevScale(view.y + (Math.min(view.imageHeight, view.y + view.height) - view.y) / 2)));
        },
        fitToView: function () {
            var image = this.get("image"), view = this.get("view");
            var zoom = Math.round(Math.log(Math.max((image.width / view.width), (image.height / view.height))) / Math.log(2));
            this.zoom(zoom - this.get("zoom"));
            this.center();
        }
    }, {
        ATTRS: {
            image: {
                value: NULL_IMAGE_VALUE,
                validator: function (val) {
                    return (val.width >= 0) && (val.height >= 0) && (val.maxZoom >= 0) && (val.tileSize > 0);
                }
            },
            view: {
                value: NULL_VIEW_VALUE,
                setter: function (val) {
                    var image = this.get("image"),
                        scale = qscale(-this.get("zoom")),
                        imageWidth = scale(image.width),
                        imageHeight = scale(image.height),
                        view = this.get("view") || NULL_VIEW_VALUE,
                        isNumber = Y.Lang.isNumber;
                    return {
                        x: Math.min(Math.max(isNumber(val.x) ? val.x : view.x, 0), Math.max(0, imageWidth - view.width)),
                        y: Math.min(Math.max(isNumber(val.y) ? val.y : view.y, 0), Math.max(0, imageHeight - view.height)),
                        width: Math.max(isNumber(val.width) ? val.width : view.width, 0),
                        height: Math.max(isNumber(val.height) ? val.height : view.height, 0),
                        imageWidth: imageWidth,
                        imageHeight: imageHeight
                    };
                }
            },
            zoom: {
                value: 0,
                setter: function (val) {
                    return Math.min(Math.max(val || 0, 0), this.get("image").maxZoom);
                }
            },
            tiles: {
                valueFn: function () {
                    return []
                }
            }
        }
    });

    var FacsimileViewer = Y.Base.create("facsimile-viewer", Y.Widget, [], {
        initializer: function (config) {
            var view = config.view || NULL_VIEW_VALUE;

            var svgRoot = svgStyles(svgAttrs(svgElement("svg"), {
                "version": "1.1",
                "width": view.width,
                "height": view.height
            }), {
                background: "black",
                margin: "1em auto",
                border: 0,
                padding: 0,
                position: "relative",
                overflow: "hidden"
            });
            this.mask = svgRoot.appendChild(svgElement("defs")).appendChild(svgAttrs(svgElement("mask"), {
                id: "highlight",
                x: "0",
                y: "0",
                width: "100%",
                height: "100%"
            }));
            this.svg = this.get("contentBox").getDOMNode().appendChild(svgRoot).appendChild(svgStyles(svgElement("g"), {
                mask: "url(#highlight)"
            }));

            this.model = new TiledViewModel({ view: view });
            this.modelChangeSub = this.model.after(["tilesChange", "viewChange", "imageChange"], this.syncUI, this);

            Y.io(this.imageSrc(), {
                data: {metadata: true },
                on: { success: Y.bind(this.metadataReceived, this) }
            });
        },
        destructor: function () {
            this.modelChangeSub.detach();
        },
        renderUI: function () {
        },
        metadataReceived: function (transactionId, response) {
            this.model.set("image", Y.merge(this.get("image"), Y.JSON.parse(response.responseText)));
            this.model.fitToView();
        },
        imageSrc: function () {
            return cp + "/facsimile/gsa/391098/391098_0001";
        },
        tileSrc: function (x, y) {
            return Y.substitute("{imageSrc}?x={x}&y={y}&zoom={zoom}", {
                imageSrc: this.imageSrc(),
                x: x,
                y: y,
                zoom: this.model.get("zoom")
            });
        },
        syncUI: function () {
            var view = this.model.get("view"),
                xOffset = Math.floor(Math.max(0, (view.width - view.imageWidth) / 2)),
                yOffset = Math.floor(Math.max(0, (view.height - view.imageHeight) / 2)),
                tileSize = this.model.get("image").tileSize,
                tiles = this.model.get("tiles");

            empty(this.svg);
            svgAttrs(this.svg.parentNode, {
                "width": view.width,
                "height": view.height
            });

            empty(this.mask);
            this.mask.appendChild(svgStyles(svgAttrs(svgElement("rect"), {
                x: 0,
                y: 0,
                width: "100%",
                height: "100%"
            }), {
                fill: "#999"
            }));
            this.mask.appendChild(svgStyles(svgAttrs(svgElement("rect"), {
                x: "10%",
                y: "40%",
                rx: "10",
                ry: "10",
                width: "80%",
                height: "20%"
            }), {
                fill: "#fff",
                stroke: "#fff",
                strokeWidth: "10"
            }));

            var fixedCoordOffset = (Y.UA.gecko && 1 || 0);
            Y.Array.each(tiles, function (tile) {
                var x = tile.x * tileSize, y = tile.y * tileSize;
                this.svg.appendChild(svgAttrs(svgElement("image"), {
                    "x": Math.floor(x - view.x + xOffset),
                    "y": Math.floor(y - view.y + yOffset),
                    "width": Math.min(tileSize, view.imageWidth - x) + fixedCoordOffset,
                    "height": Math.min(tileSize, view.imageHeight - y) + fixedCoordOffset
                })).setAttributeNS(XLINK_NS, "href", this.tileSrc(tile.x, tile.y));
            }, this);
        }
    });

    Y.mix(Y.namespace("Faust"), { FacsimileViewer: FacsimileViewer });
}, '0.0', {
    requires: ["base", "widget", "substitute", "array-extras", "io", "json"]
});