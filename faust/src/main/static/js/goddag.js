const NT_TEXT = 0;
const NT_ELEMENT = 1;
const NT_COMMENT = 2;
const NT_PI = 3;

MultiRootedTree = function(data) {
	var nodes = new Array([], [], [], []);
	for (var nt = 0; (nt < 4 && nt < data.nodes.length); nt++) {
		var nodespace = nodes[nt];
		data.nodes[nt].forEach(function(node) {
			nodespace[node.shift()] = node;
			//node.unshift([]);
		});
	}

	var trees = [];
	data.trees.forEach(function(root) { trees.push(transformTree(null, root, nodes)); });

	this.nodes = nodes;
	this.trees = trees;
	this.ns = data.namespaces;
};

Node = function(parent, nt, id, nodes) {
	this.parent = parent;
	this.nt = nt;
	this.id = id;
	this.nodes = nodes;
	this.children = [];
	if (nt == NT_TEXT) Y.mix(this, Text, true);
	if (nt == NT_ELEMENT) Y.mix(this, Element, true);
};
Node.prototype.data = function() { return this.nodes[this.nt][this.id]; };
Node.prototype.descendants = function() {
	var collector = function(list, node) {
		node.children.forEach(function(child) { 
			list.push(child);
			collector(list, child); 
		});
	}
	descendants = [];
	collector(descendants, this);
	return descendants;
};
Node.prototype.text = function() { 
	text = "";
	this.descendants().forEach(function(n) { text += n.text(); });
	return text;
};

Element = {
	qName: function() { this.data()[1]; },
	attributes: function() { this.data()[2]; }
};

Text = {
	text: function() { this.data()[1]; }
}

transformTree = function(parent, data, nodes) {
	var node = new Node(parent, data.nt, data.id, nodes);
	//nodes[data.nt][data.id][0].push(node);
	if ("ch" in data) {
		data.ch.forEach(function(child) {
			node.children.push(transformTree(node, child, nodes));
		});
	}
	return node;
};