YUI.add('facsimile', function (Y) {

    var NULL_IMAGE_VALUE = {width: Number.MAX_VALUE, height: Number.MAX_VALUE, tileSize: 1, maxZoom: 0 },
        NULL_VIEW_VALUE = {x: 0, y: 0, width: 0, height: 0, zoom: 0 };

    function FacsimileViewer(config) {
        FacsimileViewer.superclass.constructor.apply(this, arguments);
    }

    FacsimileViewer.NAME = 'facsimile-viewer';
    FacsimileViewer.ATTRS = {
        image: {
            value: NULL_IMAGE_VALUE,
            validator: function (val) {
                return (val.width >= 0) && (val.height >= 0) && (val.maxZoom >= 0) && (val.tileSize > 0);
            }
        },
        view: {
            value: NULL_VIEW_VALUE,
            setter: function (val) {
                var image = this.get("scaled"),
                    view = this.get("view") || NULL_VIEW_VALUE,
                    isNumber = Y.Lang.isNumber;
                return {
                    x: Math.min(Math.max(isNumber(val.x) ? val.x : view.x, 0), image.width),
                    y: Math.min(Math.max(isNumber(val.y) ? val.y : view.y, 0), image.height),
                    width: Math.max(isNumber(val.width) ? val.width : view.width, 0),
                    height: Math.max(isNumber(val.height) ? val.height : view.height, 0)
                };
            }
        },
        zoom: {
            value: 0,
            setter: function (val) {
                return Math.min(Math.max(val || 0, 0), this.get("image").maxZoom);
            }
        },
        scaled: {
            getter: function () {
                var zoom = this.get("zoom"), image = this.get("image"),
                    scale = Math.pow(2, zoom);
                return {
                    width: Math.floor(image.width / scale),
                    height: Math.floor(image.height / scale)
                };
            }
        },
        tiles: {
            valueFn: function () {
                return []
            }
        }
    };

    Y.extend(FacsimileViewer, Y.Widget, {
        initializer: function () {
            this.after("imageChange", this._updateTiles, this);
            this.after("viewChange", this._updateTiles, this);
            this.after("tilesChange", this.syncUI, this);
            this.fetchMetadata();
        },
        _updateTiles: function () {
            var sc = this.get("scaled"), view = this.get("view"), tileSize = this.get("image").tileSize;

            // Get the start points for our tiles
            var startx = Math.max(0, Math.floor(Math.min(sc.width, view.x) / tileSize));
            var starty = Math.max(0, Math.floor(Math.min(sc.height, view.y) / tileSize));

            // If our size is smaller than the display window, only get these tiles!
            var endx = Math.ceil(Math.min(sc.width, view.x + view.width) / tileSize);
            var endy = Math.ceil(Math.min(sc.height, view.y + view.height) / tileSize);

            var tiles = [];
            for (var j = starty - 1; j < endy; j++) {
                for (var i = startx - 1; i < endx; i++) {
                    tiles.push({ x: i, y: j });
                }
            }
            this.setAttrs({tiles: tiles});
        },
        renderUI: function () {
        },
        fetchMetadata: function () {
            Y.io(this.imageSrc(), {
                data: {metadata: true },
                on: { success: Y.bind(this.metadataReceived, this) }
            });
        },
        metadataReceived: function (transactionId, response) {
            this.set("image", Y.merge(this.get("image"), Y.JSON.parse(response.responseText)));
            this.center();
        },
        center: function () {
            var st = this.getAttrs(["scaled", "view"]);
            this.moveTo(Math.max(st.scaled.width - st.view.width, 0) / 2, Math.max(st.scaled.height - st.view.height, 0) / 2);
        },
        moveTo: function (x, y) {
            this.set("view", { x: x, y: y });
        },
        move: function (x, y) {
            var view = this.get("view");
            this.moveTo(view.x + x, view.y + y);
        },
        zoom: function (zoom) {
            var prev = this.get("zoom"), next = Math.max(0, prev + (zoom || 1)), view = this.get("view");
            this.set("zoom", next);
            this.set("view", {
                x: Math.floor(view.x * Math.pow(2, prev) / Math.pow(2, next)),
                y: Math.floor(view.y * Math.pow(2, prev) / Math.pow(2, next))
            });
        },
        imageSrc: function () {
            return cp + "/facsimile/gsa/390883/390883_0002";
        },
        tileSrc: function (x, y) {
            return Y.substitute("{imageSrc}?x={x}&y={y}&zoom={zoom}", {
                imageSrc: this.imageSrc(),
                x: x,
                y: y,
                zoom: this.get("zoom")
            });
        },
        syncUI: function () {
            var contentBox = this.get("contentBox"),
                tileSize = this.get("image").tileSize,
                view = this.get("view"),
                tiles = this.get("tiles");

            contentBox.empty();
            contentBox.setStyles({
                background: "black",
                margin: "1em auto",
                border: 0,
                padding: 0,
                position: "relative",
                left: 0,
                top: 0,
                width: view.width,
                height: view.height,
                overflow: "hidden"
            });

            Y.Array.each(tiles, function (tile) {
                contentBox.append(Y.Node.create(Y.substitute('<img src="{src}" alt="{x}x{y}">', Y.merge(tile, {
                    src: this.tileSrc(tile.x, tile.y)
                }))).setStyles({
                        position: "absolute",
                        left: (tile.x * tileSize) - view.x,
                        top: (tile.y * tileSize) - view.y
                    }));
            }, this);
        }
    });

    Y.mix(Y.namespace("Faust"), { FacsimileViewer: FacsimileViewer });
}, '0.0', {
    requires: ["widget", "substitute", "array-extras", "io", "json"]
});