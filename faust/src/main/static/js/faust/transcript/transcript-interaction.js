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

YUI.add('transcript-interaction', function (Y) {
	Y.on('faust:transcript-overlay-done', function() {

		// ******* hand and material display *******
		// tooltip

		var tooltip = new Y.Overlay({
			visible: false,
			zIndex: 1300
		}).plug(Y.Plugin.WidgetAnim);
		tooltip.anim.get('animHide').set('duration', 0.6);
		tooltip.anim.get('animShow').set('duration', 0.6);
		tooltip.render();

		//display text properties or annotations
		var propertiesToDisplay = {
			'under': function(textElement){ return Y.SvgUtils.hasClass(textElement, 'under'); },
			'over': function(textElement){ return Y.SvgUtils.hasClass(textElement, 'over'); },
			'patch': function(textElement){ return textElement.ancestor('.element-patch'); },
			'interline': function(textElement){ return Y.SvgUtils.hasClass(textElement, 'interline');},
			'inbetween': function(textElement){ return Y.SvgUtils.hasClass(textElement, 'inbetween');},
			'gap': function(textElement){ return Y.SvgUtils.hasClass(textElement, 'element-gap');},
			'supplied': function(textElement){ return textElement.ancestor('.element-supplied'); },
			'unclear-cert-high': function(textElement){ return textElement.ancestor('.unclear-cert-high'); },
			'unclear-cert-low': function(textElement){ return textElement.ancestor('.unclear-cert-low'); },
			'used': function(textElement){ return Y.SvgUtils.hasClass(textElement, 'used'); }
		}

		function handDisplayContent(classValue) {
			var handValue = Y.SvgUtils.decodeClassValue(classValue, 'hand-');
			var materialValue = Y.SvgUtils.decodeClassValue(classValue, 'material-');
			var scriptValue = Y.SvgUtils.decodeClassValue(classValue, 'script-');


			var content = '<div>'
				+ '<span class="tooltip-hand"><span class="tooltip-caption-hand-' + handValue + '"></span></span>'
				+ '<span class="tooltip-material"><span class="tooltip-caption-material-' + materialValue + '"></span></span>'
				+ '<span class="tooltip-script"><span class="tooltip-caption-script-' + scriptValue + '"></span></span>'
				+ '</div>';
			if (handValue || materialValue || scriptValue) {
				return Y.Node.create(content);
			} else {
				return [];
			}

		};



		function showTooltip(e) {

			// display the hand, material and script of the text
			var textClassValue = e.target.getAttribute('class');
			var content = Y.Node.create('<div></div>');
			var textHandDisplay = handDisplayContent(textClassValue);
			content.append(textHandDisplay);

			// display all text decorations such as strikethrough, underline, ...
			Y.each(Y.one(e.target).ancestor('.text-wrapper').all('.text-decoration'), function(decoration){
				var decorationClassValue = decoration.getAttribute('class');
				var decorationType = Y.SvgUtils.decodeClassValue(decorationClassValue, 'text-decoration-type-');
				var decorationDisplay = Y.Node.create('<div class="tooltip-decoration"><div><span class="tooltip-caption-decoration-'
					+ decorationType + '"></span></div></div>');
				var decorationHandDisplay = handDisplayContent(decorationClassValue);
				decorationDisplay.append(decorationHandDisplay);
				content.append(decorationDisplay);
			});

			// display all inline decorations such as rect, circle, ...
			Y.each(Y.one(e.target).ancestors('.inline-decoration'), function(decoration){
				var decorationClassValue = decoration.getAttribute('class');
				var decorationType = Y.SvgUtils.decodeClassValue(decorationClassValue, 'inline-decoration-type-');
				var decorationDisplay = Y.Node.create('<div class="tooltip-decoration"><div><span class="tooltip-caption-decoration-'
					+ decorationType + '"></span></div></div>');
				var decorationHandDisplay = handDisplayContent(decorationClassValue);
				decorationDisplay.append(decorationHandDisplay);
				content.append(decorationDisplay);
			});

			// display various properties
			Y.each(Object.keys(propertiesToDisplay), function(key) {
				if (propertiesToDisplay[key](e.target)) {
					var propertyDisplay = Y.Node.create('<div class="tooltip-property"><div><span class="tooltip-caption-property-'
						+ key + '"></span></div></div>');
					content.append(propertyDisplay);
				}
			});


			//display

			tooltip.setStdModContent('body', content);
			var tooltipWidth = parseFloat(tooltip.get('boundingBox').getComputedStyle('width'));
			var tooltipHeight = parseFloat(tooltip.get('boundingBox').getComputedStyle('height'));
			tooltip.move([e.pageX - (tooltipWidth / 2.0), e.pageY - tooltipHeight - 30]);
			tooltip.show();
		}

		function hideTooltip(e) {
			tooltip.hide();
		}

		//FIXME: upgrade YUI and use event delegation to prevent memory leaking
		// s. http://yuilibrary.com/projects/yui3/ticket/2532495
		//Y.one('svg.diplomatic').delegate('mouseenter', showTooltip, '.text, .bgBox');
		var allTextElements = Y.all('svg.diplomatic .text, svg.diplomatic .bgBox');

		allTextElements.on('mousemove', function(e) {
			showTooltip(e);
			var containingLine = Y.one(e.target).ancestors('.element-line').item(0);
			var lineClassValue = containingLine.getAttribute('class');
			var lineNumber = Y.SvgUtils.decodeClassValue(lineClassValue, 'lineNumber');
			Y.fire('faust:examine-line', { lineNumber: lineNumber});
		});

		allTextElements.on('mouseleave', function(e) {
			hideTooltip(e);
			var containingLine = Y.one(e.target).ancestors('.element-line').item(0);
			var lineClassValue = containingLine.getAttribute('class');
			var lineNumber = Y.SvgUtils.decodeClassValue(lineClassValue, 'lineNumber');
			Y.fire('faust:stop-examine-line', { lineNumber: lineNumber});

		});

		Y.on('faust:examine-line', function(e) {
			var bgBoxes = Y.all('svg.diplomatic .element-line.lineNumber' + e.lineNumber + ' .bgBox');
			Y.each(bgBoxes, function(bgBox) {
				var domNode = bgBox.getDOMNode();
				Y.SvgUtils.addClass(bgBox, 'highlight');

			});
			bgBoxes.transition('fadeIn');

		});

		Y.on('faust:stop-examine-line', function(e) {
			var bgBoxes = Y.all('svg.diplomatic .element-line.lineNumber' + e.lineNumber + ' .bgBox');
			Y.each(bgBoxes, function(bgBox) {
				var domNode = bgBox.getDOMNode();
				Y.SvgUtils.removeClass(bgBox, 'highlight');

			});
			bgBoxes.transition('fadeOut');
		});


		// highlight hands

		var sheet = Y.StyleSheet('#style-document-transcript-highlight-hands');
		var zones = Y.all('svg.diplomatic .element-zone');

		zones.on("mouseenter", function () {
			Y.all('svg.diplomatic .bgBox').transition('fadeIn');
			if (sheet) sheet.enable();
		});

		zones.on("mouseleave", function () {
			Y.all('svg.diplomatic .bgBox').transition('fadeOut');
			//sheet.disable();
		});

		// ******* patches *******

		var patches = Y.all('svg.diplomatic .element-patch');

		patches.on("mouseenter", function (e) {
			Y.all('svg.diplomatic .element-patch').transition({
				duration: 1,
				opacity: 0.2
			});
		});

		patches.on("mouseleave", function () {
			Y.all('svg.diplomatic .element-patch').transition({
				duration: 1,
				opacity: 1
			});
		});



	})
}, '0.0', {
	requires: ['event-custom', 'node', 'overlay', 'widget-anim', 'transition', 'svg-utils']
});
