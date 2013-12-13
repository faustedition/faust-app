YUI.add('transcript-interaction', function (Y) {
	Y.on('faust:transcript-layout-done', function() {

		// TOOLTIP

		var tooltip = new Y.Overlay({
			visible: false,
			zIndex: 1300
		}).plug(Y.Plugin.WidgetAnim);
		tooltip.anim.get('animHide').set('duration', 0.6);
		tooltip.anim.get('animShow').set('duration', 0.6);
		tooltip.render();

		function decodeClassValue(classValue, key) {
			var start = classValue.indexOf(key);
			if (start < 0) return '';
			var rightSide = classValue.substring(start + key.length);
			var end = rightSide.search('\\s');
			return end >= 0 ? rightSide.substring(0, end) : rightSide;
		}

		function showTooltip(e) {
			var classValue = e.target.getAttribute('class');
			var handValue = decodeClassValue(classValue, 'hand-');
			var materialValue = decodeClassValue(classValue, 'material-');
			var scriptValue = decodeClassValue(classValue, 'script-');
			var content = '<div class="tooltip-hand"><span class="tooltip-caption-hand-' + handValue + '"></span></div>'
				+ '<div class="tooltip-material"><span class="tooltip-caption-material-' + materialValue + '"></span></div>'
				+ '<div class="tooltip-script"><span class="tooltip-caption-script-' + scriptValue + '"></span></div>'
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
		var zones = Y.all('svg.diplomatic .Zone');

		zones.on("mouseenter", function () {
			Y.all('.bgBox').transition('fadeIn');
			if (sheet) sheet.enable();
		});

		zones.on("mouseleave", function () {
			Y.all('.bgBox').transition('fadeOut');
			//sheet.disable();
		});



	})
}, '0.0', {
	requires: ['event-custom', 'node', 'overlay', 'widget-anim']
});
