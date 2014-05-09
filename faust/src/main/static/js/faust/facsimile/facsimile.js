/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

YUI.add('facsimile', function (Y) {

    var SVG_NS = "http://www.w3.org/2000/svg",
    XLINK_NS = "http://www.w3.org/1999/xlink",
    NULL_IMAGE_VALUE = { width: Number.MAX_VALUE, height: Number.MAX_VALUE, tileSize: 1, maxZoom: 0 },
    NULL_VIEW_VALUE = { x: 0, y: 0, width: 0, height: 0, zoom: 0 };

	var svg = Y.SvgUtils.svg,
	qscale = Y.SvgUtils.qscale,
	svgAttrs = Y.SvgUtils.svgAttrs,
	empty = Y.SvgUtils.empty;
	
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
		centerTop: function () {
			var view = this.get("view");
			this.panTo(
				Math.max(view.imageWidth - view.width, 0) / 2,
				-100
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
        },
		fitWidthToView: function (width) {
			this.zoom(this.fittingZoom(width, 0.0001) - this.get("zoom"));
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

    var FacsimileViewer = Y.Base.create("facsimile-viewer", Y.Widget, [], {
        initializer: function (config) {
            var view = config.view || NULL_VIEW_VALUE;
			var svgRoot = svg("svg", {
                "version": "1.1",
                "width": view.width,
                "height": view.height
            }, {
                background: "white",
                margin: "0em auto",
                border: 0,
                padding: 0,
                position: "relative",
                overflow: "hidden"
            });
            var contentBox = this.get("contentBox");
            this.view = contentBox.getDOMNode().appendChild(svgRoot).appendChild(svg("g"));

            this.model = new TiledViewModel({ view: view });
            this.modelChangeSub = this.model.after(["tilesChange", "viewChange", "imageChange"], this.syncUI, this);
			this.src = config.src;
            //this.highlightPane = new HighlightPane({ view: this.view, model: this.model });
            //this.svgPane = new SvgPane({ svgSrc: config.svgSrc, view: this.view, model: this.model });

            Y.io(this.imageSrc(), {
                data: {metadata: true },
                on: { success: Y.bind(this.metadataReceived, this) }
            });
        },
        destructor: function () {
            this.modelChangeSub.detach();
        },
        metadataReceived: function (transactionId, response) {
            this.model.set("image", Y.merge(this.get("image"), Y.JSON.parse(response.responseText)));
            this.model.fitWidthToView();
            this.model.centerTop();
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

		fitViewToSrcNode: function() {
			var srcNode = this.get('srcNode');
			var srcNodeWidth = parseFloat(srcNode.getComputedStyle('width'));
			var srcNodeHeight = parseFloat(srcNode.getComputedStyle('height'));
			var view = this.model.get('view');
			view.width = srcNodeWidth;
			view.height = srcNodeHeight;
			this.model.set('view', view);
			this.syncUI();
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
    requires: ["base", "widget", "substitute", "array-extras", "io", "json", "event-key",
			   "svg-utils"]
});