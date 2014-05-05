YUI({ 
	logInclude: { TestRunner: true }
	//root: '../../../main/static/yui3/build/',
}).use('test-console', 'test', 
	   'test-adhoc-tree', 'test-transcript', 'test-transcript-view', 'test-svg-utils', 'transcript-view',
	   function (Y) {

		   Y.Test.Runner.add(Y.FaustTest.adhocTreeTestSuite);
		   Y.Test.Runner.add(Y.FaustTest.transcriptTestSuite);
		   Y.Test.Runner.add(Y.FaustTest.transcriptViewTestSuite);
		   Y.Test.Runner.add(Y.FaustTest.svgUtilsTestSuite);

		   new Y.Test.Console({
			   filters: {
				   pass: true
			   }
		   }).render('#test_console');

		   Y.one('#run').on('click', function(e){
			   Y.Test.Runner.run();
		   });

	   });
