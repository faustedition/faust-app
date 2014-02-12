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

YUI.add('facsimile-highlightpane', function (Y) {

	var NULL_HIGHLIGHT_VALUE = { x: 0, y: 0, width: 0, height: 0 },
	SVG_NS = "http://www.w3.org/2000/svg";

	var svg = Y.SvgUtils.svg,
	qscale = Y.SvgUtils.qscale,
	svgAttrs = Y.SvgUtils.svgAttrs,
	svgStyles = Y.SvgUtils.svgStyles,
	empty = Y.SvgUtils.empty;

    var HighlightPane = Y.Base.create("hightlight-pane", Y.Base, [], {

        initializer: function(config) {
			this.host = config.host;
            var view = this.host.view, //get("view"),
            svgRoot = view.ownerSVGElement,
            defs = svgRoot.getElementsByTagNameNS(SVG_NS, "defs"),
            model = this.host.model; //.get("model");

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

			this.set("highlight", NULL_HIGHLIGHT_VALUE);
        },
        destructor: function() {
            this.highlightChangeSub.detach();
            this.modelChangeSub.detach();

            this.mask.parentNode.removeChild(this.mask);
        },
        highlight: function() {
            var highlight = this.get("highlight"), model = this.host.model, //get("model"),
            scale = qscale(-model.get("zoom")),
            x = Math.floor(scale(highlight.x)),
            y = Math.floor(scale(highlight.y)),
            width = Math.floor(scale(highlight.width)),
            height = Math.floor(scale(highlight.height)),
            view = model.get("view");

            svgStyles(this.host.view, {
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
        },
        highlightArea: function(area) {
            this.host.model.zoom(this.host.model.fittingZoom(area.width, area.height) + 1 - this.host.model.get("zoom"));
            var scale = qscale(-this.host.model.get("zoom"));
            this.host.model.centerOn(scale(area.x + Math.floor(area.width / 2)), scale(area.y + Math.floor(area.height / 2)));
            this.set("highlight", area);
        }
    }, {
		NAME : 'highlightPane',
		NS : 'highlight',
        ATTRS: {
            view: {},
            model: {},
            highlight: {
                value: NULL_HIGHLIGHT_VALUE
            }
        }
    });

    Y.mix(Y.namespace("Faust"), { HighlightPane: HighlightPane });

}, '0.0', {
	requires: ['facsimile', 'plugin', 'svg-utils']
});