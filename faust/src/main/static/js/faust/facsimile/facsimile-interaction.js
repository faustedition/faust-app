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

YUI.add('facsimile-interaction', function (Y) {

	var XLINK_NS = 'http://www.w3.org/1999/xlink';
	var LINKEDTO_PREFIX = 'linkedto-';

	function hoverLineHighlight(e) {
		//e.target.transition({	duration: 0, opacity: 1});
		var highlightClassValue = e.target.getAttribute('class');
		var lineNumber = Y.SvgUtils.decodeClassValue(highlightClassValue, 'linkedto-lineNumber');
		Y.fire('faust:examine-line', { lineNumber: lineNumber});
	}

	function stopHoverLineHighlight(e) {
		//e.target.transition({	duration: 0, opacity: 0});
		var highlightClassValue = e.target.getAttribute('class');
		var lineNumber = Y.SvgUtils.decodeClassValue(highlightClassValue, 'linkedto-lineNumber');
		Y.fire('faust:stop-examine-line', { lineNumber: lineNumber});
	}

	Y.on('faust:facsimile-svg-pane-loaded', function() {
		var allLineHighlights = Y.all('#facsimile-view #svgpane .imageannotationLine.imageannotationLinked');
		allLineHighlights.on('mouseenter', hoverLineHighlight);
		allLineHighlights.on('mouseleave', stopHoverLineHighlight);
		Y.each(allLineHighlights, function(svgElement){
			// prepare the text-image-overlay
			var domNode = svgElement.getDOMNode();
			domNode.setAttribute('fill', '#ffc20e');
			domNode.setAttribute('stroke', 'none');
			domNode.setAttribute('opacity', '0');
			domNode.setAttribute('fill-opacity', '0.4');

			// for better handling, increase the height of the highlight boxes

			var heightFactor = 2;
			var height = domNode.getAttribute('height');
			domNode.setAttribute('height', height * heightFactor);
			var y = domNode.getAttribute('y');
			domNode.setAttribute('y', y - ((heightFactor * height) - height));




			var linkedLine = domNode.getAttributeNS(XLINK_NS, 'href');
			var classVal = LINKEDTO_PREFIX + linkedLine.substring(1);
			domNode.setAttribute('class', domNode.getAttribute('class') + ' ' + classVal);
		});

		// ******* button to hide/show facsimile *******


		Y.mix(Y.namespace("Faust"), { showingTranscriptOverlay: true });

		var showHideButton = Y.Node.create('<button class="pure-button button-opaque svgpane-show-hide"' +
			' style="position: absolute; bottom: 2em; left: 1em;"></button>');
		showHideButton.append('<i class="icon-font"></i> transcription visible');
		showHideButton.on("click", function() {
			var allLines = Y.all('.svgpane-transcript-overlay svg.diplomatic .element-line');
			if (Y.Faust.showingTranscriptOverlay) {
				allLines.transition('fadeOut');
				showHideButton.empty().append('<i class="icon-font"></i> transcription hidden');
				Y.Faust.showingTranscriptOverlay = false;

			} else {
				allLines.transition('fadeIn');
				showHideButton.empty().append('<i class="icon-font"></i> transcription visible');
				Y.Faust.showingTranscriptOverlay = true;
			}

		});

		Y.one('#facsimile-view').append(showHideButton);


		// ******* transcript overlay on facsimile *******

		Y.on('faust:examine-line', function(e) {
			var transcriptLine = Y.all('.svgpane-transcript-overlay svg.diplomatic .element-line.lineNumber' + e.lineNumber);
			transcriptLine.transition('fadeIn');

		});

		Y.on('faust:stop-examine-line', function(e) {
			var transcriptLine = Y.all('.svgpane-transcript-overlay svg.diplomatic .element-line.lineNumber' + e.lineNumber);
			if (!Y.Faust.showingTranscriptOverlay) {
				transcriptLine.transition('fadeOut');
			}

		});



		// ******* text-image-links *******
		Y.on('faust:examine-line', function(e) {
			Y.all('#facsimile-view #svgpane .linkedto-lineNumber' + e.lineNumber).transition({	duration: 1, opacity: 1});
		});

		Y.on('faust:stop-examine-line', function(e) {
			Y.all('#facsimile-view #svgpane .linkedto-lineNumber' + e.lineNumber).transition({	duration: 1, opacity: 0});

		});

	})
}, '0.0', {
	requires: ['event-custom', 'node', 'svg-utils']
});
