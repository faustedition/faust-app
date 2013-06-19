YUI.add('adhoc-tree', function (Y) {

 	var XMLNodeUtils = {
		documentOrderSort : function(a,b) {
			var aNode = a.data['xml:node'].split('/').reverse().map(function(x){return parseInt(x)});
			var bNode = b.data['xml:node'].split('/').reverse().map(function(x){return parseInt(x)});
			if (bNode[0].length === 0)
				return 1;
			for (var i=0; true; i++) {
				if (!aNode[i])
					return -1;
				if (!bNode[i])
					return 1;
				if (aNode[i] < bNode[i])
					return -2;				
				if (aNode[i] > bNode[i])
					return 2;				
			}
		},

		// is a descendant of b in document?
		isDescendant : function(a, b) {
			var aNode = a.data['xml:node'].split('/').reverse().map(function(x){return parseInt(x)});
			var bNode = b.data['xml:node'].split('/').reverse().map(function(x){return parseInt(x)});
			if (aNode[0].length === 0)
				return false;
			for (var i=0; true; i++) {
				if (!aNode[i])
					return false;
				if (!bNode[i])
					return true;
				if (aNode[i] != bNode[i])
					return false;				
			}		
		},

		// is a following b in document order?
		isFollowing : function(a,b) {
			return XMLNodeUtils.documentOrderSort(a, b) > 0 ? true : false;
		},		
	};


	function _sortByRange(a, b) {
		if (a.targets[0].range.start === b.targets[0].range.start)
			return a.targets[0].range.end - b.targets[0].range.end;
		else
			return a.targets[0].range.start - b.targets[0].range.start;
	};

	function _nextOutermostAnnotationCandidates(transcript, start, end, filter, siblings, ancestors, parent,
												documentOrderSort, isDescendant) {

		var allAnnotations = transcript.find (start, end, filter);
		
		var includedAnnotations = Y.Array.filter(allAnnotations, function(annotation) {

			//exclude children of exclude too!
			var descendantOrSelfOfExcluded = false;
			Y.Array.each(siblings, function(sibling){
				descendantOrSelfOfExcluded = isDescendant(annotation, sibling) || descendantOrSelfOfExcluded;
			});
			
			var exclude = ancestors.concat(siblings);
			
			return exclude.indexOf(annotation) < 0 && isDescendant(annotation, parent) && !descendantOrSelfOfExcluded;

			// return exclude.indexOf(annotation) < 0 && isDescendant(annotation, parent);
		});

		if (includedAnnotations.length === 0)
			return [];

		// assuming annotations ordered by start position

		var smallestStart = includedAnnotations[0].targets[0].range.start;

		var firstAnnotations = Y.Array.filter(includedAnnotations, function(annotation) {
			return annotation.targets[0].range.start === smallestStart;
		});

		var ends = Y.Array.map(firstAnnotations, function(annotation) {
			return annotation.targets[0].range.end;
		});

		var greatestEnd = Y.Array.reduce(ends, start, function(x,y){
			return Math.max(x,y)
		});

		var nextOutermostAnnotationCandidates = Y.Array.filter(firstAnnotations, function(annotation){
			return annotation.targets[0].range.end === greatestEnd;
		});

		//return nextOutermostAnnotationCandidates;
		return firstAnnotations.sort(documentOrderSort);

	};

	function _nextOutermostAnnotation(transcript, start, end, filter, siblings, ancestors, parent,
									  documentOrderSort, isDescendant) {

		var nextOutermostAnnotationCandidates = _nextOutermostAnnotationCandidates(transcript, start, end, filter, siblings, ancestors, parent,
																				   documentOrderSort, isDescendant);

		var exclude = ancestors.concat(siblings);

		var includedAnnotations = Y.Array.filter(nextOutermostAnnotationCandidates, function(annotation) {
			return exclude.indexOf(annotation) < 0;
		});

		if (includedAnnotations.length === 0)
			return null;

 		var descendants = Y.Array.filter(includedAnnotations, function(annotation){
			return isDescendant(annotation, parent);
		});
		

		// filter out descendents of siblings; necessary with empty elements

		var nonDescendantsOfSiblings = Y.Array.filter(descendants, function(annotation){
			for (var i=0; i < siblings.length; i++ ) {
				if (isDescendant(annotation, siblings[i]))
					return false;
			}
			return true;
		});

		var sortedByNesting = nonDescendantsOfSiblings.sort(documentOrderSort);

		return sortedByNesting.length > 0 ? sortedByNesting[0] : null;
	}

	function AdhocNode(parent) {
		this.parent = parent;
	};

	Y.extend(AdhocNode, Object, {
		ancestors : function() {
			var node = this;
			var result = [];
			while (node.parent) {
				node = node.parent;
				result.push(node);
			}
			return result;	
		},
		children : function() {
			return [];
		},
		_getAncestorProperty: function(property) {
			if (this[property]) {
				return this[property];
			} else {
				if (this.parent) {
					return this.parent._getAncestorProperty(property);
				}
			}
		},
		transcript : function() {
			return this._getAncestorProperty('_transcript');
		}, 
		filter  : function() {
			return this._getAncestorProperty('_filter');
		}, 
	});

	function TextNode(range, parent) {
		TextNode.superclass.constructor.call(this, parent);
		this.range = range;
	}

	Y.extend(TextNode, AdhocNode, {
		content: function() {
			return this.range.of(this.transcript().content);
		},
		toString : function() {
			return this.content();
		}

	});

	function AnnotationNode(annotation, parent) {
		AnnotationNode.superclass.constructor.call(this, parent);	
		this.annotation = annotation;
	};

	Y.extend(AnnotationNode, AdhocNode, {

		_textNodesForPartitions: function(start, end, parent) {
			var partitions = this.transcript().partition(null, start, end);
			var textNodes = Y.Array.map(partitions, function(partition) {
				return new TextNode(new Y.Faust.Range(partition.start, partition.end), parent);
			});
			return textNodes;
		},

		_iterateChildren: function(start, end, ancestors, documentOrderSort, isDescendant) {
			var siblings = [];
			var result = [];
			var from = start;
			var to = end;
			while (true) {
				var annotation = _nextOutermostAnnotation(this.transcript(), from, end, this.filter(), siblings, 
														  ancestors, this.annotation,
														  documentOrderSort, isDescendant);
				if (annotation) {

					// there is a sibling non-text node
					var prefixStart = from;
					var prefixEnd = annotation.targets[0].range.start;
					var annotationEnd = annotation.targets[0].range.end;
					if (prefixEnd > prefixStart) {
						// there is text content before the next sibling node
						var textNodes = this._textNodesForPartitions(prefixStart, prefixEnd, this);
						result = result.concat(textNodes);
					} 
					siblings.push(annotation);
					var annotationNode = new AnnotationNode(annotation, this);
					result.push(annotationNode);
					from = annotationEnd;
				} else {
					// text node
					if (from < end) {					
						var textNodes = this._textNodesForPartitions(from, end, this);
						result = result.concat(textNodes);
					}
					return result;
				}
			}
		},
		
		data: function() {
			return this.annotation.data;
		},
		children: function() {
			var ancestorAnnotationCandidates = Y.Array.map([this].concat(this.ancestors()), function(node) {return node.annotation});
			//ancestorAnnotationCandidates = ancestorAnnotationCandidates.push(this.annotation);
			var ancestorAnnotations = Y.Array.filter(ancestorAnnotationCandidates, function(annotation){return annotation});
			var range = this.annotation.targets[0].range;
			return this._iterateChildren(range.start, range.end, ancestorAnnotations, 
										 this._getAncestorProperty('_documentOrderSort'),
										 this._getAncestorProperty('_isDescendant'));
		},
		name: function() {
			return this.annotation.name;
		},
		toString : function() {
			var result = "";
			var name = this.name().localName;
			var node = this.data()['xml:node'] ? ' ' + this.data()['xml:node'] + ' ' : '';
			result = result + (name ? '<' + name + node + '>': '');
			Y.Array.each(this.children(), function(child){
				result = result + child.toString();
			});
			result = result + (name ? '</' + name + '>': '');
			return result;
		}
	});

	function AdhocTree(transcript, filter, documentOrderSort, isDescendant) {

		var mockAnnotation = new Y.Faust.Annotation(new Y.Faust.Name("http://interedition.eu/ns", "treeRoot"),
													{'xml:node': ''},
													[new Y.Faust.TextTarget(transcript,
																		   new Y.Faust.Range(0, transcript.content.length))]
		 										   );												
													
		AdhocTree.superclass.constructor.call(this, mockAnnotation);
		this._transcript = transcript;
		this._filter = filter;		
		this._documentOrderSort = documentOrderSort;
		this._isDescendant = isDescendant;
	}
	
	Y.extend(AdhocTree, AnnotationNode, {
		children: function() {
			return this._iterateChildren(0, this.transcript().content.length, [],
										 this._getAncestorProperty('_documentOrderSort'),
										 this._getAncestorProperty('_isDescendant'));
		},
		data: function() {
			return {};
		},

	});
	Y.mix(Y.namespace("Faust"), {
        AdhocTree: AdhocTree,
        TextNode: TextNode,
        AnnotationNode: AnnotationNode,
		XMLNodeUtils: XMLNodeUtils
    });
}, '0.0', {
    requires: ["text-annotation", "array"]
});