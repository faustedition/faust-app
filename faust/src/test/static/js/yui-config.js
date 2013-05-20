YUI.GlobalConfig.groups.faust_test = {
    base: cp + '../../test/static/js/',
    combine: false,
    filter: "raw",
    root: 'js/',
    modules: {
        'test-adhoc-tree': { path: 'faust/text-annotation/adhoc-tree-test.js'},
    }
};