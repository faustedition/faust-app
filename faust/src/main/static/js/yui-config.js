YUI.GlobalConfig = {
    debug: true,
    combine: true,
    comboBase: cp + '/resources?',
    root: 'yui3/build/',
    groups: {
        faust: {
            base: cp + '/js/faust/',
            combine: true,
            comboBase: cp + '/resources?',
            filter: "raw",
            root: 'js/faust/',
            modules: {
                'facsimile': {},
                'facsimile-svgpane': { path: 'facsimile/facsimile-svgpane.js' },
                'facsimile-highlightpane': { path: 'facsimile/facsimile-highlightpane.js' },
                'util': {},
				'document': {},
				'document-view' : { path: 'document/document-view.js' },
				'document-view-svg' : { path: 'document/document-view-svg.js' },
				'document-controller' : { path: 'document/document-controller.js' },
				'document-model' : { path: 'document/document-model.js' },
				'materialunit' : { path: 'document/materialunit.js' },
				'svg-utils' : {}
            }
        }
    }
};