YUI.add('facsimile', function (Y) {

    var NULL_IMAGE_VALUE = {width: Number.MAX_VALUE, height: Number.MAX_VALUE, tileSize: 1},
        NULL_VIEW_VALUE = {x: 0, y: 0, width: 0, height: 0, zoom: 1 },
        MAX_ZOOM = 10;

    function FacsimileViewer(config) {
        FacsimileViewer.superclass.constructor.apply(this, arguments);
    }

    FacsimileViewer.NAME = 'facsimile-viewer';
    FacsimileViewer.ATTRS = {
        image: {
            value: NULL_IMAGE_VALUE,
            validator: function (val) {
                return (val.width >= 0) && (val.height >= 0) && (val.tileSize > 0);
            }
        },
        view: {
            value: NULL_VIEW_VALUE,
            setter: function (val) {
                var image = this.get("image") || NULL_IMAGE_VALUE,
                    view = this.get("view") || NULL_VIEW_VALUE,
                    isNumber = Y.Lang.isNumber;
                return {
                    x: Math.min(Math.max(isNumber(val.x) ? val.x : view.x, 0), image.width),
                    y: Math.min(Math.max(isNumber(val.y) ? val.y : view.y, 0), image.height),
                    width: Math.min(Math.max(isNumber(val.width) ? val.width : view.width, 0), image.width),
                    height: Math.min(Math.max(isNumber(val.height) ? val.height : view.height, 0), image.height),
                    zoom: Math.min(Math.max(isNumber(val.zoom) ? val.zoom : view.zoom, 1), MAX_ZOOM)
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
            var state = this.getAttrs(["image", "view"]);

            // Get the start points for our tiles
            var startx = Math.max(Math.floor(state.view.x / state.image.tileSize), 0);
            var starty = Math.max(Math.floor(state.view.y / state.image.tileSize), 0);

            // If our size is smaller than the display window, only get these tiles!
            var endx = Math.ceil(((Math.min(state.image.width, state.view.width) + state.view.x) / state.image.tileSize) - 1);
            var endy = Math.ceil(((Math.min(state.image.height, state.view.height) + state.view.y) / state.image.tileSize) - 1);

            // Number of tiles is dependent on view width and height
            var xtiles = Math.ceil(state.image.width / state.image.tileSize);
            var ytiles = Math.ceil(state.image.height / state.image.tileSize);

            if (endx >= xtiles) endx = xtiles - 1;
            if (endy >= ytiles) endy = ytiles - 1;

            // Calculate the offset from the tile top left that we want to display.
            var xoffset = Math.floor(state.view.x % state.image.tileSize);
            var yoffset = Math.floor(state.view.y % state.image.tileSize);

            // Center the image if our viewable image is smaller than the window
            if (state.image.width < state.view.width) xoffset -= (state.view.width - state.image.width) / 2;
            if (state.image.height < state.view.height) yoffset -= (state.view.height - state.image.height) / 2;

            var tiles = [];
            for (var j = starty; j <= endy; j++) {
                for (var i = startx; i <= endx; i++) {
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
            var st = this.getAttrs(["image", "view"]);
            this.moveTo(Math.max(st.image.width - st.view.width, 0) / 2, Math.max(st.image.width - st.view.width, 0) / 2);
        },
        moveTo: function (x, y) {
            this.set("view", { x: x, y: y });
        },
        move: function (x, y) {
            var view = this.get("view");
            this.moveTo(view.x + x, view.y + y);
        },
        imageSrc: function () {
            return cp + "/facsimile/gsa/390883/390883_0002";
        },
        tileSrc: function (x, y) {
            return Y.substitute("{imageSrc}?x={x}&y={y}", {imageSrc: this.imageSrc(), x: x, y: y});
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