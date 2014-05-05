YUI.GlobalConfig.groups.faust_test = {
	debug: true,
    base: cp + '../../test/static/js/',
    combine: false,
    filter: "raw",
    root: 'js/',
    modules: {
        'test-adhoc-tree': { path: 'faust/text-annotation/test-adhoc-tree.js'},
		'test-transcript' : { path: 'faust/transcript/test-transcript.js'},
		'test-transcript-view' : { path: 'faust/transcript/test-transcript-view.js'},
		'test-svg-utils' : { path: 'faust/svg-utils/test-svg-utils.js'},

		'test-utils' : { path: 'test-utils.js'}
	}
};