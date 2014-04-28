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

YUI.GlobalConfig = {
    debug: false,
    combine: false,
    comboBase: cp + '/resources?',
    root: 'yui3/build/',
    groups: {
        faust: {
            base: cp + '/static/js/',
            combine: false,
            comboBase: cp + '/resources?',
            filter: "raw",
            root: 'js/',
            modules: {
                'adhoc-tree': { path: 'faust/text-annotation/adhoc-tree.js'},
                'facsimile': { path: 'faust/facsimile/facsimile.js'},
                'facsimile-svgpane': { path: 'faust/facsimile/facsimile-svgpane.js' },
				'facsimile-highlightpane': { path: 'faust/facsimile/facsimile-highlightpane.js' },
				'facsimile-navigation-buttons': { path: 'faust/facsimile/facsimile-navigation-buttons.js' },
				'facsimile-navigation-keyboard': { path: 'faust/facsimile/facsimile-navigation-keyboard.js' },
				'facsimile-navigation-mouse': { path: 'faust/facsimile/facsimile-navigation-mouse.js' },
				'facsimile-interaction': { path: 'faust/facsimile/facsimile-interaction.js' },
                'util': { path: "faust/util/util.js"},
				"transcript-adhoc-tree" : { path: 'faust/transcript/transcript-adhoc-tree.js' },
	            'document-app' : { path: 'faust/document/document-app.js' },
				"transcript-configuration-faust" : { path: 'faust/transcript/transcript-configuration-faust.js' },
				"transcript-view" : { path: 'faust/transcript/transcript-view.js' },
				'document-structure-view' : { path: 'faust/document/document-structure-view.js' },
				"transcript-svg" : { path: 'faust/transcript/transcript-svg.js' },
				"transcript" : { path: 'faust/transcript/transcript.js' },
				"transcript-interaction" : { path: 'faust/transcript/transcript-interaction.js' },
				'document-text' : { path: 'faust/document/document-text.js' },
				'materialunit' : { path: 'faust/document/materialunit.js' },
				'protovis': { path: "../lib/protovis-r3.2.js"},
				'search': { path: "faust/search/search.js"},
				'svg-utils' : { path: "faust/svg-utils/svg-utils.js"},
				'text-annotation': { path: "faust/text-annotation/text-annotation.js"},
				'text-display': { path: "faust/text-display/text-display.js"},
				'paged-text-display': { path: 'faust/text-display/paged-text-display.js'},
				'text-index': { path: 'faust/text-annotation/text-index.js'}
			}
		}
	}
};
