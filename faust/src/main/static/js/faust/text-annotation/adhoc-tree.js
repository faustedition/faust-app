YUI.add('adhoc-tree', function (Y) {
	
	function _sortXMLNodeValues(a,b) {
		var aNode = a.split('/').reverse();
		var bNode = b.split('/').reverse();
		if (bNode[0].length === 0)
			return 1;
		for (i=0; true; i++) {
			if (!aNode[i])
				return -1;
			if (!bNode[i])
				return 1;
			if (aNode[i] < bNode[i])
				return -2;				
		}
	};

	// is a following b in document order?
	function _isFollowing(a,b) {
		return _sortXMLNodeValues(a.data['xml:node'], b.data['xml:node']) === 1 ? true : false;
	};
	
	function _sortByXMLNodeData(a, b) {
		return _sortXMLNodeValues(a.data['xml:node'], b.data['xml:node']);
	};

	function _sortByRange(a, b) {
		if (a.targets[0].range.start === b.targets[0].range.start)
			return a.targets[0].range.end - b.targets[0].range.end;
		else
			return a.targets[0].range.start - b.targets[0].range.start;
	};

	function _outermostAnnotations(transcript, start, end, filter, exclude) {
		
		var allAnnotations = transcript.find (start, end, filter);
		
		var includedAnnotations = Y.Array.filter(allAnnotations, function(annotation) {
			return exclude.indexOf(annotation) < 0;
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

		var outermostAnnotations = Y.Array.filter(firstAnnotations, function(annotation){
			return annotation.targets[0].range.end === greatestEnd;
		});

		return outermostAnnotations;

	};

	function _outermostAnnotation(transcript, start, end, filter, exclude, parent) {

		//var ancestorsAndSelf = parent.ancestors().push(parent);
		var outermostAnnotations = _outermostAnnotations(transcript, start, end, filter, exclude);

		var includedAnnotations = Y.Array.filter(outermostAnnotations, function(annotation) {
			return exclude.indexOf(annotation) < 0;
		});

		if (includedAnnotations.length === 0)
			return null;

		var descendants = Y.Array.filter(includedAnnotations, function(annotation){
			return _isFollowing(annotation, parent);
		});
		
		var sortedByNesting = descendants.sort(_sortByXMLNodeData);

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

		_iterateChildren: function(start, end, annotationStack) {
			var result = [];
			var from = start;
			var to = end;
			while (true) {

				var annotation = _outermostAnnotation(this.transcript(), from, end, this.filter(), annotationStack, this.annotation);
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
					annotationStack.push(annotation);
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
			return this._iterateChildren(range.start, range.end, ancestorAnnotations);
		},
		name: function() {
			return this.annotation.name;
		},
		toString : function() {
			var result = "";
			var name = this.name().localName;
			result = result + (name ? '<' + name + '>': '');
			Y.Array.each(this.children(), function(child){
				result = result + child.toString();
			});
			result = result + (name ? '</' + name + '>': '');
			return result;
		}
	});

	function AdhocTree(transcript, filter) {
		var mockAnnotation = {
			targets: [{
				ranges: [{
					start: 0,
					end: transcript.content.length
				}]
			}],
			name: new Y.Faust.Name("http://interedition.eu/ns", "treeRoot"),
			data: {'xml:node': ''}
		};
		AdhocTree.superclass.constructor.call(this, mockAnnotation);
		this._transcript = transcript;
		this._filter = filter;		
	}
	
	Y.extend(AdhocTree, AnnotationNode, {
		children: function() {
			return this._iterateChildren(0, this.transcript().content.length, []);
		},
		data: function() {
			return {};
		},

	});
	Y.mix(Y.namespace("Faust"), {
        AdhocTree: AdhocTree,
        TextNode: TextNode,
        AnnotationNode: AnnotationNode
    });
}, '0.0', {
    requires: ["text-annotation", "array"]
});