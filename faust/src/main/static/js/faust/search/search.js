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

YUI.add('search', function (Y) {

    var QuickSearch = Y.Base.create("quick-search", Y.Widget, [], {
        renderUI:function () {
            this.searchAutoComplete = new Y.AutoComplete({
                inputNode: this.get("contentBox"),
                source: cp + "/search/{query}",
                resultListLocator: "documents",
                resultTextLocator: function(document) {

					if (document.fulltextWindow) {
						return document.fulltextWindow + ' <' + document.callnumber + '>';
					} else {
						var uri_part = document.source.substring("faust://xml/document/".length);
						//var result = "<" + document.idnos + "; " + uri_part + ">";
						var result = "<" + document.idnos + ">";
						return result;
					}
                },
                resultHighlighter:'phraseMatch',
				align: {
					node  : this.get('contentBox'),
					points: ['tr', 'br']
				}
            });
            this.searchAutoComplete.render();
        },
        bindUI:function () {
            this.searchAutoComplete.on("select", this._select, this);
        },
        _select:function (e) {
            e.preventDefault();
			var source = e.result.raw.source;
            Y.getLocation().href = cp + "/document/" + source.slice("faust://xml/document/".length);
        }
    }, {});

    Y.mix(Y.namespace("Faust"), { QuickSearch:QuickSearch});
}, '0.0', {
    requires:["base", "widget", "autocomplete", "autocomplete-highlighters", "substitute", "array-extras", "io", "json"]
});