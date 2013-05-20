YUI.add('test-adhoc-tree', function (Y) {

	function structurallyEqual(node1, node2) {
		if (node1 instanceof Y.Faust.TextNode)
			return node1.content === node2.content;

		var nameEqual = node1.annotation.name.localName === node2.annotation.name.localName;
		var children1 = node1.children();
		var children2 = node2.children();
		if (children1.length !== children2.length)
			return false;
		for (i=0; i < children1.length; i++) {
			if (!structurallyEqual(children1[i], children2[i]))
				return false;
		}
		return true;
	};

	var AdhocTreeTest = new Y.Test.Case({

		name: "Ranges to Goddag",
		
		text: 
		{
			"annotations": [
				{
					"d": {
						"xml:node": "1",
					}, 
					"id": 1, 
					"n": 1, 
					"t": [
						[
							0, 
							25, 
							1000
						]
					]
				}, 
				{
					"d": {
						"xml:node": "1/1"
					}, 
					"id": 2, 
					"n": 2, 
					"t": [
						[
							3, 
							5, 
							1000
						]
					]
				}, 
				{
					"d": {
						"xml:node": "2/1"
					}, 
					"id": 3, 
					"n": 3, 
					"t": [
						[
							20, 
							23, 
							1000
						]
					]
				}, 

			], 

			"names": {
 				"1": [
					"http://interedition.eu/ns", 
					"element_a"
				], 
				"2": [
					"http://interedition.eu/ns", 
					"element_b"
				], 
				"3": [
					"http://interedition.eu/ns", 
					"element_c"
				], 
				"100": [
					"http://interedition.eu/ns", 
					"text"
				], 

			}, 
			"text": {
				"d": {},
				"id": 1000, 
				"n": 100, 
				"t": [
					[
						0,
						25, 
						1000
					]
				]
			}, 
			"textContent": " [a[b] example text [c]] "
		},



		testSimpleTree : function () {
			var text = Y.Faust.Text.create(this.text);
			var tree = new Y.Faust.AdhocTree(text, 
											 ['element_a',
											  'element_b', 
											  'element_c']);
			expected = {};
			console.log(tree);
			Y.assert(structurallyEqual(tree, tree), "adhoctree test");
		},
	});

	var r2gTestCase = new Y.Test.Case({

		name: "Ranges to Goddag",

		testRangesToGoddag : function () {
			//Y.assert(this.ranges != this.goddag, "Testing the testing");
			console.log('running a test');
			Y.assert(false, "bla test");
			
		},

		ranges: {
			"annotations": [
				{
					"d": {
						"xml:piData": "RNGSchema=\"https://faustedition.uni-wuerzburg.de/schema/1.3/faust-tei.rng\" type=\"xml\"", 
						"xml:piTarget": "oxygen"
					}, 
					"id": 68394, 
					"n": 27, 
					"t": [
						[
							0, 
							0, 
							4176
						]
					]
				}, 
				{
					"d": {
						"url": "faust://facsimile/gsa/391098/391098_0001"
					}, 
					"id": 68395, 
					"n": 48, 
					"t": [
						[
							0, 
							0, 
							4176
						]
					]
				}, 
				{
					"d": {}, 
					"id": 68396, 
					"n": 40, 
					"t": [
						[
							0, 
							0, 
							4176
						]
					]
				}, 
				{
					"d": {
						"mimeType": "image/svg+xml", 
						"url": "faust://xml/image-text-links/8472954e-2177-4d16-8015-767fe34c49d1.svg"
					}, 
					"id": 68397, 
					"n": 48, 
					"t": [
						[
							0, 
							0, 
							4176
						]
					]
				}, 
				{
					"d": {}, 
					"id": 68398, 
					"n": 44, 
					"t": [
						[
							0, 
							0, 
							4176
						]
					]
				}, 
				{
					"d": {
						"new": "#xx_bl"
					}, 
					"id": 68399, 
					"n": 45, 
					"t": [
						[
							0, 
							0, 
							4176
						]
					]
				}, 
				{
					"d": {}, 
					"id": 68400, 
					"n": 76, 
					"t": [
						[
							0, 
							3, 
							4176
						]
					]
				}, 
				{
					"d": {
						"quantity": "5", 
						"unit": "chars"
					}, 
					"id": 68401, 
					"n": 50, 
					"t": [
						[
							4, 
							4, 
							4176
						]
					]
				}, 
				{
					"d": {
						"{http://www.w3.org/XML/1998/namespace}id": "la"
					}, 
					"id": 68402, 
					"n": 38, 
					"t": [
						[
							0, 
							4, 
							4176
						]
					]
				}, 
				{
					"d": {
						"ref": "#parenthesis_left"
					}, 
					"id": 68403, 
					"n": 69, 
					"t": [
						[
							5, 
							5, 
							4176
						]
					]
				}, 
				{
					"d": {
						"ref": "#parenthesis_right"
					}, 
					"id": 68404, 
					"n": 69, 
					"t": [
						[
							6, 
							6, 
							4176
						]
					]
				}, 
				{
					"d": {
						"{http://www.w3.org/XML/1998/namespace}id": "lb"
					}, 
					"id": 68405, 
					"n": 38, 
					"t": [
						[
							5, 
							6, 
							4176
						]
					]
				}, 
				{
					"d": {
						"{http://www.faustedition.net/ns}bottom-top": "#Hauptzone", 
						"{http://www.faustedition.net/ns}right-left": "#Hauptzone", 
						"{http://www.w3.org/XML/1998/namespace}id": "Signatur"
					}, 
					"id": 68406, 
					"n": 39, 
					"t": [
						[
							0, 
							6, 
							4176
						]
					]
				}, 
				{
					"d": {
						"new": "#sc_t"
					}, 
					"id": 68407, 
					"n": 45, 
					"t": [
						[
							8, 
							8, 
							4176
						]
					]
				}, 
				{
					"d": {}, 
					"id": 68408, 
					"n": 40, 
					"t": [
						[
							8, 
							8, 
							4176
						]
					]
				}, 
				{
					"d": {
						"rend": "sup"
					}, 
					"id": 68409, 
					"n": 33, 
					"t": [
						[
							11, 
							13, 
							4176
						]
					]
				}, 
				{
					"d": {
						"rend": "right", 
						"{http://www.w3.org/XML/1998/namespace}id": "lc"
					}, 
					"id": 68410, 
					"n": 38, 
					"t": [
						[
							8, 
							13, 
							4176
						]
					]
				}, 
				{
					"d": {
						"quantity": "1", 
						"unit": "lines"
					}, 
					"id": 68411, 
					"n": 47, 
					"t": [
						[
							13, 
							13, 
							4176
						]
					]
				}, 
				{
					"d": {
						"{http://www.faustedition.net/ns}left-right": "#Hauptzone", 
						"{http://www.faustedition.net/ns}top": "#Signatur"
					}, 
					"id": 68412, 
					"n": 39, 
					"t": [
						[
							7, 
							13, 
							4176
						]
					]
				}, 
				{
					"d": {
						"new": "#jo_t_lat"
					}, 
					"id": 68413, 
					"n": 45, 
					"t": [
						[
							15, 
							15, 
							4176
						]
					]
				}, 
				{
					"d": {
						"rend": "centered", 
						"{http://www.w3.org/XML/1998/namespace}id": "ld"
					}, 
					"id": 68414, 
					"n": 38, 
					"t": [
						[
							15, 
							21, 
							4176
						]
					]
				}, 
				{
					"d": {
						"rend": "centered", 
						"{http://www.w3.org/XML/1998/namespace}id": "le"
					}, 
					"id": 68415, 
					"n": 38, 
					"t": [
						[
							22, 
							35, 
							4176
						]
					]
				}, 
				{
					"d": {
						"quantity": "0.5", 
						"unit": "lines"
					}, 
					"id": 68416, 
					"n": 47, 
					"t": [
						[
							35, 
							35, 
							4176
						]
					]
				}, 
				{
					"d": {}, 
					"id": 68417, 
					"n": 82, 
					"t": [
						[
							36, 
							41, 
							4176
						]
					]
				}, 
				{
					"d": {
						"rend": "centered", 
						"{http://www.w3.org/XML/1998/namespace}id": "lf"
					}, 
					"id": 68418, 
					"n": 38, 
					"t": [
						[
							36, 
							41, 
							4176
						]
					]
				}, 
				{
					"d": {
						"type": "main", 
						"{http://www.w3.org/XML/1998/namespace}id": "Hauptzone"
					}, 
					"id": 68419, 
					"n": 39, 
					"t": [
						[
							14, 
							41, 
							4176
						]
					]
				}, 
				{
					"d": {}, 
					"id": 68420, 
					"n": 43, 
					"t": [
						[
							0, 
							42, 
							4176
						]
					]
				}, 
				{
					"d": {}, 
					"id": 68421, 
					"n": 46, 
					"t": [
						[
							0, 
							42, 
							4176
						]
					]
				}, 
				{
					"d": {}, 
					"id": 68422, 
					"n": 32, 
					"t": [
						[
							0, 
							42, 
							4176
						]
					]
				}, 
				{
					"d": {
						"value": "#xx_bl"
					}, 
					"id": 68423, 
					"n": 49, 
					"t": [
						[
							0, 
							8, 
							4176
						]
					]
				}, 
				{
					"d": {
						"value": "#sc_t"
					}, 
					"id": 68424, 
					"n": 49, 
					"t": [
						[
							8, 
							15, 
							4176
						]
					]
				}, 
				{
					"d": {
						"value": "#jo_t_lat"
					}, 
					"id": 68425, 
					"n": 49, 
					"t": [
						[
							15, 
							42, 
							4176
						]
					]
				}
			], 
			"names": {
				"27": [
					"http://www.w3.org/XML/1998/namespace", 
					"pi"
				], 
				"32": [
					"http://www.tei-c.org/ns/1.0", 
					"TEI"
				], 
				"33": [
					"http://www.tei-c.org/ns/1.0", 
					"hi"
				], 
				"38": [
					"http://www.tei-c.org/ns/geneticEditions", 
					"line"
				], 
				"39": [
					"http://www.tei-c.org/ns/1.0", 
					"zone"
				], 
				"40": [
					"http://www.w3.org/XML/1998/namespace", 
					"comment"
				], 
				"43": [
					"http://www.tei-c.org/ns/1.0", 
					"surface"
				], 
				"44": [
					"http://www.tei-c.org/ns/1.0", 
					"facsimile"
				], 
				"45": [
					"http://www.tei-c.org/ns/1.0", 
					"handShift"
				], 
				"46": [
					"http://www.tei-c.org/ns/geneticEditions", 
					"document"
				], 
				"47": [
					"http://www.faustedition.net/ns", 
					"vspace"
				], 
				"48": [
					"http://www.tei-c.org/ns/1.0", 
					"graphic"
				], 
				"49": [
					"http://www.faustedition.net/ns", 
					"hand"
				], 
				"50": [
					"http://www.faustedition.net/ns", 
					"hspace"
				], 
				"69": [
					"http://www.tei-c.org/ns/1.0", 
					"g"
				], 
				"76": [
					"http://www.tei-c.org/ns/1.0", 
					"num"
				], 
				"82": [
					"http://www.tei-c.org/ns/1.0", 
					"date"
				]
			}, 
			"text": {
				"id": 4176, 
				"l": 42, 
				"t": "txt"
			}, 
			"textContent": "XIX \n3 \n38.4. \nFau\u017ft.\nZweyter Theil\n1831. "
		},
		
		goddag: {
			"namespaces": {
				"dtd": "http://www.w3.org/TR/REC-xml", 
				"f": "http://www.faustedition.net/ns", 
				"ge": "http://www.tei-c.org/ns/geneticEditions", 
				"goddag": "http://launchpad.net/goddag4j/", 
				"rng": "http://relaxng.org/ns/structure/1.0", 
				"svg": "http://www.w3.org/2000/svg", 
				"tei": "http://www.tei-c.org/ns/1.0", 
				"xlink": "http://www.w3.org/1999/xlink", 
				"xml": "http://www.w3.org/XML/1998/namespace", 
				"xmlns": "http://www.w3.org/2000/xmlns/", 
				"xsd": "http://www.w3.org/2001/XMLSchema", 
				"xsi": "http://www.w3.org/2001/XMLSchema-instance"
			}, 
			"nodes": [
				[
					[
						1038153, 
						"Fau\u017ft."
					], 
					[
						1038126, 
						"\n"
					], 
					[
						1038152, 
						"\n"
					], 
					[
						1038119, 
						"XIX"
					], 
					[
						1038177, 
						"1831."
					], 
					[
						1038117, 
						"\n"
					], 
					[
						1038176, 
						"\n"
					], 
					[
						1038140, 
						"38."
					], 
					[
						1038141, 
						"4."
					], 
					[
						1038173, 
						"\n"
					], 
					[
						1038174, 
						"Zweyter "
					], 
					[
						1038137, 
						"\n"
					], 
					[
						1038175, 
						"Theil"
					], 
					[
						1038129, 
						"3"
					]
				], 
				[
					[
						1038127, 
						"tei:g", 
						[
							[
								"tei:ref", 
								"#parenthesis_right"
							]
						]
					], 
					[
						1038186, 
						"tei:seg", 
						[
							[
								"tei:function", 
								"ws"
							]
						]
					], 
					[
						1038184, 
						"tei:seg", 
						[
							[
								"tei:function", 
								"ws"
							]
						]
					], 
					[
						1038124, 
						"tei:g", 
						[
							[
								"tei:ref", 
								"#parenthesis_left"
							]
						]
					], 
					[
						1038123, 
						"ge:line", 
						[]
					], 
					[
						1038190, 
						"tei:seg", 
						[
							[
								"tei:function", 
								"ws"
							]
						]
					], 
					[
						1038188, 
						"tei:seg", 
						[
							[
								"tei:function", 
								"ws"
							]
						]
					], 
					[
						1038120, 
						"f:hspace", 
						[
							[
								"f:unit", 
								"chars"
							], 
							[
								"f:quantity", 
								"5"
							]
						]
					], 
					[
						1038178, 
						"tei:seg", 
						[
							[
								"tei:function", 
								"ws"
							]
						]
					], 
					[
						1038118, 
						"tei:num", 
						[]
					], 
					[
						1038182, 
						"tei:seg", 
						[
							[
								"tei:function", 
								"ws"
							]
						]
					], 
					[
						1038114, 
						"ge:line", 
						[]
					], 
					[
						1038180, 
						"tei:seg", 
						[
							[
								"tei:function", 
								"ws"
							]
						]
					], 
					[
						1038142, 
						"f:vspace", 
						[
							[
								"f:unit", 
								"lines"
							], 
							[
								"f:quantity", 
								"1"
							]
						]
					], 
					[
						1038138, 
						"tei:hi", 
						[
							[
								"tei:rend", 
								"sup"
							]
						]
					], 
					[
						1038133, 
						"ge:line", 
						[
							[
								"ge:rend", 
								"right"
							]
						]
					], 
					[
						1038130, 
						"tei:zone", 
						[
							[
								"f:top", 
								"#Signatur"
							], 
							[
								"f:left-right", 
								"#Hauptzone"
							]
						]
					], 
					[
						1038154, 
						"ge:line", 
						[
							[
								"ge:rend", 
								"centered"
							]
						]
					], 
					[
						1038157, 
						"f:vspace", 
						[
							[
								"f:unit", 
								"lines"
							], 
							[
								"f:quantity", 
								"0.5"
							]
						]
					], 
					[
						1038145, 
						"tei:zone", 
						[
							[
								"xml:id", 
								"Hauptzone"
							], 
							[
								"tei:type", 
								"main"
							]
						]
					], 
					[
						1038148, 
						"ge:line", 
						[
							[
								"ge:rend", 
								"centered"
							]
						]
					], 
					[
						1038168, 
						"f:hand", 
						[
							[
								"f:id", 
								"sc_t"
							]
						]
					], 
					[
						1038108, 
						"ge:document", 
						[]
					], 
					[
						1038109, 
						"tei:surface", 
						[]
					], 
					[
						1038170, 
						"f:hand", 
						[
							[
								"f:id", 
								"jo_t_lat"
							]
						]
					], 
					[
						1038110, 
						"tei:zone", 
						[
							[
								"xml:id", 
								"Signatur"
							], 
							[
								"f:right-left", 
								"#Hauptzone"
							], 
							[
								"f:bottom-top", 
								"#Hauptzone"
							]
						]
					], 
					[
						1038172, 
						"f:words", 
						[]
					], 
					[
						1038160, 
						"ge:line", 
						[
							[
								"ge:rend", 
								"centered"
							]
						]
					], 
					[
						1038164, 
						"f:apps", 
						[]
					], 
					[
						1038165, 
						"f:hands", 
						[]
					], 
					[
						1038166, 
						"f:hand", 
						[
							[
								"f:id", 
								"xx_bl"
							]
						]
					]
				], 
				[], 
				[], 
				[]
			], 
			"trees": [
				{
					"ch": [
						{
							"id": 1038117, 
							"nt": 0
						}, 
						{
							"ch": [
								{
									"id": 1038119, 
									"nt": 0
								}, 
								{
									"id": 1038126, 
									"nt": 0
								}
							], 
							"id": 1038178, 
							"nt": 1
						}, 
						{
							"ch": [
								{
									"id": 1038129, 
									"nt": 0
								}, 
								{
									"id": 1038137, 
									"nt": 0
								}
							], 
							"id": 1038180, 
							"nt": 1
						}, 
						{
							"ch": [
								{
									"id": 1038140, 
									"nt": 0
								}, 
								{
									"id": 1038141, 
									"nt": 0
								}, 
								{
									"id": 1038152, 
									"nt": 0
								}
							], 
							"id": 1038182, 
							"nt": 1
						}, 
						{
							"ch": [
								{
									"id": 1038153, 
									"nt": 0
								}, 
								{
									"id": 1038173, 
									"nt": 0
								}
							], 
							"id": 1038184, 
							"nt": 1
						}, 
						{
							"ch": [
								{
									"id": 1038174, 
									"nt": 0
								}
							], 
							"id": 1038186, 
							"nt": 1
						}, 
						{
							"ch": [
								{
									"id": 1038175, 
									"nt": 0
								}, 
								{
									"id": 1038176, 
									"nt": 0
								}
							], 
							"id": 1038188, 
							"nt": 1
						}, 
						{
							"ch": [
								{
									"id": 1038177, 
									"nt": 0
								}
							], 
							"id": 1038190, 
							"nt": 1
						}
					], 
					"id": 1038172, 
					"nt": 1
				}, 
				{
					"ch": [
						{
							"ch": [
								{
									"id": 1038119, 
									"nt": 0
								}, 
								{
									"id": 1038126, 
									"nt": 0
								}, 
								{
									"id": 1038129, 
									"nt": 0
								}, 
								{
									"id": 1038137, 
									"nt": 0
								}
							], 
							"id": 1038166, 
							"nt": 1
						}, 
						{
							"ch": [
								{
									"id": 1038140, 
									"nt": 0
								}, 
								{
									"id": 1038141, 
									"nt": 0
								}, 
								{
									"id": 1038152, 
									"nt": 0
								}
							], 
							"id": 1038168, 
							"nt": 1
						}, 
						{
							"ch": [
								{
									"id": 1038153, 
									"nt": 0
								}, 
								{
									"id": 1038173, 
									"nt": 0
								}, 
								{
									"id": 1038174, 
									"nt": 0
								}, 
								{
									"id": 1038175, 
									"nt": 0
								}, 
								{
									"id": 1038176, 
									"nt": 0
								}, 
								{
									"id": 1038177, 
									"nt": 0
								}
							], 
							"id": 1038170, 
							"nt": 1
						}
					], 
					"id": 1038165, 
					"nt": 1
				}, 
				{
					"id": 1038164, 
					"nt": 1
				}, 
				{
					"ch": [
						{
							"ch": [
								{
									"ch": [
										{
											"ch": [
												{
													"id": 1038117, 
													"nt": 0
												}, 
												{
													"ch": [
														{
															"id": 1038119, 
															"nt": 0
														}
													], 
													"id": 1038118, 
													"nt": 1
												}, 
												{
													"id": 1038120, 
													"nt": 1
												}
											], 
											"id": 1038114, 
											"nt": 1
										}, 
										{
											"ch": [
												{
													"id": 1038126, 
													"nt": 0
												}, 
												{
													"id": 1038124, 
													"nt": 1
												}, 
												{
													"id": 1038129, 
													"nt": 0
												}, 
												{
													"id": 1038127, 
													"nt": 1
												}
											], 
											"id": 1038123, 
											"nt": 1
										}
									], 
									"id": 1038110, 
									"nt": 1
								}, 
								{
									"ch": [
										{
											"ch": [
												{
													"id": 1038137, 
													"nt": 0
												}, 
												{
													"id": 1038140, 
													"nt": 0
												}, 
												{
													"ch": [
														{
															"id": 1038141, 
															"nt": 0
														}
													], 
													"id": 1038138, 
													"nt": 1
												}
											], 
											"id": 1038133, 
											"nt": 1
										}, 
										{
											"id": 1038142, 
											"nt": 1
										}
									], 
									"id": 1038130, 
									"nt": 1
								}, 
								{
									"ch": [
										{
											"ch": [
												{
													"id": 1038152, 
													"nt": 0
												}, 
												{
													"id": 1038153, 
													"nt": 0
												}
											], 
											"id": 1038148, 
											"nt": 1
										}, 
										{
											"ch": [
												{
													"id": 1038173, 
													"nt": 0
												}, 
												{
													"id": 1038174, 
													"nt": 0
												}, 
												{
													"id": 1038175, 
													"nt": 0
												}
											], 
											"id": 1038154, 
											"nt": 1
										}, 
										{
											"id": 1038157, 
											"nt": 1
										}, 
										{
											"ch": [
												{
													"id": 1038176, 
													"nt": 0
												}, 
												{
													"id": 1038177, 
													"nt": 0
												}
											], 
											"id": 1038160, 
											"nt": 1
										}
									], 
									"id": 1038145, 
									"nt": 1
								}
							], 
							"id": 1038109, 
							"nt": 1
						}
					], 
					"id": 1038108, 
					"nt": 1
				}
			]
		}

	});

	Y.mix(Y.namespace("FaustTest"), {
        AdhocTreeTest: AdhocTreeTest,
    });

}, '0.0', {
    requires: ["adhoc-tree", "text-annotation"]
});