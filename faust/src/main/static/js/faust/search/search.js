YUI.add('search', function (Y) {

    var QuickSearch = Y.Base.create("quick-search", Y.Widget, [], {
        renderUI:function () {
            this.searchAutoComplete = new Y.AutoComplete({
                inputNode: this.get("contentBox"),
                source: cp + "/search/{query}",
                resultListLocator: "documents",
                resultTextLocator: function(document) {
                    return Y.Array.map(document.waIds.concat(document.callnumbers), function(id) {
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