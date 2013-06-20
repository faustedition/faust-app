YUI.add('facsimile', function (Y) {

    var NS = Y.namespace("Faust"),
        SVG_NS = "http://www.w3.org/2000/svg",
        XLINK_NS = "http://www.w3.org/1999/xlink",
        NULL_IMAGE_VALUE = { width: Number.MAX_VALUE, height: Number.MAX_VALUE, tileSize: 1, maxZoom: 0 },
        NULL_VIEW_VALUE = { x: 0, y: 0, width: 0, height: 0, zoom: 0 };

	var svg = Y.SvgUtils.svg,
        qscale = Y.SvgUtils.qscale,
        svgAttrs = Y.SvgUtils.svgAttrs,
        empty = Y.SvgUtils.empty;

    NS.FacsimileWidget = Y.Base.create("facsimile", Y.Widget, [], {
        renderUI: function () {
            var contentBox = this.get("contentBox"),
                controls = contentBox.appendChild("<div></div>").addClass(this.getClassName("controls")),
                view = contentBox.appendChild("<div></div>").addClass(this.getClassName("view")),
                viewSize = this.get("viewSize");

            this.centerBtn = controls.appendChild("<button></button>").set("text", "Center");
            this.zoomInBtn = controls.appendChild("<button></button>").set("text", "+");
            this.zoomOutBtn = controls.appendChild("<button></button>").set("text", "-");
            this.rotateLeftBtn = controls.appendChild("<button></button>").set("text", "<");
            this.rotateRightBtn = controls.appendChild("<button></button>").set("text", ">");
            this.positionLabel = controls.appendChild("<span></span>").addClass(this.getClassName("position-label"));

            this.svg = SVG(view.getDOMNode()).size(viewSize.width, viewSize.height).style("background", "black");
            this.pane = this.svg.group().attr({ id: "pane" }).draggable();
            this.pane.dragend = Y.bind(this.paneDragged, this);
            this.pane.add(this.facsimile = this.svg.group().attr({ id: "image" }));

            facsimileSvg = this.svg;
        },
        bindUI: function () {
            this.after(["rotateChange", "zoomChange"], this.redraw, this);
            this.after("positionChange", this.updatePositionLabel, this);

            this.centerBtn.on("click", this.center, this);
            this.zoomInBtn.on("click", Y.bind(this.zoom, this, -1), this);
            this.zoomOutBtn.on("click", Y.bind(this.zoom, this, +1), this);
            this.rotateLeftBtn.on("click", Y.bind(this.rotate, this, -15), this);
            this.rotateRightBtn.on("click", Y.bind(this.rotate, this, 15), this);
        },
        syncUI: function () {
            this.redraw();
            this.center();
        },
        center: function() {
            var viewSize = this.get("viewSize");
            this.move(-this.facsimile.cx() + (viewSize.width / 2), -this.facsimile.cy() + (viewSize.height / 2));
        },
        zoom: function(delta) {
            this.set("zoom", this.get("zoom") + delta);
            this.center();
        },
        rotate: function(delta) {
            this.set("rotate", this.get("rotate") + delta);
        },
        redraw: function() {
            var attrs = this.getAttrs(), size = attrs.zoomedSize, tileSize = attrs.tileSize;

            this.panelSize = Math.ceil(Math.sqrt(Math.pow(size.width, 2) + Math.pow(size.height, 2))),
            this.panelCenter = this.panelSize / 2;
            this.offsetX = (this.panelSize - size.width) / 2;
            this.offsetY = (this.panelSize - size.height) / 2;

            this.facsimile.clear().x(this.offsetX).y(this.offsetY).rotate(attrs.rotate, this.panelCenter, this.panelCenter);
            for (var y = 0, tilesY = Math.ceil(size.height / tileSize); y < tilesY; y++) {
                for (var x = 0, tilesX = Math.ceil(size.width / tileSize); x < tilesX; x++) {
                    this.facsimile.add(this.svg.rect(
                        Math.min(tileSize, size.width - (x * tileSize)),
                        Math.min(tileSize, size.height - (y * tileSize))
                    ).move(x * tileSize, y * tileSize).fill("#9c9"));
                }
            }
        },
        updatePositionLabel: function() {
            var position = this.get("position");
            this.positionLabel.set("text", "[" + position.x + ", " + position.y + "]");
        },
        paneDragged: function() {
            this.set("position", { x: this.pane.x(), y: this.pane.y() });
        },
        move: function (x, y) {
            var pos = { x: x || 0, y: y || 0};
            this.pane.move(pos.x, pos.y);
            this.set("position", pos);
        }
    }, {
        ATTRS: {
            maxZoom: {
                value: 0
            },
            zoom: {
                value: 0,
                setter: function (val) { return Math.min(Math.max(val || 0, 0), this.get("maxZoom")); }
            },
            rotate: {
                value: 0,
                setter: function (val) { return (val % 360); }
            },
            position: {
                value: { x: 0, y: 0}
            },
            viewSize: {
                value: { width: 0, height: 0 }
            },
            imageSize: {
                value: { width: 0, height: 0 }
            },
            tileSize: {
                value: 256
            },
            zoomedSize: {
                readOnly: true,
                getter: function () {
                    var imageSize = this.get("imageSize"), scale = qscale(-this.get("zoom"));
                    return { width: scale(imageSize.width), height: scale(imageSize.height) };
                }
            }
        }
    });


    NS.ZoomImagePlugin = Y.Base.create("zoom-image-widget", Y.Plugin.Base, [], {
    }, {
        NS: "zoomImage",
        ATTRS: {}
    });

    var TiledViewModel = Y.Base.create("tile-view-model", Y.Base, [], {
        center: function () {
            var view = this.get("view");
            this.panTo(
                Math.max(view.imageWidth - view.width, 0) / 2,
                Math.max(view.imageHeight - view.height, 0) / 2
            );
        },
        centerOn: function (x, y) {
            var view = this.get("view");
            this.panTo(
                x - Math.min(view.imageWidth, view.width) / 2,
                y - Math.min(view.imageHeight, view.height) / 2
            );
        },
        moveTo: function(x, y) {
            var scale = qscale(-this.get("zoom"));
            this.panTo(scale(x || 0), scale(y || 0));
        },
        move: function(x, y) {
            var scale = qscale(-this.get("zoom"));
            this.pan(scale(x || 0), scale(y || 0));
        },
        panTo: function (x, y) {
            this.set("view", { x: x || 0, y: y || 0 });
        },
        pan: function (x, y) {
            var view = this.get("view");
            this.panTo(view.x + (x || 0), view.y + (y || 0));
        },
        zoom: function (zoom) {
            var view = this.get("view"), prev = this.get("zoom"), prevScale = qscale(prev);
            this.set("zoom", prev + (zoom || 0));
            var nextScale = qscale(-this.get("zoom"));
            this.centerOn(
                nextScale(prevScale(view.x + (Math.min(view.imageWidth, view.x + view.width) - view.x) / 2)),
                nextScale(prevScale(view.y + (Math.min(view.imageHeight, view.y + view.height) - view.y) / 2)));
        },
        fittingZoom: function(width, height) {
            var image = this.get("image"), view = this.get("view");
            return Math.round(Math.log(Math.max(((width || image.width) / view.width), ((height || image.height) / view.height))) / Math.log(2));
        },
        fitToView: function (width, height) {
            this.zoom(this.fittingZoom(width, height) - this.get("zoom"));
        }
    }, {
        ATTRS: {
            maxZoom: { value: 0 },
            zoom: {
                value: 0,
                setter: function (val) { return Math.min(Math.max(val || 0, 0), this.get("maxZoom")); }
            },
            tileSize: { value: 1 },
            image: {
                value: { width: 0, height: 0 }
            },
            view: {
                value: { width: 0, height: 0 }
            },
            position: {
                value: { x: 0, y: 0 },
                setter: function (val) {
                    var image = this.get("image"), scale = qscale(-this.get("zoom"));
                    return {
                        x: Math.min(Math.max(val.x || 0, 0), scale(image.width)),
                        y: Math.min(Math.max(val.y || 0, 0), scale(image.height))
                    };
                }
            },
            tiles: {
                readOnly: true,
                getter: function () {
                    var state = this.getAttrs(["tileSize", "origin", "dimension", "zoom"]),
                        scaledWidth = state.zoom.scale(state.dimension.width),
                        scaledHeight = state.zoom.scale(state.dimension.height),
                        startX = Math.max(0, Math.floor(Math.min(scaledWidth, state.origin.x) / state.tileSize)),
                        startY = Math.max(0, Math.floor(Math.min(scaledHeight, state.origin.y) / state.tileSize)),
                        endX = Math.ceil(Math.min(scaledWidth, state.origin.x + state.dimension.width) / state.tileSize),
                        endY = Math.ceil(Math.min(scaledHeight, state.origin.y + state.dimension.height) / state.tileSize),
                        tiles = [];
                    for (var y = startY; y < endY; y++) {
                        for (var x = startX; x < endX; x++) {
                            tiles.push({ x: x, y: y });
                        }
                    }
                    return tiles;
                }
            }
        }
    });

    var FacsimileViewer = Y.Base.create("facsimile-viewer", Y.Widget, [], {
        initializer: function (config) {
            this.src = config.src;
            this.model = new TiledViewModel(config.view || NULL_VIEW_VALUE);

            //this.highlightPane = new HighlightPane({ view: this.view, model: this.model });
            //this.svgPane = new SvgPane({ svgSrc: config.svgSrc, view: this.view, model: this.model });

            //Y.io(this.metadataSrc(), { on: { success: Y.bind(this.metadataReceived, this) } });
        },
        destructor: function () {
            this.navigationSub && this.navigationSub.detach();
            this.modelChangeSub && this.modelChangeSub.detach();
        },
        renderUI: function () {
            var contentBox = this.get("contentBox"), dimension = this.model.get("dimension");
            this.view = contentBox.getDOMNode().appendChild(svg("svg", {
                "version": "1.1",
                "width": dimension.width,
                "height": dimension.height
            }, {
                background: "black",
                margin: "1em auto",
                border: 0,
                padding: 0,
                position: "relative",
                overflow: "hidden"
            })).appendChild(svg("g"));
        },
        bindUI: function () {
            this.modelChangeSub = this.model.after(["change"], this.syncUI, this);
            this.navigationSub = Y.one("body").on('key', Y.bind(this.navigate, this), 'down:37,38,39,40,48,67,107,109,187,189');
        },
        navigate: function(e) {
            var view = this.model.get("view"), moveX = Math.floor(view.width / 4), moveY = Math.floor(view.height / 4);
            switch (e.keyCode) {
                case 37:
                    // arrow left
                    this.model.pan(-moveX, 0);
                    break;
                case 38:
                    // arrow up
                    this.model.pan(0, -moveY);
                    break;
                case 39:
                    // arrow right
                    this.model.pan(moveX, 0);
                    break;
                case 40:
                    // arrow down
                    this.model.pan(0, moveY);
                    break;
                case 48:
                    // 0
                    this.model.fitToView();
                    this.model.center();
                    break;
                case 67:
                    // c
                    this.model.center();
                    break;
                case 107:
                case 187:
                    // +
                    this.model.zoom(-1);
                    return;
                case 109:
                case 189:
                    // -
                    this.model.zoom(1);
                    return;
            }

        },
        metadataReceived: function (transactionId, response) {
            this.model.set("image", Y.merge(this.get("image"), Y.JSON.parse(response.responseText)));
            this.model.fitToView();
            this.model.center();
        },
        imageSrc: function () {
			return (cp + "/facsimile/" + this.src);
        },
        metadataSrc: function () {
            return (cp + "/facsimile/metadata/" + this.src);
        },
        tileSrc: function (x, y, zoom) {
            return Y.substitute("{imageSrc}?x={x}&y={y}&zoom={zoom}", {
                imageSrc: this.imageSrc(),
                x: x,
                y: y,
                zoom: zoom
            });
        },
        syncUI: function () {
            var state = this.model.getAttrs("dimension", "origin", "tiles", "tileSize", "zoom"), fixedCoordOffset = (Y.UA.gecko && 1 || 0);

            svgAttrs(this.view.ownerSVGElement, state.dimension);

            empty(this.view);
            Y.Array.each(state.tiles, function (tile) {
                var x = tile.x * state.tileSize, y = tile.y * state.tileSize;
                this.view.appendChild(svg("image", {
                    "x": Math.floor(x - state.origin.x /*+ view.centerX*/),
                    "y": Math.floor(y - state.origin.y /*+ view.centerY*/),
                    "width": state.tileSize + fixedCoordOffset,
                    "height": state.tileSize + fixedCoordOffset
                })).setAttributeNS(XLINK_NS, "href", this.tileSrc(tile.x, tile.y, state.zoom.value));
            }, this);
        }
    });
}, '0.0', {
    requires: ["base", "widget", "plugin", "substitute", "array-extras", "io", "json", "querystring-stringify", "event-key", "svg-utils"]
});