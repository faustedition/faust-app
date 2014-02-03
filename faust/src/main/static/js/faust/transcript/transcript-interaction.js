YUI.add('transcript-interaction', function (Y) {
	Y.on('faust:transcript-layout-done', function() {

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
			under: function(textElement){ return Y.SvgUtils.hasClass(textElement, 'under'); },
			over: function(textElement){ return Y.SvgUtils.hasClass(textElement, 'over'); },
			patch: function(textElement){ return textElement.ancestor('.element-patch'); },
			interline: function(textElement){ return Y.SvgUtils.hasClass(textElement, 'interline');},
			gap: function(textElement){ return Y.SvgUtils.hasClass(textElement, 'element-gap');},
			supplied: function(textElement){ return textElement.ancestor('.element-supplied'); },
			'unclear-cert-high': function(textElement){ return textElement.ancestor('.unclear-cert-high'); },
			'unclear-cert-low': function(textElement){ return textElement.ancestor('.unclear-cert-low'); }
		}

		function decodeClassValue(classValue, key) {
			var start = classValue.indexOf(key);
			if (start < 0) return '';
			var rightSide = classValue.substring(start + key.length);
			var end = rightSide.search('\\s');
			return end >= 0 ? rightSide.substring(0, end) : rightSide;
		}

		function handDisplayContent(classValue) {
			var handValue = decodeClassValue(classValue, 'hand-');
			var materialValue = decodeClassValue(classValue, 'material-');
			var scriptValue = decodeClassValue(classValue, 'script-');


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

			Y.each(Object.keys(propertiesToDisplay), function(key) {
				if (propertiesToDisplay[key](e.target)) {
					var propertyDisplay = Y.Node.create('<div class="tooltip-property"><div><span class="tooltip-caption-property-'
						+ key + '"></span></div></div>');
					content.append(propertyDisplay);
				}
			});

			// display all text decorations such as strikethrough, underline, ...
			Y.each(Y.one(e.target).ancestor('.text-wrapper').all('.text-decoration'), function(decoration){
				var decorationClassValue = decoration.getAttribute('class');
				var decorationType = decodeClassValue(decorationClassValue, 'text-decoration-type-');
				var decorationDisplay = Y.Node.create('<div class="tooltip-decoration"><div><span class="tooltip-caption-decoration-'
					+ decorationType + '"></span></div></div>');
				var decorationHandDisplay = handDisplayContent(decorationClassValue);
				decorationDisplay.append(decorationHandDisplay);
				content.append(decorationDisplay);
			});

			//display

			tooltip.setStdModContent('body', content);
			var tooltipWidth = parseFloat(tooltip.get('boundingBox').getComputedStyle('width'));
			var tooltipHeight = parseFloat(tooltip.get('boundingBox').getComputedStyle('height'));
			tooltip.move([e.pageX - (tooltipWidth / 2.0), e.pageY - tooltipHeight - 10]);
			tooltip.show();
		}

		function hideTooltip(e) {
			tooltip.hide();
		}

		//FIXME: upgrade YUI and use event delegation to prevent memory leaking
		// s. http://yuilibrary.com/projects/yui3/ticket/2532495
		//Y.one('svg.diplomatic').delegate('mouseenter', showTooltip, '.text, .bgBox');
		Y.all('svg.diplomatic .text, svg.diplomatic .bgBox').on('mousemove', showTooltip);
		Y.all('svg.diplomatic .text, svg.diplomatic .bgBox').on('mouseleave', hideTooltip);

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
			Y.all('svg.diplomatic .element-patch').transition('fadeOut');
		});

		patches.on("mouseleave", function () {
			Y.all('svg.diplomatic .element-patch').transition('fadeIn');
		});


	})
}, '0.0', {
	requires: ['event-custom', 'node', 'overlay', 'widget-anim', 'svg-utils']
});
