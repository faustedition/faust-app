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

YUI.add('facsimile-svgpane', function (Y) {

	var svg = Y.SvgUtils.svg;

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
							Y.fire('faust:facsimile-svg-pane-loaded', {});

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
			var view = this.host.model.get("view");
			var image = this.host.model.get("image");

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
			this.host = config.host;
            var svgRoot = this.host.view.ownerSVGElement;
            this.svgContainer = svg("g", {
                id: "svgpane",
                x: "0",
                y: "0",
			});
			svgRoot.appendChild(this.svgContainer);
			this.modelChangeSub = this.host.model.after(["tilesChange", "viewChange", "imageChange"], this.adjustTransform, this);
			this.loadSvg(config.svgSrc);
        },
        destructor: function() {
			this.modelChangeSub.detach();
        },
    }, {
		NAME : 'svgPane',
		NS : 'svg',
        ATTRS: {
            view: {},
            model: {},
        }
    });

    Y.mix(Y.namespace("Faust"), { SvgPane: SvgPane });

}, '0.0', {
	requires: ['facsimile', 'plugin', 'svg-utils']
});