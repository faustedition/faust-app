YUI({ 
	logInclude: { TestRunner: true },
	//root: '../../../main/static/yui3/build/',
}).use('test-console', 'test', 
	   'test-adhoc-tree',
	   function (Y) {

		   Y.Test.Runner.add(Y.FaustTest.AdhocTreeTest);



		   new Y.Test.Console({
			   filters: {
				   pass: true
			   }
		   }).render('#test_console');

		   Y.one('#run').on('click', function(e){
			   Y.Test.Runner.run();
		   });

	   });
