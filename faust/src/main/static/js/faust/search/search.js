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
                	uri_parts = Y.Array.map(document.uris, function(uri) {
                		return uri.substring("faust://document/".length);
                	});
                    return Y.Array.map(document.waIds.concat(document.callnumbers.concat(uri_parts)), function(id) {
                        return "<" + id + ">"
                    }).join("; ");
                },
                resultHighlighter:'phraseMatch'
            });
            this.searchAutoComplete.render();
        },
        bindUI:function () {
            this.searchAutoComplete.on("select", this._select, this);
        },
        _select:function (e) {
            e.preventDefault();
            Y.getLocation().href = cp + Y.substitute("/transcript/{id}", e.result.raw);
        }
    }, {});

    Y.mix(Y.namespace("Faust"), { QuickSearch:QuickSearch});
}, '0.0', {
    requires:["base", "widget", "autocomplete", "autocomplete-highlighters", "substitute", "array-extras", "io", "json"]
});