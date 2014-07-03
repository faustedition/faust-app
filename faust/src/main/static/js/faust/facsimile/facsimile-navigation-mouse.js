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

YUI.add('facsimile-navigation-mouse', function (Y) {

	var SVG_NS = "http://www.w3.org/2000/svg";

    var FacsimileNavigationMouse = Y.Base.create("facsimile-navigation-mouse", Y.Base, [], {

        initializer: function(config) {

			// drag to move

			var isDragging = false;
			var dragStart = {};
			var hasDragged = false;

			var contentBox = config.host.get('contentBox');
			contentBox.on('mousedown', function(e) {
				hasDragged = false;
				isDragging = true;
				dragStart = {x: e.clientX, y: e.clientY};
				e.preventDefault();
			});

			contentBox.on('mousemove', function(e) {
				if (isDragging) {
					hasDragged = true;
					var dx = dragStart.x - e.clientX;
					var dy = dragStart.y - e.clientY;
					dragStart = {x: e.clientX, y: e.clientY};
					config.host.model.pan(dx, dy);
					e.preventDefault();
				}
			});

			contentBox.on('mouseup', function(e) {
				isDragging = false;
			});

			contentBox.on('mouseleave', function(e) {
				isDragging = false;
			});

			// click to zoom in

/*
			contentBox.on('click', function(e) {
				if (!hasDragged) {
					var contentBox = config.host.get('contentBox');

					var contentBoxX = e.pageX - contentBox.getX();
					var contentBoxWidth = parseFloat(contentBox.getComputedStyle('width'));
					var dx = contentBoxX - contentBoxWidth / 2;

					var contentBoxY = e.pageY - contentBox.getY();
					var contentBoxHeight = parseFloat(contentBox.getComputedStyle('height'));
					var dy = contentBoxY - contentBoxHeight / 2;

					config.host.model.pan(dx, dy);
					config.host.model.zoom(-1);
				}
			});
*/
		},
        destructor: function() {
        }
    }, {
		NAME : 'facsimileNavigationMouse',
		NS : 'navigationMouse',
        ATTRS: {
            view: {},
            model: {}
        }
    });

    Y.mix(Y.namespace("Faust"), { FacsimileNavigationMouse: FacsimileNavigationMouse });

}, '0.0', {
	requires: ['facsimile', 'plugin', 'svg-utils', 'node-base', 'event', 'dd-drag']
});