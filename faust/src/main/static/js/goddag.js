Faust.YUI().use("oop", function(Y) {
	Goddag = function() {};
	Goddag.NT_TEXT = "0";
	Goddag.NT_ELEMENT = "1";
	Goddag.NT_COMMENT = "2";
	Goddag.NT_PI = "3";

	Goddag.Graph = function(data) {
		var nodes = { "0": {}, "1": {}, "2": {}, "3": {}};
		for (var nt = 0; (nt < 4 && nt < data.nodes.length); nt++) {
			var wrap = null;
			if (nt == 0) wrap = function(n) { nodes[Goddag.NT_TEXT][n[0].toString()] = new Goddag.Text(n); };
			if (nt == 1) wrap = function(n) { nodes[Goddag.NT_ELEMENT][n[0].toString()] = new Goddag.Element(n); };
			if (nt == 2) wrap = function(n) { nodes[Goddag.NT_COMMENT][n[0].toString()] = new Goddag.Comment(n); };
			if (nt == 3) wrap = function(n) { nodes[Goddag.NT_PI][n[0].toString()] = new Goddag.ProcessingInstruction(n); };

			for (var nc = 0; nc < data.nodes[nt].length; nc++) wrap(data.nodes[nt][nc]);
		}

		var wrapTree = function(data, p, pos) { 
			var node = nodes[data.nt.toString()][data.id.toString()];
			var tree = new Goddag.Tree(p, pos, node);		
			node.trees.push(tree);

			if (data.ch)
				for (var cc = 0; cc < data.ch.length; cc++) 
					tree.children.push(wrapTree(data.ch[cc], tree, cc));

			return tree;
		};

		var roots = [];
		for (var dc = 0; dc < data.trees.length; dc++) roots.push(wrapTree(data.trees[dc], null, -1));

		this.nodes = nodes;
		this.roots = roots;
		this.ns = data.namespaces;
	};
	Goddag.Graph.prototype = {
		texts: function() { return this.nodes[Goddag.NT_TEXT]; },
		root: function(name) { 
			for (var rc = 0; rc < this.roots.length; rc++)
				if (this.roots[rc].name() == name) return this.roots[rc];
		}
	};

	Goddag.Node = function(data) {
		this.trees = [];
	};
	Goddag.Node.prototype = {
		text: function() { return "" },
		ancestors: function() { 
			var ancestors = [];
			for (var tc = 0; tc < this.trees.length; tc++) {
				ancestors = ancestors.concat(this.trees[tc].ancestors());
			}
			return ancestors;
		}
	};

	Goddag.Element = function(data) {
		Goddag.Element.superclass.constructor.call(this, data);
		this.name = data[1];
		this.attrs = {};
		Y.each(data[2], function(a) { this.attrs[a[0]] = a[1]; }, this);
	};
	Y.extend(Goddag.Element, Goddag.Node);

	Goddag.Text = function(data) {
		Goddag.Text.superclass.constructor.call(this, data);
		this.content = data[1];
	};
	Y.extend(Goddag.Text, Goddag.Node, {
		text: function() { return this.content; }
	});

	Goddag.Comment = function(data) {
		Goddag.Comment.superclass.constructor.call(this, data);
		this.content = data[1];
	}
	Y.extend(Goddag.Comment, Goddag.Node);

	Goddag.ProcessingInstruction = function(data) {
		Goddag.ProcessingInstruction.superclass.constructor.call(this, data);
		this.target = data[1];
		this.data = data[2];
	};
	Y.extend(Goddag.ProcessingInstruction, Goddag.Node);

	Goddag.Tree = function(p, pos, node) {
		this.p = p;
		this.pos = pos;
		this.node = node;
		this.children = [];
	}
	Goddag.Tree.prototype = {
		ancestors: function() {
			return (function(list, tree) {
				if (tree.p == null) return list;
				list.push(tree.p);
				return arguments.callee(list, tree.p);
			})([], this);
		},
		descendants: function() {
			return (function(list, tree) {
				for (var cc = 0; cc < tree.children.length; cc++) {
					list.push(tree.children[cc]); 
					arguments.callee(list, tree.children[cc]); 				
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
});
