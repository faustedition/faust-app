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

YUI.add('facsimile-navigation-keyboard', function (Y) {

	var SVG_NS = "http://www.w3.org/2000/svg";

    var FacsimileNavigationKeyboard = Y.Base.create("facsimile-navigation-keyboard", Y.Base, [], {

		navigate: function (model, keyCode) {

			var view = model.get("view");
			var moveX = Math.floor(view.width / 4);
			var moveY = Math.floor(view.height / 4);

			switch (keyCode) {
				case 37:
					// arrow left
					model.pan(-moveX, 0);
					break;
				case 38:
					// arrow up
					model.pan(0, -moveY);
					break;
				case 39:
					// arrow right
					model.pan(moveX, 0);
					break;
				case 40:
					// arrow down
					model.pan(0, moveY);
					break;
				case 48:
					// 0
					model.fitToView();
					model.center();
					break;
				case 67:
					// c
					model.center();
					break;
				case 107:
				case 187:
					// +
					model.zoom(-1);
					return;
				case 109:
				case 189:
					// -
					model.zoom(1);
					return;
			}

		},

		initializer: function (config) {
			var model = config.host.model;
			var navigate = this.navigate;
			this.navigationSub = Y.one("body").on('key', function (e) {
				navigate(model, e.keyCode);
			}, 'down:37,38,39,40,48,67,107,109,187,189');

		},
		destructor: function () {
			this.navigationSub.detach();
		}
	}, {
		NAME: 'facsimileNavigationKeyboard',
		NS: 'navigationKeyboard',
		ATTRS: {
			view: {},
			model: {}
		}
	});

    Y.mix(Y.namespace("Faust"), { FacsimileNavigationKeyboard: FacsimileNavigationKeyboard });

}, '0.0', {
	requires: ['facsimile', 'plugin', 'svg-utils']
});