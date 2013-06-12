YUI.add('test-adhoc-tree', function (Y) {

	var adhocTreeTestSuite = new Y.Test.Suite('AdhocTree Test Suite');

	function structurallyEqual(node1, node2) {
		if (node1 instanceof Y.Faust.TextNode || Y.Lang.isString(node1)) {
			var text1 = node1 instanceof Y.Faust.TextNode ? node1.content() : node1;
			var text2 = node2 instanceof Y.Faust.TextNode ? node2.content() : node2;
			return text1 === text2;
		}

		var name1 = node1.annotation ? node1.annotation.name.localName : node1.name.localName;
		var name2 = node2.annotation ? node2.annotation.name.localName : node2.name.localName;
		if (name1 !== name2)
			return false;

		var children1 = Y.Lang.isFunction(node1.children) ? node1.children() : node1.children;
		var children2 = Y.Lang.isFunction(node2.children) ? node2.children() : node2.children;
		if (children1.length !== children2.length)
			return false;
		for (var i=0; i < children1.length; i++) {
			if (!structurallyEqual(children1[i], children2[i]))
				return false;
		}
		return true;
	};

	function a(nodeData) {
		return { data: { 'xml:node': nodeData } }
	};

	var isDescendantTest = new Y.Test.Case({

		name: 'Test for isDecendant()',
		
		testIsDescendant : function() {
			Y.assert(Y.Faust.XMLNodeUtils.isDescendant( a('1/1'), a('1')), 'child 1');
			Y.assert(Y.Faust.XMLNodeUtils.isDescendant( a('2/1'), a('1')), 'child 2');
			Y.assert(Y.Faust.XMLNodeUtils.isDescendant( a('2/2/1'), a('1')), 'grandchild');
			Y.assert(!Y.Faust.XMLNodeUtils.isDescendant( a('2'), a('1')), 'sibling');
			Y.assert(!Y.Faust.XMLNodeUtils.isDescendant( a('1'), a('1/1')), 'parent');
		}

	});
	
	adhocTreeTestSuite.add(isDescendantTest);										  

	var documentOrderSortTest = new Y.Test.Case({

		name: 'Test for documentOrderSort()',
		
		testDocumentOrderSort : function() {

			Y.assert(Y.Faust.XMLNodeUtils.documentOrderSort( a('1'), a('2')) < 0, 'x is following sibling of y');
			Y.assert(Y.Faust.XMLNodeUtils.documentOrderSort( a('2'), a('1')) > 0, 'y is following sibling of x');
			Y.assert(Y.Faust.XMLNodeUtils.documentOrderSort( a('2/1'), a('1')) > 0, 'x is child of y');
			Y.assert(Y.Faust.XMLNodeUtils.documentOrderSort( a('1'), a('2/1')) < 0, 'y is child of x');
			Y.assert(Y.Faust.XMLNodeUtils.documentOrderSort( a('3/2/1'), a('1')) > 0, 'x is descendant of y');

			Y.assert(Y.Faust.XMLNodeUtils.documentOrderSort( a('100/4/2/6/2'), a('98/4/2/6/2')) > 0, 'real world example 1');
			Y.assert(Y.Faust.XMLNodeUtils.documentOrderSort( a('98/4/2/6/2'), a('100/4/2/6/2')) < 0, 'real world example 2');
			Y.assert(Y.Faust.XMLNodeUtils.documentOrderSort( a('2/98/4/2/6/2'), a('100/4/2/6/2')) < 0, 'real world example 3');

		}

	});
	
	adhocTreeTestSuite.add(documentOrderSortTest);										  


	var basicTest = new Y.Test.Case({

		name: 'Basic Adhoc Tree',
		
		text: Y.Faust.Text.create(
			{
				'annotations': [
					{
						'd': {
							'xml:node': '1',
						}, 
						'id': 1, 
						'n': 1, 
						't': [
							[
								1, 
								24, 
								1000
							]
						]
					}, 
					{
						'd': {
							'xml:node': '1/1'
						}, 
						'id': 2, 
						'n': 2, 
						't': [
							[
								3, 
								6, 
								1000
							]
						]
					}, 
					{
						'd': {
							'xml:node': '2/1'
						}, 
						'id': 3, 
						'n': 3, 
						't': [
							[
								20, 
								23, 
								1000
							]
						]
					}, 

				], 

				'names': {
 					'1': [
						'http://interedition.eu/ns', 
						'element_a'
					], 
					'2': [
						'http://interedition.eu/ns', 
						'element_b'
					], 
					'3': [
						'http://interedition.eu/ns', 
						'element_c'
					], 
					'100': [
						'http://interedition.eu/ns', 
						'text'
					], 

				}, 
				'text': {
					'd': {},
					'id': 1000, 
					'n': 100, 
					't': [
						[
							0,
							25, 
							1000
						]
					]
				}, 
				'textContent': 'x<a<b> example text <c>>x'
			}),

		expected : { name: {localName: 'treeRoot'},
					 children: [ 
						 'x',
						 { name: {localName: 'element_a'},
						   children: [
							   '<a',
 							   { name:{localName: 'element_b'},
								 children:['<b>'] },
							   ' example text ',
							   { name:{localName: 'element_c'},
								 children:['<c>']},
							   '>'
						   ]},
						 'x'
					 ]},

		
		testTree : function () {

			var tree = new Y.Faust.AdhocTree(this.text, 
											 ['element_a',
											  'element_b', 
											  'element_c']);


			Y.assert(structurallyEqual(tree, this.expected), 'adhoctree test');
		},
	});

	adhocTreeTestSuite.add(basicTest);

	var emptyTest = new Y.Test.Case({

		name: 'Empty Adhoc Tree',
		
		text: Y.Faust.Text.create(
			{
				'annotations': [
					{
						'd': {
							'xml:node': '1'
						}, 
						'id': 0, 
						'n': 4, 
						't': [
							[
								0, 
								0, 
								1000
							]
						]
					}, 
					{
						'd': {
							'xml:node': '2',
						}, 
						'id': 1, 
						'n': 1, 
						't': [
							[
								0, 
								0, 
								1000
							]
						]
					},
					{
						'd': {
							'xml:node': '1/2'
						}, 
						'id': 2, 
						'n': 2, 
						't': [
							[
								0, 
								0, 
								1000
							]
						]
					}, 
					{
						'd': {
							'xml:node': '2/2'
						}, 
						'id': 3, 
						'n': 3, 
						't': [
							[
								0, 
								0, 
								1000
							]
						]
					}, 
					{
						'd': {
							'xml:node': '4'
						}, 
						'id': 4, 
						'n': 4, 
						't': [
							[
								0, 
								0, 
								1000
							]
						]
					}, 

				], 

				'names': {
 					'1': [
						'http://interedition.eu/ns', 
						'element_a'
					], 
					'2': [
						'http://interedition.eu/ns', 
						'element_b'
					], 
					'3': [
						'http://interedition.eu/ns', 
						'element_c'
					], 
					'4': [
						'http://interedition.eu/ns', 
						'element_d'
					], 
					'100': [
						'http://interedition.eu/ns', 
						'text'
					], 

				}, 
				'text': {
					'd': {},
					'id': 1000, 
					'n': 100, 
					't': [
						[
							0,
							25, 
							1000
						]
					]
				}, 
				'textContent': ''
			}),

		expected : { name: {localName: 'treeRoot'},
					 children: [ 
						 { name:{localName: 'element_d'},
						   children:[] },
						 { name: {localName: 'element_a'},
						   children: [
 							   { name:{localName: 'element_b'},
								 children:[] },
							   { name:{localName: 'element_c'},
								 children:[]},
						   ]},
						 { name:{localName: 'element_d'},
						   children:[] },
					 ]},

		
		testEmptyTree : function () {

			var tree = new Y.Faust.AdhocTree(this.text, 
											 ['element_a',
											  'element_b', 
											  'element_c',
											  'element_d']);


			Y.assert(structurallyEqual(tree, this.expected), 'test an empty tree');
		},
	});

	adhocTreeTestSuite.add(emptyTest);

	var emptyParentSiblingTest = new Y.Test.Case({

		name: 'Empty Parent Sibling Test',
		
		text: Y.Faust.Text.create(
			{
				'annotations': [
					{
						'd': {
							'xml:node': '1',
						}, 
						'id': 1, 
						'n': 1, 
						't': [
							[
								0, 
								0, 
								1000
							]
						]
					}, 
					{
						'd': {
							'xml:node': '2'
						}, 
						'id': 2, 
						'n': 2, 
						't': [
							[
								0, 
								1, 
								1000
							]
						]
					}, 
					{
						'd': {
							'xml:node': '1/2'
						}, 
						'id': 1, 
						'n': 3, 
						't': [
							[
								1, 
								1, 
								1000
							]
						]
					}, 

				], 

				'names': {
 					'1': [
						'http://interedition.eu/ns', 
						'element_a'
					], 
					'2': [
						'http://interedition.eu/ns', 
						'element_b'
					], 
					'3': [
						'http://interedition.eu/ns', 
						'element_c'
					], 
					'100': [
						'http://interedition.eu/ns', 
						'text'
					], 

				}, 
				'text': {
					'd': {},
					'id': 1000, 
					'n': 100, 
					't': [
						[
							0,
							25, 
							1000
						]
					]
				}, 
				'textContent': 'xyz'
			}),

		expected : { name: {localName: 'treeRoot'},
					 children: [
						 { name: {localName: 'element_a'},
						   children: []},
 						 { name:{localName: 'element_b'},
						   children:[
							   'x',
							   { name:{localName: 'element_c'},
								 children:[]},
						   ] },
						 'yz'
					 ]},

		
		testEmptyParentSibling : function () {

			var tree = new Y.Faust.AdhocTree(this.text, 
											 ['element_a',
											  'element_b', 
											  'element_c']);


			Y.assert(structurallyEqual(tree, this.expected), 'empty siblings of the parent are not nested correctly');
		},
	});

	adhocTreeTestSuite.add(emptyParentSiblingTest);

	var realWorldTreeTest = new Y.Test.Case({

		name: 'Real World Tree Test',
		
		text: Y.Faust.Text.create(
			{
				'annotations': [
					{
						'd': {
							'xml:node': '1',
						}, 
						'id': 1, 
						'n': 1, 
						't': [
							[
								0, 
								2, 
								1000
							]
						]
					}, 
					{
						'd': {
							'xml:node': '1/1'
						}, 
						'id': 2, 
						'n': 2, 
						't': [
							[
								0, 
								1, 
								1000
							]
						]
					}, 
 					{
						'd': {
							'xml:node': '2/1'
						}, 
						'id': 3, 
						'n': 2, 
						't': [
							[
								1, 
								1, 
								1000
							]
						]
					}, 
					{
						'd': {
							'xml:node': '1/2/1'
						}, 
						'id': 4, 
						'n': 3, 
						't': [
							[
								1, 
								1, 
								1000
							]
						]
					}, 

					{
						'd': {
							'xml:node': '2/2/1'
						}, 
						'id': 5, 
						'n': 4, 
						't': [
							[
								1, 
								1, 
								1000
							]
						]
					}, 

					{
						'd': {
							'xml:node': '3/1'
						}, 
						'id': 6, 
						'n': 2, 
						't': [
							[
								1, 
								2, 
								1000
							]
						]
					}, 

					{
						'd': {
							'xml:node': '1/3/1'
						}, 
						'id': 7, 
						'n': 3, 
						't': [
							[
								1, 
								1, 
								1000
							]
						]
					}, 


				], 

				'names': {
 					'1': [
						'http://interedition.eu/ns', 
						'element_a'
					], 
					'2': [
						'http://interedition.eu/ns', 
						'element_b'
					], 
					'3': [
						'http://interedition.eu/ns', 
						'element_c'
					], 
					'4': [
						'http://interedition.eu/ns', 
						'element_d'
					], 

				}, 
				'text': {
					'd': {},
					'id': 1000, 
					'n': 100, 
					't': [
						[
							0,
							25, 
							1000
						]
					]
				}, 
				'textContent': 'xy'
			}),

		expected : 
		{name: {localName: 'treeRoot'}, children: [
			{name: {localName: 'element_a'}, children: [	
				{name: {localName: 'element_b'}, children: [
					"x"
				]},
				{name: {localName: 'element_b'}, children: [
					{name: {localName: 'element_c'}, children: []},
					{name: {localName: 'element_d'}, children: []},
				]},
				{name: {localName: 'element_b'}, children: [
					{name: {localName: 'element_c'}, children: []},
					'y'
				]},
			]},
		]},

		
		testRealWorldTree : function () {

			var tree = new Y.Faust.AdhocTree(this.text, 
											 ['element_a',
											  'element_b', 
											  'element_c',
											  'element_d']);


			Y.assert(structurallyEqual(tree, this.expected), 'real world tree fails');
		},
	});

	adhocTreeTestSuite.add(realWorldTreeTest);


	Y.mix(Y.namespace("FaustTest"), {
        adhocTreeTestSuite: adhocTreeTestSuite,
    });

}, '0.0', {
    requires: ["adhoc-tree", "text-annotation", "yui-base"]
});