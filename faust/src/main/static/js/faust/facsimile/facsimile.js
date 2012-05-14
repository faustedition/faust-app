YUI.add('facsimile', function (Y) {

    var SVG_NS = "http://www.w3.org/2000/svg",
        XLINK_NS = "http://www.w3.org/1999/xlink",
        NULL_IMAGE_VALUE = { width: Number.MAX_VALUE, height: Number.MAX_VALUE, tileSize: 1, maxZoom: 0 },
        NULL_VIEW_VALUE = { x: 0, y: 0, width: 0, height: 0, zoom: 0 },
        NULL_HIGHLIGHT_VALUE = { x: 0, y: 0, width: 0, height: 0 }

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

    function svg(element, attrs, styles) {
        return svgStyles(svgAttrs(svgElement(element), attrs || {}), styles || {});
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
            var view = this.get("view");
            this.set("view", {
                x: x || 0,
                y: y || 0
            });
        },
        pan: function (x, y) {
            var view = this.get("view");
            this.panTo(
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
        fittingZoom: function(width, height) {
            var image = this.get("image"), view = this.get("view");
            return Math.round(Math.log(Math.max(((width || image.width) / view.width), ((height || image.height) / view.height))) / Math.log(2));
        },
        fitToView: function (width, height) {
            this.zoom(this.fittingZoom(width, height) - this.get("zoom"));
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
                        view = this.get("view") || NULL_VIEW_VALUE,
                        scale = qscale(-this.get("zoom")),
                        isNumber = Y.Lang.isNumber,
                        width = Math.max(isNumber(val.width) ? val.width : view.width, 0),
                        height = Math.max(isNumber(val.height) ? val.height : view.height, 0),
                        imageWidth = scale(image.width),
                        imageHeight = scale(image.height);

                    return {
                        x: Math.min(Math.max(isNumber(val.x) ? val.x : view.x, 0), Math.max(0, imageWidth - view.width)),
                        y: Math.min(Math.max(isNumber(val.y) ? val.y : view.y, 0), Math.max(0, imageHeight - view.height)),
                        centerX: Math.floor(Math.max(0, (view.width - imageWidth) / 2)),
                        centerY: Math.floor(Math.max(0, (view.height - imageHeight) / 2)),
                        width: width,
                        height: height,
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

    var HighlightPane = Y.Base.create("hightlight-pane", Y.Base, [], {
        initializer: function() {
            var view = this.get("view"),
                svgRoot = view.ownerSVGElement,
                defs = svgRoot.getElementsByTagNameNS(SVG_NS, "defs"),
                model = this.get("model");

            this.mask = (defs.length ? defs[0] : svgRoot.appendChild(svg("defs"))).appendChild(svg("mask", {
                id: "highlight",
                x: "0",
                y: "0",
                width: "100%",
                height: "100%"
            }));
            this.mask.appendChild(svg("rect", {
                x: 0,
                y: 0,
                width: "100%",
                height: "100%"
            }, {
                fill: "#999"
            }));
            this.highlightRect = this.mask.appendChild(svg("rect", {
                x: 0,
                y: 0,
                width: 0,
                height: 0,
                rx: 0,
                ry: 0
            }, {
                fill: "#fff",
                stroke: "#fff",
                strokeWidth: "10"
            }));

            this.modelChangeSub = model.after(["tilesChange", "viewChange", "imageChange"], this.highlight, this);
            this.highlightChangeSub = this.after("highlightChange", this.highlight, this);
        },
        destructor: function() {
            this.highlightChangeSub.detach();
            this.modelChangeSub.detach();

            this.mask.parentNode.removeChild(this.mask);
        },
        highlight: function() {
            var highlight = this.get("highlight"), model = this.get("model"),
                scale = qscale(-model.get("zoom")),
                x = Math.floor(scale(highlight.x)),
                y = Math.floor(scale(highlight.y)),
                width = Math.floor(scale(highlight.width)),
                height = Math.floor(scale(highlight.height)),
                view = model.get("view");

            svgStyles(this.get("view"), {
                mask: ((width && height) ? "url(#highlight)" : "none")
            });

            svgAttrs(this.highlightRect, {
                x: x - view.x + view.centerX,
                y: y - view.y + view.centerY,
                rx: Math.floor(width / 100),
                ry: Math.floor(height / 100),
                width: width,
                height: height
            });
        }
    }, {
        ATTRS: {
            view: {},
            model: {},
            highlight: {
                value: NULL_HIGHLIGHT_VALUE
            }
        }
    });

    var SvgPane = Y.Base.create("svg-pane", Y.Base, [], {

        loadSvg : function(svgSrc){
			var that = this;
			if (svgSrc) {
				Y.io(svgSrc, {
					method: "GET",
					xdr: { responseXML: false },
					headers: { "Accept": "image/svg" },
					on: {
						success: function(id, o, a) {
							// FIXME this is a silly hack, use a different library
							that.svgContainer.innerHTML = o.responseText;
						}, 
						failure: function(id, o, a) { 
							Y.log("ERROR " + id + " " + a, "info", "Faust") }
					}
				});
			}
		},
		adjustTransform: function() {
			var svgContainer = this.svgContainer;
			var createTransform = function(){
				return svgContainer.viewportElement.createSVGTransform();
			};

			var transforms = this.svgContainer.transform.baseVal;
			transforms.clear();
			var view = this.get("model").get("view");
			var image = this.get("model").get("image");

			// Set position
			var translateTransform = createTransform();
			translateTransform.setTranslate(-view.x, -view.y);
			transforms.appendItem(translateTransform);
			
			// Set scale
			var scale = view.imageHeight / image.height;
			var zoomTransform = createTransform();
			zoomTransform.setScale(scale, scale);
			transforms.appendItem(zoomTransform);


		},
		initializer: function(config) {
            var svgRoot = this.get("view").ownerSVGElement;
            this.svgContainer = svg("g", {
                id: "svgpane",
                x: "0",
                y: "0",
			});
			svgRoot.appendChild(this.svgContainer);
			this.modelChangeSub = this.get("model").after(["tilesChange", "viewChange", "imageChange"], this.adjustTransform, this);
			this.loadSvg(config.svgSrc);
        },
        // destructor: function() {
        // },
            }, {
        ATTRS: {
            view: {},
            model: {},
        }
    });


    var FacsimileViewer = Y.Base.create("facsimile-viewer", Y.Widget, [], {
        initializer: function (config) {
            var view = config.view || NULL_VIEW_VALUE;

            var svgRoot = svg("svg", {
                "version": "1.1",
                "width": view.width,
                "height": view.height
            }, {
                background: "black",
                margin: "1em auto",
                border: 0,
                padding: 0,
                position: "relative",
                overflow: "hidden"
            });
            var contentBox = this.get("contentBox");
            this.view = contentBox.getDOMNode().appendChild(svgRoot).appendChild(svg("g"));

            this.navigationSub = Y.one("body").on('key', Y.bind(this.navigate, this), 'down:37,38,39,40,48,67,107,109,187,189');

            this.model = new TiledViewModel({ view: view });
            this.modelChangeSub = this.model.after(["tilesChange", "viewChange", "imageChange"], this.syncUI, this);
			this.src = config.src;
            this.highlightPane = new HighlightPane({ view: this.view, model: this.model });
            this.svgPane = new SvgPane({ svgSrc: config.svgSrc, view: this.view, model: this.model });

            Y.io(this.imageSrc(), {
                data: {metadata: true },
                on: { success: Y.bind(this.metadataReceived, this) }
            });
        },
        destructor: function () {
            this.navigationSub.detach();
            this.modelChangeSub.detach();
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
            this.highlightPane.set("highlight", NULL_HIGHLIGHT_VALUE);
        },
        highlight: function(area) {
            this.model.zoom(this.model.fittingZoom(area.width, area.height) + 1 - this.model.get("zoom"));
            var scale = qscale(-this.model.get("zoom"));
            this.model.centerOn(scale(area.x + Math.floor(area.width / 2)), scale(area.y + Math.floor(area.height / 2)));
            this.highlightPane.set("highlight", area);
        },
        metadataReceived: function (transactionId, response) {
            this.model.set("image", Y.merge(this.get("image"), Y.JSON.parse(response.responseText)));
            this.model.fitToView();
            this.model.center();
        },
        imageSrc: function () {
			return cp + this.src;
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
            var view = this.model.get("view"),
                tileSize = this.model.get("image").tileSize,
                tiles = this.model.get("tiles"),
                zoom = this.model.get("zoom"),
                fixedCoordOffset = (Y.UA.gecko && 1 || 0);

            svgAttrs(this.view.ownerSVGElement, {
                "width": view.width,
                "height": view.height
            });

            empty(this.view);
            Y.Array.each(tiles, function (tile) {
                var x = tile.x * tileSize, y = tile.y * tileSize;
                this.view.appendChild(svg("image", {
                    "x": Math.floor(x - view.x /*+ view.centerX*/),
                    "y": Math.floor(y - view.y /*+ view.centerY*/),
                    "width": Math.min(tileSize, view.imageWidth - x) + fixedCoordOffset,
                    "height": Math.min(tileSize, view.imageHeight - y) + fixedCoordOffset
                })).setAttributeNS(XLINK_NS, "href", this.tileSrc(tile.x, tile.y, zoom));
            }, this);
        }
    });

    Y.mix(Y.namespace("Faust"), { FacsimileViewer: FacsimileViewer });
}, '0.0', {
    requires: ["base", "widget", "substitute", "array-extras", "io", "json", "event-key"]
});