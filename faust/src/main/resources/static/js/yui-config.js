YUI.GlobalConfig = {
    groups: {
        faust: {
            base: cp + '/static/js/',
            filter: "raw",
            root: 'js/',
            modules: {
                'adhoc-tree': { path: 'faust/text-annotation/adhoc-tree.js'},
                'facsimile': { path: 'faust/facsimile/facsimile.js'},
                'facsimile-svgpane': { path: 'faust/facsimile/facsimile-svgpane.js' },
                'facsimile-highlightpane': { path: 'faust/facsimile/facsimile-highlightpane.js' },
                'util': { path: "faust/util/util.js"},
				'document': { path: "faust/document/document.js" },
				'document-adhoc-tree' : { path: 'faust/document/document-adhoc-tree.js' },
				'document-view' : { path: 'faust/document/document-view.js' },
				'document-yui-view' : { path: 'faust/document/document-yui-view.js' },
				'document-structure-view' : { path: 'faust/document/document-structure-view.js' },
				'document-view-svg' : { path: 'faust/document/document-view-svg.js' },
				'document-controller' : { path: 'faust/document/document-controller.js' },
				'document-ranges' : { path: 'faust/document/document-ranges.js' },
				'document-model' : { path: 'faust/document/document-model.js' },
				'document-text' : { path: 'faust/document/document-text.js' },
				'materialunit' : { path: 'faust/document/materialunit.js' },
                'protovis': { path: "protovis-r3.2.js"},
                'search': { path: "faust/search/search.js"},
				'svg-utils' : { path: "faust/svg-utils/svg-utils.js"},
                'text-annotation': { path: "faust/text-annotation/text-annotation.js"},
                'text-index': { path: 'faust/text-annotation/text-index.js'}
            }
        }
    }
};