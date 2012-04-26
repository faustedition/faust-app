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
                'util': {}
            }
        }
    }
};