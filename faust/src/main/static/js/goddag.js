const NT_TEXT = "0";
const NT_ELEMENT = "1";
const NT_COMMENT = "2";
const NT_PI = "3";

MultiRootedTree = function(data) {
	var nodes = { "0": {}, "1": {}, "2": {}, "3": {}};
	for (var nt = 0; (nt < 4 && nt < data.nodes.length); nt++) {
		var nodeWrapper = null;
		if (nt == 0) nodeWrapper = function(n) { nodes[NT_TEXT][n[0].toString()] = new Text(n); };
		if (nt == 1) nodeWrapper = function(n) { nodes[NT_ELEMENT][n[0].toString()] = new Element(n); };
		if (nt == 2) nodeWrapper = function(n) { nodes[NT_COMMENT][n[0].toString()] = new Comment(n); };
		if (nt == 3) nodeWrapper = function(n) { nodes[NT_PI][n[0].toString()] = new ProcessingInstruction(n); };
		
		for (var nc = 0; nc < data.nodes[nt].length; nc++) 
			nodeWrapper(data.nodes[nt][nc]);
	}

	var treeNodeWrapper = function(data, p, pos) { 
		var node = nodes[data.nt.toString()][data.id.toString()];
		var tn = new TreeNode(p, pos, node);		
		node.treeNodes.push(tn);
		
		if (data.ch != null)
			for (var cc = 0; cc < data.ch.length; cc++)
				tn.children.push(treeNodeWrapper(data.ch[cc], tn, cc));

		return tn;
	};

	var roots = [];
	for (var dc = 0; dc < data.trees.length; dc++) 
		roots.push(treeNodeWrapper(data.trees[dc], null, -1));

	this.nodes = nodes;
	this.roots = roots;
	this.ns = data.namespaces;
};
MultiRootedTree.prototype = {
	texts: function() { return this.nodes[NT_TEXT]; },
	root: function(name) { 
		for (var rc = 0; rc < this.roots.length; rc++)
			if (this.roots[rc].name() == name) return this.roots[rc];
	}
};

Node = function(data) {
	this.treeNodes = [];
};
Node.prototype = {
	text: function() { return "" },
	ancestors: function() { 
		var ancestors = [];
		for (var tc = 0; tc < this.treeNodes.length; tc++) {
			ancestors = ancestors.concat(this.treeNodes[tc].ancestors());
		}
		return ancestors;
	}
};

Element = function(data) {
	Node.call(this, data);
	this.name = data[1];
	this.attributes = data[2];
};
Element.prototype = {
	__proto__: Node.prototype
};

Text = function(data) {
	Node.call(this, data);
	this.content = data[1];
};
Text.prototype = {
	__proto__: Node.prototype,
	text: function() { return this.content; }
};

Comment = function(data) {
	Node.call(this, data);
	this.content = data[1];
}
Comment.prototype = {
	__proto__: Node.prototype
};

ProcessingInstruction = function(data) {
	Node.call(this, data);
	this.target = data[1];
	this.data = data[2];
};
ProcessingInstruction.prototype = {
	__proto__: Node.prototype
};

TreeNode = function(p, pos, node) {
	this.p = p;
	this.pos = pos;
	this.node = node;
	this.children = [];
}
TreeNode.prototype = {
	ancestors: function() {
		return (function(list, tn) {
			if (tn.p == null) return list;
			list.push(tn.p);
			return arguments.callee(list, tn.p);
		})([], this);
	},
	descendants: function() {
		return (function(list, tn) {
			for (var cc = 0; cc < tn.children.length; cc++) {
				list.push(tn.children[cc]); 
				arguments.callee(list, tn.children[cc]); 				
			}
			return list;
		})([], this);
	},
	text: function() { 
		var text = this.node.text();
		var descendants = this.descendants();
		for (var dc = 0; dc < descendants.length; dc++) text = text + descendants[dc].node.text(); 
		return text;
	},
	name: function() { return this.node.name; },
	attributes: function() { return this.node.attributes; }
	
};