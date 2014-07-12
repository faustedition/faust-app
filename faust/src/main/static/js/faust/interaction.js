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

YUI.add('interaction', function (Y) {

	var tooltip = new Y.Overlay({
		visible: false,
		zIndex: 1300
	}).plug(Y.Plugin.WidgetAnim);
	tooltip.anim.get('animHide').set('duration', 0.6);
	tooltip.anim.get('animShow').set('duration', 0.6);
	tooltip.on('mousedown', function(e) {e.preventDefault();});
	tooltip.on('mousemove', function(e) {e.preventDefault();});

	tooltip.render();

	function showTooltip(info, mouseEvent) {
		//display
		tooltip.setStdModContent('body', info);
		var tooltipWidth = parseFloat(tooltip.get('boundingBox').getComputedStyle('width'));
		var tooltipHeight = parseFloat(tooltip.get('boundingBox').getComputedStyle('height'));
		tooltip.move([mouseEvent.pageX - (tooltipWidth / 2.0), mouseEvent.pageY - tooltipHeight - 30]);
		tooltip.show();
	}

	function hideTooltip(e) {
		tooltip.hide();
	}

	Y.on('faust:mouseover-info', function(e) {
		     showTooltip(e.info, e.mouseEvent);
	});

	Y.on('faust:mouseover-info-hide', function(e) {
		hideTooltip();
	});

}, '0.0', {
	requires: ['overlay', 'widget-anim', 'transition']
});
